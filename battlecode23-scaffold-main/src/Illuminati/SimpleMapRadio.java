package Illuminati;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class SimpleMapRadio {


    public static final int MAP_MODE_INDEX = 63;
    public static final int MAP_VOLUME_SIZE = 32;
    public static final int MAP_INDEX_OFFSET = 24;
    private RobotController rc;

    public SimpleMapRadio(RobotController rc){
        this.rc = rc;
    }

    public Deque<Integer> emptyArraySlots;

    public void clearAll() throws GameActionException {
        for(int i = 0 ; i< MAP_VOLUME_SIZE ; i++){
            rc.writeSharedArray(i + MAP_INDEX_OFFSET , (int)(0b1111111111111111) );
        }
    }

    public boolean writeBlock(SimpleMap.SimplePckg pckg){
        try{
            if(emptyArraySlots==null || emptyArraySlots.isEmpty()){
                return false;
            } else {
                rc.writeSharedArray( emptyArraySlots.pop() , pckg.serialize());
                return true;
            }
        }catch (GameActionException ex){
            return false;
        }
    }

    public List<SimpleMap.SimplePckg> readAndCacheEmpty() throws GameActionException {
        emptyArraySlots = new LinkedList<>();
        List<SimpleMap.SimplePckg> mapDiff = new ArrayList<>(32);
        for(int i = 0; i < MAP_VOLUME_SIZE; i++){
            int serialized = rc.readSharedArray(i + MAP_INDEX_OFFSET);
            SimpleMap.SimplePckg pckg = new SimpleMap.SimplePckg(serialized);
            if(isMsgValid(pckg)){
                mapDiff.add(pckg);
            } else {
                emptyArraySlots.add(i + MAP_INDEX_OFFSET);
            }
        }
        return mapDiff;
    }

    /**
     * Suggest Don't use this, unless you have to
     * @param map
     * @return
     * @throws GameActionException
     */
    public Deque<Integer> findEmpty(SimpleMap map) throws GameActionException {
        emptyArraySlots = new LinkedList<>();
        for(int i = 0; i < MAP_VOLUME_SIZE; i++){
            int serialized = rc.readSharedArray(i + MAP_INDEX_OFFSET);
            SimpleMap.SimplePckg pckg = new SimpleMap.SimplePckg(serialized);
            if(!isMsgValid(pckg)){
                emptyArraySlots.add(i + MAP_INDEX_OFFSET);
            }
        }
        return emptyArraySlots;
    }

    boolean isMsgValid(SimpleMap.SimplePckg pckg){
        return ( ! isLocationOutOfBounds(pckg.location));
    }

    boolean isLocationOutOfBounds(MapLocation location){
        return (location.x<0 || location.y<0 || location.x>= GameConstants.MAP_MAX_WIDTH || location.y > GameConstants.MAP_MAX_HEIGHT);
    }

}

package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;

import java.util.Random;

public class AmplifierController {
    public static final int MIN_TARGET_PROXIMITY = 4;
    SimpleMap map;
    SimpleMapRadio mapRadio;
    RobotRadio robotRadio;
    Random rand;
    public static enum ExplorationPattern{
        COUNTERCLOCK_WISE_BORDER_SPIRAL, CLOCK_WISE_BORDER_SPIRAL, CENTER_AND_RND , TOTAL_RANDOM
    }

    public AmplifierController(SimpleMap map, SimpleMapRadio mapRadio, RobotRadio robotRadio , Random rand) {
        this.map = map;
        this.mapRadio = mapRadio;
        this.robotRadio = robotRadio;
        this.rand = rand;
    }

    MapLocation target;

    public void run (RobotController rc, int turnCount) throws GameActionException {
        map.update( mapRadio.readAndCacheEmpty() );  // get updates from map
//        List<RobotRequest> requests = robotRadio.readScoutRequest();
//        for(RobotRequest request : requests ){
//            if(request.metadata[0] == 2){
//
//            }
//        }

        if(target == null || target.distanceSquaredTo(rc.getLocation())< MIN_TARGET_PROXIMITY){
            target = new MapLocation(rand.nextInt()%rc.getMapWidth() - 1, rand.nextInt()% rc.getMapHeight() -1);
        }

        rc.move(RobotPlayer.getDirectionToLocation(rc, target));
        rc.setIndicatorLine(rc.getLocation() , target , 0,250,0); // Maybe it'll work...lets see

        senseForWellsAndBroadcast(rc);
    }

    private void senseForWellsAndBroadcast(RobotController rc) {
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length>0){
            for(WellInfo wellInfo : wells){
                SimpleMap.BasicInfo mappedValue = map.get(wellInfo.getMapLocation());
                switch (wellInfo.getResourceType()){
                    case ADAMANTIUM:
                        if(SimpleMap.BasicInfo.WELL_AD != mappedValue){
                            //if it's wrong or missing, map it and
                            SimpleMap.SimplePckg updatePckg = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.WELL_AD,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                        }
                        break;
                    case MANA:
                        if(SimpleMap.BasicInfo.WELL_MANA != mappedValue){
                            //if it's wrong or missing, map it and
                            SimpleMap.SimplePckg updatePckg = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.WELL_MANA,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                        }
                        break;
                    case ELIXIR:
                        if(SimpleMap.BasicInfo.WELL_ELIXER != mappedValue){
                            //if it's wrong or missing, map it and
                            SimpleMap.SimplePckg updatePckg = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.WELL_ELIXER,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                        }
                        break;
                }
            }
        }
    }


}

package Zilean.util;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log;

public class RobotRadio {

    public static final int[] YELLOW = new int[]{255, 255, 191};
    public static final int NUMBER_COORDS_PACKED_PER_INT = 2;
    private static RobotController rc;

    static int SCOUT_INDEX_START = 0;
    static int MAX_SCOUT_MSG_COUNT = 8;
    public RobotRadio(RobotController rc){ this.rc = rc; }

    public void cleanRequestArray() throws GameActionException {
        for(int i = 0; i < MAX_SCOUT_MSG_COUNT; i++){
            //Read the requestedNumberOfRobots. If that number is 0, then this isn't a request anymore
            int currentMsgOffset = i * RobotRequest.MSG_SIZE;
            int writeIndex = SCOUT_INDEX_START + currentMsgOffset + RobotRequest.METADATA_1_OFFSET;
            rc.writeSharedArray(writeIndex , 0);
        }
    }
    public List<RobotRequest> readScoutRequest() throws GameActionException {
        int index = SCOUT_INDEX_START;
        ArrayList<RobotRequest> requests = new ArrayList<>();
        for(int i = 0; i < MAX_SCOUT_MSG_COUNT; i++){
            //Read the requestedNumberOfRobots. If that number is 0, then this isn't a request anymore
            int currentMsgOffset = i * RobotRequest.MSG_SIZE;
            int numBotsRequest = rc.readSharedArray(SCOUT_INDEX_START + currentMsgOffset + RobotRequest.METADATA_1_OFFSET);
            boolean isRequestValid = numBotsRequest>0;
            if (isRequestValid){
                requests.add(new RobotRequest( currentMsgOffset, rc));
            }
        }
        return requests;
    }


    public boolean sendScoutRequest(MapLocation location , int requestedBots) throws GameActionException {
        RobotRequest request = new RobotRequest(location, requestedBots );
        return sendRequest(request);
    }

    public boolean sendRequest(MapLocation location , int requestedBots , int[] metadata) throws GameActionException {
        RobotRequest request = new RobotRequest(location , metadata, requestedBots);
        return sendRequest(request);
    }

    public boolean sendRequest(RobotRequest request) throws GameActionException {
        for(int i = 0; i< MAX_SCOUT_MSG_COUNT; i++){
            if(! request.canSendRequest(rc)){
                request.incrementOffset();
            } else {
                break;
            }
        }
        if(request.canSendRequest(rc)) {
            request.writeRequest(rc);
            rc.setIndicatorDot(request.location, YELLOW[0], YELLOW[1], YELLOW[2]);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendScoutAccept(RobotRequest request) throws GameActionException {
        return request.answerRequest(rc);
    }

    public static boolean canPackMapLocation(){
        return canPackMapLocation(GameConstants.MAX_SHARED_ARRAY_VALUE, GameConstants.MAP_MAX_WIDTH, GameConstants.MAP_MAX_HEIGHT);
    }

    /**
     * The math is wonky as hell...but it's tested, so it's good.
     * @param maxSharedArrayValue
     * @param maxMapWidth
     * @param maxMapHeight
     * @return
     */
    public static boolean canPackMapLocation(int maxSharedArrayValue, int maxMapWidth, int maxMapHeight){

        int bits_per_array_int = (int)(log(maxSharedArrayValue+1)/log(2));
        int bits_to_store_width = (int)Math.ceil(log(maxMapWidth+1)/log(2));
        int bits_to_store_height = (int)Math.ceil(log(maxMapHeight+1)/log(2));

        return (bits_to_store_height + bits_to_store_width) <= bits_per_array_int ;
    }

    /**
     *
     * @param offset
     * @param location
     * @return Returns next available index to write to
     */
    public static void writeMapLocationToArray(int offset , MapLocation location ) throws GameActionException {
        if(canPackMapLocation()){
            int combined = packMapLocation(location);
            rc.writeSharedArray(offset,combined);
        } else {
            rc.writeSharedArray(offset, location.x);
            rc.writeSharedArray(offset+1, location.y);
        }
    }

    /**
     * Provide a location to be updated.
     * @param offset
     * @return The next available spot to read from.
     */
    public static MapLocation readMapLocationFromArray(int offset ) throws GameActionException {
        if(canPackMapLocation()){
            int combinedValue = rc.readSharedArray(offset);
            return unpackMapLocation( combinedValue );
        } else{
            int x = rc.readSharedArray(offset);
            int y = rc.readSharedArray(offset+1);
            return new MapLocation(x,y);
        }
    }

    public static int[] unpackCompsite(int value){
        int bits_per_array_int = (int)(log(GameConstants.MAX_SHARED_ARRAY_VALUE+1)/log(2));
        int second_value_starting_bit_position = bits_per_array_int / 2 ;

        int bitMask = (int)Math.pow( 2 , second_value_starting_bit_position ) - 1;
        int[] composite = new int[2];
        composite[1] = bitMask & value;
        composite[0] = value >> second_value_starting_bit_position;
        return composite;
    }
    public static int packCompsite( int first ,int second){
        int bits_per_array_int = (int)(log(GameConstants.MAX_SHARED_ARRAY_VALUE+1)/log(2));
        int y_mask = first << (bits_per_array_int / 2); // move bits to correct position
        return ( y_mask | second) ; //put the bits together into the same int.
    }

    static MapLocation unpackMapLocation( int value ){
        int[] mapCoords = unpackCompsite(value);
        return new MapLocation(mapCoords[0], mapCoords[1]);
    }

    static int packMapLocation(MapLocation location){
        return packCompsite(location.x,location.y);
    }

    public static int[] unpackCompsiteQuad(int value){
        int bits_per_array_int = (int)(log(GameConstants.MAX_SHARED_ARRAY_VALUE+1)/log(2));
        int second_value_starting_bit_position = bits_per_array_int / 4 ;
        int bitMask = (int)Math.pow( 2 , second_value_starting_bit_position ) - 1;
        int[] composite = new int[4];
        for(int i = 0; i< 4; i++){
            //read the first 4 bits
            composite[i] = bitMask & value;
            //knock off those first four bits
            value = value >> second_value_starting_bit_position;
        }
        return composite;
    }
    public static int packCompsiteQuad( int[] quads){
        int bits_per_array_int = (int)(log(GameConstants.MAX_SHARED_ARRAY_VALUE+1)/log(2));
        int y_mask = (1 << (bits_per_array_int / 4)) - 1; // all ones for first 4 bits , 1111
        int composite = 0;
        for(int i = 3 ; i>=0 ; i--){
            //make sure only 4 bits are added to the binary
            int maskedInput = ( y_mask & quads[i]);
            //move left 4, put the 4 new data bits on the front
            composite =  ( composite << (bits_per_array_int/4) ) | ( maskedInput ) ; //put the bits together into the same int.
        }
        return composite;
    }


}

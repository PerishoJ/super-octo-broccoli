package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

public class RobotRequest {
    public static final int[] GREEN = new int[]{0, 255, 0};
    public static final int MSG_SIZE ;
    public static int COORD_OFFSET ;
    public static int PLACEHOLDER_OFFSET ;

    public static int METADATA_1_OFFSET = 0;
    public static int METADATA_2_OFFSET;


    public static int LAST_OFFSET_POSITION;
    public MapLocation location;
    public int[] metadata = new int[4];


    public int commArrayOffset;

    public boolean isWritten = false;

    static{
        MSG_SIZE =  3 ;//(RobotRadio.canPackMapLocation() ?  2 :  3) ;//TODO rem to inc if we use placeholder value
        LAST_OFFSET_POSITION = MSG_SIZE * RobotRadio.MAX_SCOUT_MSG_COUNT + RobotRadio.SCOUT_INDEX_START;
        METADATA_1_OFFSET = 0;
        METADATA_2_OFFSET = METADATA_1_OFFSET + 1;
        COORD_OFFSET = METADATA_2_OFFSET + 1;
        //JUST in case we want to add some more data to this...for shits and giggles
        PLACEHOLDER_OFFSET = COORD_OFFSET + (RobotRadio.canPackMapLocation()?1:2) ;
    }
    /**
     * Creates a Request that is to be sent to the communications array
     *
     * @param location
     * @param metadata
     * @throws GameActionException
     */
    public RobotRequest(MapLocation location, int[] metadata, int requestedBots) throws GameActionException {
        this.location = location;
        setRequestedBotsNum(requestedBots);
        setMetadata(metadata);
        this.commArrayOffset = 0;
    }

    public RobotRequest(MapLocation location, int requestedRobots) throws GameActionException {
        this.location = location;
        this.metadata = new int[4];
        for(int i = 0 ; i<4 ; i++){
            metadata[i]=0;
        }
        setRequestedBotsNum( requestedRobots );
        this.commArrayOffset = 0;
    }


    /**
     * Used to read a request off the stack
     *
     * @param commArrayOffset
     * @param rc
     * @throws GameActionException
     */
    public RobotRequest(int commArrayOffset, RobotController rc) throws GameActionException {
        location = RobotRadio.readMapLocationFromArray(commArrayOffset + COORD_OFFSET);
        int metadata1 = rc.readSharedArray(commArrayOffset + METADATA_1_OFFSET);
        int metadata2 = rc.readSharedArray(commArrayOffset + METADATA_2_OFFSET);
        metadata = readMetaData(metadata1 , metadata2);
        this.commArrayOffset = commArrayOffset;
    }

    public static int[] combineInt(int[] a, int[] b){
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private int[] readMetaData(int metadataRaw1 , int metadataRaw2){
        int[] m1 = RobotRadio.unpackCompsite(metadataRaw1);
        int[] m2 = RobotRadio.unpackCompsite(metadataRaw2);
        return combineInt(m1, m2);
    }

    /**
     * Create a brand new request using the data from this object
     *
     * @param rc
     * @throws GameActionException
     */
    public void writeRequest(RobotController rc) throws GameActionException {
        rc.writeSharedArray(commArrayOffset + METADATA_1_OFFSET, RobotRadio.packCompsite(metadata[0],metadata[1]));
        rc.writeSharedArray(commArrayOffset + METADATA_2_OFFSET, RobotRadio.packCompsite(metadata[2],metadata[3]));
        RobotRadio.writeMapLocationToArray( commArrayOffset + COORD_OFFSET, new MapLocation(location.x,location.y));
    }

    //We leave out the bot count, just because
    public int[] getMetadata(){
        return Arrays.copyOfRange(metadata,1,3);
    }

    public void setMetadata(int[] mtData){
        metadata[1] = mtData[0];
        metadata[2] = mtData[1];
        metadata[3] = mtData[2];
    }

    public void setRequestedBotsNum(int requestedBotsNum){
        metadata[0] = requestedBotsNum;
    }

    public int getRequestedBotsNum(){
        return metadata[0];
    }

    /**
     * @param rc
     * @return true if the request could actually be answered
     * @throws GameActionException
     */
    public boolean answerRequest(RobotController rc) throws GameActionException {
        if (metadata[0] > 0) {
            metadata[0]--;
            rc.writeSharedArray(commArrayOffset + METADATA_1_OFFSET, RobotRadio.packCompsite(metadata[0],metadata[1]));
            rc.setIndicatorDot(location, GREEN[0], GREEN[1], GREEN[2]);
            return true;
        } else {
            return false;
        }
    }

    public boolean canSendRequest(RobotController rc){
        try {
            int requestedBots = rc.readSharedArray( this.commArrayOffset + METADATA_1_OFFSET);
            return requestedBots==0;
        } catch (GameActionException e) {
            return false;
        }
    }

    public int incrementOffset(){
        this.commArrayOffset += RobotRequest.MSG_SIZE;
        //don't write past the place we alotted.
        if(this.commArrayOffset>LAST_OFFSET_POSITION) {
            this.commArrayOffset = LAST_OFFSET_POSITION;
        }
        return commArrayOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotRequest request = (RobotRequest) o;
        return Arrays.equals(metadata,request.metadata) && Objects.equals(location, request.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, metadata);
    }
}

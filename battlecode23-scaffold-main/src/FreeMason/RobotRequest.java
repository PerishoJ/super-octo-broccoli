package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Objects;

public class RobotRequest {
    public static final int[] GREEN = new int[]{0, 255, 0};
    public static int MSG_SIZE = 3;
    public static int X_OFFSET = 1;
    public static int Y_OFFSET = 2;
    public static int BOTS_OFFSET = 0;

    public static int LAST_OFFSET_POSITION;
    public MapLocation location;
    public int numberOfRequestedBots = 1;
    public int commArrayOffset;

    public boolean isWritten = false;

    static{
        LAST_OFFSET_POSITION = MSG_SIZE * RobotRadio.MAX_MSG_COUNT + RobotRadio.INDEX_START;
    }
    /**
     * Creates a Request that is to be sent to the communications array
     *
     * @param location
     * @param numberOfRequestedBots
     * @param commArrayOffset
     * @throws GameActionException
     */
    public RobotRequest(MapLocation location, int numberOfRequestedBots) throws GameActionException {
        this.location = location;
        this.numberOfRequestedBots = numberOfRequestedBots;
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
        location = new MapLocation(rc.readSharedArray(commArrayOffset + X_OFFSET), rc.readSharedArray(commArrayOffset + Y_OFFSET));
        numberOfRequestedBots = rc.readSharedArray(commArrayOffset + BOTS_OFFSET);
        this.commArrayOffset = commArrayOffset;
    }

    /**
     * Create a brand new request using the data from this object
     *
     * @param rc
     * @throws GameActionException
     */
    public void writeRequest(RobotController rc) throws GameActionException {
        rc.writeSharedArray(commArrayOffset + X_OFFSET, location.x);
        rc.writeSharedArray(commArrayOffset + Y_OFFSET, location.y);
        rc.writeSharedArray(commArrayOffset + BOTS_OFFSET, numberOfRequestedBots);
    }

    /**
     * @param rc
     * @return true if the request could actually be answered
     * @throws GameActionException
     */
    public boolean answerRequest(RobotController rc) throws GameActionException {
        if (numberOfRequestedBots > 0) {
            numberOfRequestedBots--;
            rc.writeSharedArray(commArrayOffset + 3, numberOfRequestedBots);
            rc.setIndicatorDot(location, GREEN[0], GREEN[1], GREEN[2]);
            return true;
        } else {
            return false;
        }
    }

    public boolean canSendRequest(RobotController rc){
        try {
            int requestedBots = rc.readSharedArray( this.commArrayOffset + BOTS_OFFSET);
            return requestedBots==0;
        } catch (GameActionException e) {
            return false;
        }
    }

    public int incrementOffset(){
        this.commArrayOffset += RobotRequest.MSG_SIZE;
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
        return numberOfRequestedBots == request.numberOfRequestedBots  && Objects.equals(location, request.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, numberOfRequestedBots);
    }
}

package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;

public class RobotRadio {

    public static final int[] YELLOW = new int[]{255, 255, 191};
    private RobotController rc;

    static int INDEX_START = 0;
    static int MAX_MSG_COUNT = 8;
    public RobotRadio(RobotController rc){ this.rc = rc; }

    public List<RobotRequest> readScoutRequest() throws GameActionException {
        int index = INDEX_START;
        ArrayList<RobotRequest> requests = new ArrayList<>();
        for(int i = 0 ; i < MAX_MSG_COUNT ; i++){
            //Read the requestedNumberOfRobots. If that number is 0, then this isn't a request anymore
            int currentMsgOffset = i * RobotRequest.MSG_SIZE;
            int numBotsRequest = rc.readSharedArray(INDEX_START + currentMsgOffset + RobotRequest.BOTS_OFFSET);
            boolean isRequestValid = numBotsRequest>0;
            if (isRequestValid){
                requests.add(new RobotRequest( currentMsgOffset, rc));
            }
        }
        return requests;
    }


    public boolean sendScoutRequest(MapLocation location , int botsRequested) throws GameActionException {
        RobotRequest request = new RobotRequest(location, botsRequested );
        for(int i = 0 ; i<MAX_MSG_COUNT ; i++){
            if(! request.canSendRequest(rc)){
                request.incrementOffset();
            } else {
                break;
            }
        }
        if(request.canSendRequest(rc)) {
            request.writeRequest(rc);
            rc.setIndicatorDot(location, YELLOW[0], YELLOW[1], YELLOW[2]);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendScoutAccept(RobotRequest request) throws GameActionException {
        return request.answerRequest(rc);
    }


}

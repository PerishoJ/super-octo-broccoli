package FreeMason;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CarrierUtils {
    public static void moveTowardsTarget(RobotController rc, MapLocation target) throws GameActionException {
        if(rc.isMovementReady()) {
            Direction moveDir = RobotPlayer.getDirectionToLocation(rc, target);
            for(int i = 0 ; i< Direction.values().length ; i++) {
                if(rc.canMove(moveDir) && rc.senseMapInfo(rc.getLocation().add(moveDir)).getCurrentDirection() != moveDir.opposite()) {
                    break;
                } else {
                    moveDir = moveDir.rotateRight();
                }
            };
            if(rc.canMove(moveDir)){
                rc.move(moveDir);
            }
        }
    }

}

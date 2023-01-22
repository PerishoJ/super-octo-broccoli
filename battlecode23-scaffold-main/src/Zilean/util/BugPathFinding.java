package Zilean.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class BugPathFinding {

    static Direction curDir = null;
    static void moveTowards (RobotController rc , MapLocation target) throws GameActionException{
        if(rc.getLocation().equals(target) || !rc.isMovementReady()){
            return ;
        }

        Direction dir = rc.getLocation().directionTo(target);
        if(rc.canMove(dir)){
            rc.move(dir);
            curDir = null;
        } else {
            //bug nav. Go around the object
            //can't move to dir, because obstacle
            // keep obstacle on the right side.
            if(curDir == null){
                curDir = dir;
            }
            // check all the directions
            for (int i = 0 ; i<Direction.values().length ; i++) {
                boolean isCurrentOpposite = rc.senseMapInfo(rc.getLocation().add(curDir)).getCurrentDirection().opposite().equals(curDir);
                if(rc.canMove(curDir) && !isCurrentOpposite){
                    rc.move(curDir);
                    //once a step taken, what if obstacle is moving away?
                    curDir = curDir.rotateRight();
                    break;
                } else {
                    curDir = curDir.rotateLeft();
                }
            }

        }
    }
}

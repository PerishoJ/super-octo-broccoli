package FreeMason;

import battlecode.common.*;

public class HqUtils {
    /**
     * Checks if we can build a Carrier and does so
     */
    static void buildCarrier(RobotController rc, MapLocation newLoc) throws GameActionException {
        if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
            rc.buildRobot(RobotType.CARRIER, newLoc);
        }
    }

    /**
     * Checks if we can build a standard Anchor and does so
     */
    static void buildAnchorSTD(RobotController rc) throws GameActionException {
        if (rc.canBuildAnchor(Anchor.STANDARD)) {
            // If we can build an anchor do it!
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building anchor! " + rc.getAnchor());
        }
    }

    /**
     * Checks if we can build a Launcher and does so
     */
    static void buildLauncher(RobotController rc, MapLocation newLoc) throws GameActionException {
        if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
            rc.buildRobot(RobotType.LAUNCHER, newLoc);
        }
    }
}

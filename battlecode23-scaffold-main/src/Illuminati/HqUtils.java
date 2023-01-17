package Illuminati;

import battlecode.common.*;

public class HqUtils {

    static final int MAGIC_CLEAN_NUMBER = 0x1EE7 ; //because this number is leet, bro
    public static final int CLEANING_PERIOD = 32; //completely arbitrary
    public static final int DIRTY_INDEX = GameConstants.SHARED_ARRAY_LENGTH - 1;

    /**
     * Checks if we can build a Carrier and does so
     */
    static void buildCarrier(RobotController rc, MapLocation newLoc) throws GameActionException {
        if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
            rc.buildRobot(RobotType.CARRIER, newLoc);
        }
    }

    /**
     * One unit cleans the shared array every so often.
     *
     * @param rc
     * @param turnCount
     * @param robotRadio
     * @param mapRadio
     * @throws GameActionException
     */
    static void cleanSharedArray(RobotController rc, int turnCount , RobotRadio robotRadio , SimpleMapRadio mapRadio) throws GameActionException {
        //The dirty bit ensures that only a single HQ clears the array, meaning that commands can actually be used that turn.
        boolean isCleaningTime = turnCount % CLEANING_PERIOD == 1;
        boolean isTurnAfterClean = turnCount % CLEANING_PERIOD == 2;
        boolean isDirty = ! ( rc.readSharedArray( DIRTY_INDEX ) == MAGIC_CLEAN_NUMBER);
        boolean isClean = ( rc.readSharedArray( DIRTY_INDEX ) == MAGIC_CLEAN_NUMBER);

        if(isCleaningTime){
            if(isDirty) {
                clean(robotRadio, mapRadio);
                markCleaned(rc);
            }
        } else if (isTurnAfterClean){
            if(isClean) {
                dirty(rc);
            }
        }
    }

    private static void markCleaned(RobotController rc) throws GameActionException {
        rc.writeSharedArray(DIRTY_INDEX , MAGIC_CLEAN_NUMBER);
    }

    private static void dirty(RobotController rc) throws GameActionException {
        rc.writeSharedArray(DIRTY_INDEX, 0);
    }

    private static void clean(RobotRadio robotRadio, SimpleMapRadio mapRadio) throws GameActionException {
        robotRadio.cleanRequestArray();
        mapRadio.clearAll();
    }

    /**
     * Checks if we can build a standard Anchor and does so
     */
    static void buildAnchorSTD(RobotController rc) throws GameActionException {
        if (rc.canBuildAnchor(Anchor.STANDARD)) {
            // If we can build an anchor do it!
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building STD anchor! ");
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

    static boolean buildWherever(RobotController rc, RobotType type ) throws GameActionException {
        for(int i = 0 ; i<Direction.values().length ; i++){
            Direction buildDirection = Direction.values()[i];
            MapLocation buildLocation =  rc.getLocation().add(buildDirection);
            if( rc.canBuildRobot(type,buildLocation) ){
                rc.buildRobot(type , buildLocation);
                return true;
            }
        }
        return false;
    }
}

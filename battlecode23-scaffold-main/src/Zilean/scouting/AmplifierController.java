package Zilean.scouting;

import Zilean.CarrierUtils;
import Zilean.util.RobotRadio;
import Zilean.util.SimpleMap;
import Zilean.util.SimpleMapRadio;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

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
        StringBuilder indicatorString = new StringBuilder();
        map.update( mapRadio.readAndCacheEmpty() );  // get updates from map
        //move to random places
        moveRandomly(rc, indicatorString);
        ScoutingUtils.senseForWellsAndBroadcast(rc,map,mapRadio);
        rc.setIndicatorString(indicatorString.toString());
    }

    public void moveRandomly(RobotController rc, StringBuilder indicatorString) throws GameActionException {
        boolean shouldFindNewRndTarget = target == null || target.distanceSquaredTo(rc.getLocation()) < MIN_TARGET_PROXIMITY;
        if(shouldFindNewRndTarget) {
            target = new MapLocation(Math.abs(rand.nextInt() % (rc.getMapWidth() - 1)), Math.abs(rand.nextInt() % (rc.getMapHeight() - 1)));
        }
        CarrierUtils.moveTowardsTarget(rc , this.target, indicatorString);
        rc.setIndicatorLine(rc.getLocation() , target , 0,250,0); // Maybe it'll work...lets see
        indicatorString.append("moving to "+target+ " ");
    }






}

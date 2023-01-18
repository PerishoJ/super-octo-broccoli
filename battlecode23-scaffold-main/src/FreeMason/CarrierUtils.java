package FreeMason;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CarrierUtils {
    public static void moveTowardsTarget(RobotController rc, MapLocation target, StringBuilder indicatorString) throws GameActionException {
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
                indicatorString.append(moveDir + " ");
                rc.move(moveDir);
            }
        }
    }

    public static boolean is_islandKnown (RobotController rc,
                                          Set<MapLocation> knownIslands,
                                          MapLocation island_in,
                                          StringBuilder indicatorString) throws GameActionException {
        for (MapLocation island : knownIslands) {
            int thisID = rc.senseIsland(island);

            int ID_in = rc.senseIsland(island_in);

            if (ID_in == thisID) {
                //island is already known
                return true;

            }


        }
        return false;
    }
        /*
int         	senseIsland(MapLocation loc)
                Given a location, returns the index of the island located at that location.

MapInfo	senseMapInfo(MapLocation loc)
Sense the map info at a location MapInfo includes if there is a cloud, current direction, cooldown multiplier, number of various boosts.

MapLocation[]	senseNearbyIslandLocations(int idx)
Returns an array of all locations that belong to the island with the given index that are within vision radius.

MapLocation[]	senseNearbyIslandLocations(int radiusSquared, int idx)
Returns an array of all locations that belong to the island with the given index that are within a specified radius of your robot location.

MapLocation[]	senseNearbyIslandLocations(MapLocation center, int radiusSquared, int idx)
Returns an array of all locations that belong to the island with the given index that are within a specified radius of the center location.

int[]	senseNearbyIslands()
Returns an array containing the indexes of all islands that have at least one location within vision radius


         */



}

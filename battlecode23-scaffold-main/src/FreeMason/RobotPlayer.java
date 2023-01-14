package FreeMason;

import battlecode.common.*;

import java.util.*;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    static PathFinding gameMap;

    //remember HQ location
    static MapLocation hqLocation = null;

    //remember well location
    static MapLocation wellLocation = null;
    //well blacklist
    static List<MapLocation> wellBlackList = new ArrayList<MapLocation>();

    static RobotRadio scoutingRadio;

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        //System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        scoutingRadio = new RobotRadio(rc);

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case HEADQUARTERS:     runHeadquarters(rc);  break;
                    case CARRIER:      runCarrier(rc);   break;
                    case LAUNCHER: runLauncher(rc); break;
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER:       break;
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * write hq location to commArray
     * command code 0 is hq location command code
     * two extra ints might be used for something else later, like maybe the commands it wants to signal
     */
    static void DecalareHQLocationTurn0 (RobotController rc){
        MapLocation me = rc.getLocation();
        //writeCommand(0, me.x, me.y, 0, 0);
    }

    /**
     *write hq vector for a robot id
     */


    /**
     * decide on weather to make more carriers based on how many are around you
     * return carrier id
     */
    static int carrierBuilder (RobotController rc, StringBuilder statusString) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        int carrierCount = countBotsAroundLocation(rc, myLocation, RobotType.CARRIER, rc.getTeam());
        if (carrierCount > 5) {
            statusString.append("TooCrowdedToBuildCarrier");
            return 0;
        }
        else {
            //find a square to build a carrier on
            MapLocation buildLocation = null;
            outerloop:
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 && dy != 0) {
                        MapLocation testLocation = new MapLocation(myLocation.x + dx, myLocation.y + dy);
                        //if testLocation doesn't have a robot on it, build there
                        if (!rc.canSenseRobotAtLocation(testLocation)){
                            buildLocation = testLocation;
                            break outerloop;
                        }
                    }
                }
            }
            HqUtils.buildCarrier(rc, buildLocation);
            //find id of built carrier
            RobotInfo newBuildInfo = rc.senseRobotAtLocation(buildLocation);
            return newBuildInfo.ID;
        }
    }//end carrierBuilder

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        StringBuilder statusString = new StringBuilder();
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if (turnCount == 1) {
            // Build an anchor first thing.
            HqUtils.buildAnchorSTD(rc);

        }
        if(turnCount == 2){
            rc.setIndicatorString("Calling for help");
            scoutingRadio.sendScoutRequest( new MapLocation( 5, 5), 2);
            scoutingRadio.sendScoutRequest( new MapLocation( 20, 20), 2);
        }
        if (turnCount < 4) {
            // Let's try to build a carrier first.
            rc.setIndicatorString("Trying to build a carrier first");
            HqUtils.buildCarrier(rc, newLoc);
        }
        else{
            // If we don't have an anchor, build one!
            if (rc.getNumAnchors(Anchor.STANDARD) < 1 &&
                    rc.getResourceAmount(ResourceType.ADAMANTIUM) > 200 &&
                    rc.getResourceAmount(ResourceType.MANA) > 200) {
                HqUtils.buildAnchorSTD(rc);
            }
            if (rng.nextBoolean()) {
                // Let's try to build a carrier.
                rc.setIndicatorString("Trying to build a carrier");
                carrierBuilder(rc, statusString);
            } else {
                // Let's try to build a launcher.
                rc.setIndicatorString("Trying to build a launcher");
                HqUtils.buildLauncher(rc, newLoc);
            }
        }

    }



    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        StringBuilder statusString = new StringBuilder();
        boolean wasHqFound = !(hqLocation == null);
        if (!wasHqFound) {
            hqLocation = findHq(rc);
        }
        //ANSWER THE CALL!
        if (turnCount == 5){
            List<RobotRequest> requests = scoutingRadio.readScoutRequest();
            if(!requests.isEmpty()){
                RobotRequest acceptedRequest = requests.get(0);
                scoutingRadio.sendScoutAccept( acceptedRequest );
                statusString.append("Heard Radio Call to ("+acceptedRequest.location.x+","+acceptedRequest.location.y+")");
            }
        }
        if(wasHqFound) {
            statusString.append("HQ:" + hqLocation + ". ");
        }
        //if hq has an anchor, pick it up.
        if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD)) {
            rc.takeAnchor(hqLocation, Anchor.STANDARD);
        }
        else if (rc.canTakeAnchor(hqLocation, Anchor.ACCELERATING)) {
            rc.takeAnchor(hqLocation, Anchor.ACCELERATING);
        }
        if (rc.getAnchor() != null) { // If I have an anchor singularly focus on getting it to the first island I see
            anchorDelivery(rc, statusString);
        }
        //if it is full of either resource, go home.
        final int CARRIER_THRESHOLD = (int)(GameConstants.CARRIER_CAPACITY * 0.8f);
        boolean isCarrierFull = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) ) >= (CARRIER_THRESHOLD) ;
        if(isCarrierFull) {
            depositToHQ(rc, statusString);
        }

        // Try to gather from squares around us.
        boolean didGather = gatherNearbyResources(rc, statusString);

        // Occasionally try out the carriers attack
        carrierAttack(rc, statusString);

        // If we can see a well, move towards it
        wellLogic2(rc, statusString);

        statusString.append("NoOP");
        rc.setIndicatorString(statusString.toString());
    }//end runCarrier

    /**
     * Searches nearby for the Headquarters. This should be run to init the bot.
     */
    static MapLocation findHq(RobotController rc) throws GameActionException {
        MapLocation headQuarters = null;
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for( RobotInfo bot : robots){
            if(bot.getType() == RobotType.HEADQUARTERS ){
                headQuarters = bot.getLocation();
            }
        }
        return headQuarters;
    }

    /**
     * Default anchor delivery logic
     */
    static void anchorDelivery(RobotController rc, StringBuilder statusString) throws GameActionException {
        int[] islands = rc.senseNearbyIslands();
        Set<MapLocation> islandLocs = new HashSet<>();
        for (int id : islands) {
            MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
            islandLocs.addAll(Arrays.asList(thisIslandLocs));
        }
        if (islandLocs.size() > 0) {
            MapLocation islandLocation = islandLocs.iterator().next();
            //rc.setIndicatorString("Moving my anchor towards " + islandLocation);
            statusString.append("Moving my anchor towards " + islandLocation + ". ");
            while (!rc.getLocation().equals(islandLocation)) {
                Direction dir = getDirectionToLocation(rc, islandLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
            if (rc.canPlaceAnchor()) {
                //rc.setIndicatorString("Huzzah, placed anchor!");
                statusString.append("Huzzah, placed anchor! ");
                rc.placeAnchor();
            }
        }
    }

    /**
     * Check all adjacent squares and gather if we can
     */
    static boolean gatherNearbyResources(RobotController rc, StringBuilder statusString) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        statusString.append( " Trying to mine from well location");
        if(rc.getWeight()==GameConstants.CARRIER_CAPACITY){
            statusString.append("This mofo can't hold no mo!");
            return false;
        }
        for(WellInfo well : wells){
            if(rc.canCollectResource(well.getMapLocation(),-1)){
                rc.collectResource(well.getMapLocation() , -1 );
                statusString.append("OHHH YEAH BABY! That's the good stuff!!!/n");
                statusString.append("Slurping up " + well.getResourceType()+"/n");
                return true;
            }
        }
        return false;//nothing around. Probably should do something else.
    }

    /**
     * Return to HQ and deposit all resources
     */
    static void depositToHQ(RobotController rc, StringBuilder statusString) throws GameActionException {
        //are we close enough to deposit resources?
        statusString.append("isFull. ");
        if (rc.canTransferResource(hqLocation, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))) {
            //yes - deposit resources
            rc.transferResource(hqLocation, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
            statusString.append("Depositing Ma. ");
        }
        else if (rc.canTransferResource(hqLocation, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))) {
            //yes - deposit resources
            rc.transferResource(hqLocation, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
            statusString.append("Depositing Ad. ");
        }
        else if (rc.canTransferResource(hqLocation, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR))) {
            //yes - deposit resources
            rc.transferResource(hqLocation, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR));
            statusString.append("Depositing Ex. ");
        }
        else {
            //no - try to move towards hqLocation
            //rc.setIndicatorString("Moving full Carrier towards hqLocation: ");// + hqLocation.toString());
            statusString.append("Returning to HQ. ");
            //check if we are close enough to deposit resources to hq and if not, try moving closer.
            int distanceToHQ = rc.getLocation().distanceSquaredTo(hqLocation);
            if (distanceToHQ > 2 && rc.isMovementReady()) {
                rc.move(  getDirectionToLocation(rc , hqLocation) );
            }
        }


        //Have we init our map?

        //       if(gameMap==null){
        //gameMap = new PathFinding(where do we get our grid?);
    }

    /**
     * well logic 2 here
     * goal.. find a new well if the current one is surrounded by 3 or more of our carriers
     *
     * if we don't have a well location
     *  find one not on blacklist / walk to it
     * if we do have a well location
     *  check if it is surrounded
     *  if it is surrounded, blacklist this well
     *      find a new well / walk to it
     *  if it's not surrounded
     *      mine from it
     */
    static void wellLogic2(RobotController rc, StringBuilder statusString) throws GameActionException {
        if (wellLocation == null){
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0) {
                statusString.append("Well Detected. ");
                //only choose wells not on a blacklist of well locations
                for (WellInfo well : wells) {
                    MapLocation well_location = well.getMapLocation();
                    if (!wellBlackList.contains(well_location)) {
                        wellLocation = well.getMapLocation();
                        break;
                    }
                }
                //if we don't have a valid well by now, must go find one
                if(wellLocation == null) {
                    // randomly move to find well
                    statusString.append("FindEmptyWell. ");
                    randomWalk(rc, statusString);
                }
                else { //go to the well
                    Direction dir = getDirectionToLocation(rc, wellLocation);
                    if (rc.canMove(dir))
                        rc.move(dir);
                }
            }
            else { //no well in sight
                // randomly move to find well
                statusString.append("FindEmptyWell. ");
                randomWalk(rc, statusString);
            }
        }
        else {
            // or if we know a well, go to it.
            statusString.append("Well walk. ");
            boolean blackwell = false;
            //if there are several of our carriers on a well, blacklist this well
            if (rc.getLocation().distanceSquaredTo(wellLocation) <= 20) {
                if (countBotsAroundLocation(rc, wellLocation, RobotType.CARRIER, rc.getTeam()) > 2) {
                    //blacklist this well location
                    wellBlackList.add(wellLocation);
                    blackwell = true;
                }
            }
            if (!blackwell){ //otherwise move to it
                Direction dir = getDirectionToLocation(rc, wellLocation);
                if (rc.canMove(dir))
                    rc.move(dir);
            }

        }
    }

    /**
     * detect number of roboType of team around location
     */
    static int countBotsAroundLocation (RobotController rc, MapLocation location, RobotType roboType, Team team) throws GameActionException {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation testLocation = new MapLocation(location.x + dx, location.y + dy);
                if (rc.canSenseRobotAtLocation(testLocation)) {
                    RobotInfo detectedBot = rc.senseRobotAtLocation(testLocation);
                    if(detectedBot.getTeam() == team && detectedBot.getType() == roboType) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * original well logic here
     */
    static void wellLogic(RobotController rc, StringBuilder statusString) throws GameActionException {
        if (wellLocation == null){
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0) {
                statusString.append("Well Detected. ");
                WellInfo well_one = wells[0];
                wellLocation = well_one.getMapLocation();
                Direction dir = getDirectionToLocation(rc, wellLocation);
                if (rc.canMove(dir))
                    rc.move(dir);
            }
            else {
                // randomly move to find well
                statusString.append("FindWell. ");
                randomWalk(rc, statusString);
            }
        }
        else {
            // or if we know a well, go to it.
            statusString.append("Well walk. ");
            Direction dir = getDirectionToLocation(rc, wellLocation);
            if (rc.canMove(dir))
                rc.move(dir);
        }
    }

    /**
     * if the path is blocked, rotate right
     */
    private static Direction getDirectionToLocation(RobotController rc , MapLocation location ) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        //find a direction we can actually move to
        for(int dirs = 0  ; dirs <= directions.length ; dirs ++){
            if(rc.canMove(dir)){
                break;
            } else {
                dir = dir.rotateRight();
            }
        }
        return dir;
    }

    /**
     * basic carrier attack
     */
    static void carrierAttack (RobotController rc, StringBuilder statusString) throws GameActionException {
        if (rng.nextInt(20) == 1) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemyRobots.length > 0) {
                if (rc.canAttack(enemyRobots[0].location) && enemyRobots[0].getType() != RobotType.CARRIER && enemyRobots[0].getType() != RobotType.HEADQUARTERS) {
                    rc.attack(enemyRobots[0].location);
                }
            }
        }
    }

    /**
     * Default Launcher attack enemyrobot[0]
     */
    static void launcherAttack (RobotController rc, StringBuilder statusString) throws GameActionException {
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            //MapLocation toAttack = rc.getLocation().add(Direction.EAST); //examplefuncsplayer original line
            if (rc.canAttack(toAttack)) {
                statusString.append("Attacking:" + enemies[0].location + ". ");
                rc.attack(toAttack);
            }
        }
    }

    /**
     * Move in random direction
     */
    static void randomWalk (RobotController rc, StringBuilder statusString) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            statusString.append("RandomWalk:" + dir + ". ");
            rc.move(dir);
        }
    }

    /**
     * patrol walk - defend a square by circling it
     */
    static void patrolWalk (RobotController rc, MapLocation loc) {
        //for each square around me, see if it's further away than we want to be, if not, move there
        outerloop:
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 && dy != 0) {
                    if(rc.getLocation().distanceSquaredTo(hqLocation) < 9 ){

                    }
                }
            }
        }
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        StringBuilder statusString = new StringBuilder();
        // Try to attack someone
        launcherAttack(rc, statusString);

        // Also try to move randomly.
        randomWalk(rc, statusString);

        rc.setIndicatorString(statusString.toString());
    }

}

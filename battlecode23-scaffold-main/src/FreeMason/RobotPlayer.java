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
    static int wellNumber = 0;
    static Set<MapLocation> knownWellLocations = new LinkedHashSet<MapLocation>();
    //remember island location
    static MapLocation islandLocation = null;
    static MapLocation newislandLocation = null;
    static Set<MapLocation> knownIslandLocations = new LinkedHashSet<MapLocation>();

    //well blacklist
    static List<MapLocation> wellBlackList = new ArrayList<MapLocation>();

    //remember last direction traversed
    static Direction lastDir = null;

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
        //scoutingRadio.sendScoutRequest( new MapLocation(me.x, me.y), -1);
    }

    /**
     *write enemy hq vector for a robot id
     * look at where the other hq's are... guess at symetry line and send a scout
     */


    /**
     * decide on weather to make more carriers based on how many are around you
     * return carrier id
     */
    static void carrierBuilder (RobotController rc, StringBuilder statusString) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        int carrierCount = countBotsAroundLocation(rc, myLocation, RobotType.CARRIER, rc.getTeam());
        if (carrierCount > 2) {
            statusString.append("TooCrowdedToBuildCarrier");
            //return 0;
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
            //RobotInfo newBuildInfo = rc.senseRobotAtLocation(buildLocation);
            //return newBuildInfo.ID; //this was causing exception null pointer
        }
    }//end carrierBuilder

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {

        StringBuilder statusString = new StringBuilder();
        statusString.append(turnCount + ":");

        hqlogic(rc, statusString);

        /*
        //System.out.println("turn: " + turnCount);
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if (turnCount == 1) {
            for(int i = 0 ; i < 64 ; i++){rc.writeSharedArray(i,0);} //zero out the shared array.
            // Build an anchor first thing.
            HqUtils.buildCarrier(rc, newLoc);

        }
        if(turnCount == 2){
            rc.setIndicatorString("Calling for help");
            //scoutingRadio.sendScoutRequest( new MapLocation( 5, 5), 2);
            //scoutingRadio.sendScoutRequest( new MapLocation( 20, 20), 2);
        }
        if (turnCount < 4) {
            // Let's try to build a carrier first.
            rc.setIndicatorString("Trying to build a carrier first");
            HqUtils.buildLauncher(rc, newLoc);
        }
        if (turnCount < 50){
            // If we don't have an anchor, build one!
            //if (rc.getNumAnchors(Anchor.STANDARD) < 1 &&
            //        rc.getResourceAmount(ResourceType.ADAMANTIUM) > 200 &&
            //        rc.getResourceAmount(ResourceType.MANA) > 200) {
            //    HqUtils.buildAnchorSTD(rc);
            //}
            if (rng.nextInt(10) == 1) {
                //if (rng.nextBoolean()) {
                    // Let's try to build a carrier.
                    rc.setIndicatorString("Trying to build a carrier");
                    carrierBuilder(rc, statusString);
                //}
            }
            else if (rng.nextInt(3) == 1){
                // Let's try to build a launcher.
                rc.setIndicatorString("Trying to build a launcher");
                HqUtils.buildLauncher(rc, newLoc);
            }
            else{
                if (rc.getNumAnchors(Anchor.STANDARD) < 1 &&
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) > 200 &&
                        rc.getResourceAmount(ResourceType.MANA) > 200) {
                    HqUtils.buildAnchorSTD(rc);
                }
            }
        }

         */
        rc.setIndicatorString(statusString.toString());
    }

    /**
     * test communications hq
     */
    static void hqlogic (RobotController rc, StringBuilder statusString) throws GameActionException {
        if (turnCount == 5) {
            //make a carrier
            HqUtils.buildCarrier(rc, rc.getLocation().add(Direction.EAST));
            statusString.append("build carrier. ");
        }
        if (turnCount == 4) {
            //make anchor
            HqUtils.buildAnchorSTD(rc);
            statusString.append("sending reqest: (20,23), 1");
            //send a message for a carrier to take anchor to a known island. run on default map
            scoutingRadio.sendScoutRequest(new MapLocation(20, 23), 1);
        }
    }

    /**
     * new carrier logic - depositing, picking up anchors, delivering, looking for islands/wells, reporting islands, following commands, mining wells, finding wells, attack enemy
     * if fist turn alive: find hq
     *           depositing:
     * if standing next to base, deposit
     *
     *          picking up anchors:
     *  pick up any anchors if we know of any islands
     *
     *          delivering anchors:
     * if we have an anchor, place it at cloest island we know of
     *
     *          looking for islands/wells
     * always search for islands and wells
     * if we see an island
     *  if it is unclaimed or enemy return to hq to talk about it, request anchor for island if unclaimed
     *
     *          report island we can see
     *
     *          following commands
     * look for command from communication array
     *  walk to commanded location
     *  todo: might just be adding island or well to known list.
     *
     *
     *          mining wells
     * Try to gather from squares around us.
     * if it is full of either resource, go home.
     * if we know of a well, go mine it.
     *      collect from well, return to hq, deposit and tell hq about it
     *
     *          finding wells
     * else walk away from hq
     */
    static void runCarrier2 (RobotController rc) throws GameActionException {
        StringBuilder statusString = new StringBuilder();
        statusString.append(turnCount + ":");
        //save hq you spawned from
        if( turnCount == 1) {
            hqLocation = findHq(rc);
            statusString.append("FoundHQ turn 1.");
        }
        //save other hq locations too
        if (turnCount ==2){
            //todo Save other hq locations read from communications array
        }
        //depositing:
        //are we close to home? do home things
        int distanceToHq = rc.getLocation().distanceSquaredTo(hqLocation);
        if (distanceToHq <= 2) {
            depositToHQ(rc, statusString); //moves closer to hq if it has to, deposits
            //picking up anchors:
            //if we know where to take an anchor, grab one from hq
            if (knownIslandLocations.size() > 0) {
                statusString.append("wantAnchor. ");
                //if hq has an anchor, pick it up.
                if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD)) {
                    statusString.append("pickupAnchor. ");
                    rc.takeAnchor(hqLocation, Anchor.STANDARD);
                } else if (rc.canTakeAnchor(hqLocation, Anchor.ACCELERATING)) {
                    rc.takeAnchor(hqLocation, Anchor.ACCELERATING);
                }
            }
        }

        //delivering anchors:
        if (rc.getAnchor() != null) {
            //deliver to first island in list
            anchorDelivery2(rc, knownIslandLocations, statusString); //todo might need to find cloest
        }

        //looking for islands/wells:
        //add wells in sight
        WellInfo[] visibleWells = rc.senseNearbyWells();
        if (visibleWells.length > 0) {
            for (WellInfo well : visibleWells) {
                MapLocation thisWellLocation = well.getMapLocation();
                if(thisWellLocation != null) {
                    if (!knownWellLocations.contains(thisWellLocation)) {
                        statusString.append("saveWell" + well.getResourceType() + "(" + thisWellLocation + "). ");
                        //System.out.println("savewell: " + well.getResourceType() + " at:(" + thisWellLocation + ")");
                        knownWellLocations.add(thisWellLocation);
                    }
                }
            }
        }

        //islands by team
        int[] islands = rc.senseNearbyIslands();
        Set<MapLocation> islandLocs = new HashSet<>(); //neutral islands = enum value of 2
        Set<MapLocation> ourIslandLocs = new HashSet<>();
        Set<MapLocation> enemyIslandLocs = new HashSet<>();
        for (int id : islands) {
            Team islandteam = rc.senseTeamOccupyingIsland(id);
            if (islandteam == rc.getTeam()){
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                ourIslandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            else if (islandteam != rc.getTeam()){
                if ( islandteam == Team.NEUTRAL ) { //neutral
                    MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                    islandLocs.addAll(Arrays.asList(thisIslandLocs));
                    //add islandLocs to knownIslandLocations
                    for (MapLocation island : islandLocs){
                        if (!knownIslandLocations.contains(island)) {
                            //statusString.append("remIs(" + island + "). ");
                            knownIslandLocations.add(island); //todo might need to pick cloest one to hq
                            newislandLocation = island;
                        }
                    }
                }
                else { //enemy
                    MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                    enemyIslandLocs.addAll(Arrays.asList(thisIslandLocs));
                    //todo add these too?
                    statusString.append("seeEnemyIsland. ");
                }
            }
        }

        //report island we can see
        //if unclaimed island - report this island
        if (islandLocation != null) { //todo maybe follow the one we know about first?
            //remember island exists
            islandLocation = newislandLocation;
            //todo if we see an amplifier nearby, just say it
            //else return to hq to talk about it, (request anchor for island)
            statusString.append("repI("+ islandLocation +"). ");
            int distanceToHQ = rc.getLocation().distanceSquaredTo(hqLocation);
            //todo maybe a move request that we can decide on performing later?
            if (distanceToHQ > 10 && rc.isMovementReady()) {
                Direction dir = getDirectionToLocation(rc , hqLocation);
                if (rc.canMove(dir)) {
                    statusString.append("repMove. ");
                    rc.move(dir);
                }
            }
            if (distanceToHQ <= 9 ){
                //todo transmit about island location
                statusString.append("txIs. ");
                newislandLocation = null;
            }
        }

            rc.setIndicatorString(statusString + " ...wtf after report island ");

        //look for command from communication array
        //  execute command, might just be adding island or well to known list.
        List<RobotRequest> requests = scoutingRadio.readScoutRequest();
        if(!requests.isEmpty()){
            for(RobotRequest request : requests){
                int msgType = request.getMetadata()[0]; //we have 4 bits, so 16 options here
                switch (msgType){
                    case 0: // FOUND AN ISLAND! Go here scurvy dog!
                        //deliver an anchor here
                        //Launcher version: go to this if you are a launcher
                        MapLocation cmdLocation = request.location;
                        int cmd = request.getMetadata()[1];
                        //if cmd == 0, regular anchor
                        //if cmd == 1, accelerating anchor

                        if (cmd == 0) {
                            //get regular anchor
                              //there should be one waiting at hq and we should already have it on us
                            if (rc.getAnchor() != null){
                                statusString.append("Delivering Anchor, answering call. ");
                                //set deliver state
                                Set<MapLocation> temp = new LinkedHashSet<MapLocation>();
                                temp.add(cmdLocation);
                                for (MapLocation oldWells : knownIslandLocations) {
                                    temp.add(oldWells);
                                }
                                knownIslandLocations = temp;
                                //reply to message
                                scoutingRadio.sendScoutAccept(request);
                            }
                        }
                        break;
                    case 1: //launcher "go mine here"
                        break;
                    case 2: //launcher "make elixer here"
                        break;
                    case 3:
                        break;
                    //and on it goes to 15 (16 total)
                    default:
                        break;
                }
            }
        //     RobotRequest acceptedRequest = requests.get(0);
        //     scoutingRadio.sendScoutAccept( acceptedRequest );
        //     MapLocation cmdTarget = new MapLocation(acceptedRequest.location.x, acceptedRequest.location.y);
        //     statusString.append("Heard Radio Call to ("+cmdTarget.x+","+cmdTarget.y+") ");
        //     //todo maybe a move request
        //     //move to location
        //     int distanceToXY = rc.getLocation().distanceSquaredTo(cmdTarget);
        //     if (distanceToXY > 10 && rc.isMovementReady()) {
        //         Direction dir = getDirectionToLocation(rc, cmdTarget);
        //         if (rc.canMove(dir)){
        //             rc.move(dir);
        //         }
        //     }
        }
        //*/
        //mining wells
        // Try to gather from squares around us.
        if (knownWellLocations.size() > 0) {
            statusString.append("Mine:");
            if (rc.getWeight() == GameConstants.CARRIER_CAPACITY) {
                statusString.append("Full. ");
            }
            else {
                for (MapLocation well : knownWellLocations) {
                    if (rc.getLocation().distanceSquaredTo(well) <= 2) {
                        if (rc.canCollectResource(well, -1)) {
                            statusString.append("Mining");
                            rc.collectResource(well, -1);
                        }
                    }
                }
            }
            statusString.append(". ");
        }
        rc.setIndicatorString(statusString + " ...wtf after mining wells ");

        //if it is full of either resource, go home.
        final int CARRIER_THRESHOLD = (int)(GameConstants.CARRIER_CAPACITY * 0.8f);
        boolean isCarrierFull = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) ) >= (CARRIER_THRESHOLD) ;
        if(isCarrierFull) {
            statusString.append("isFull. ");
            depositToHQ(rc, statusString);
        }

        rc.setIndicatorString(statusString + " ...wtf after deposit to HQ ");

        // if well
        //  collect from well, return to hq, deposit and tell hq about it
        // If we can see a well, move towards it

        //System.out.println("if well\n  knownWellLocations: " + knownWellLocations.size());
        if (!knownWellLocations.isEmpty()) {
            //todo pick closest well, best well?
            //decide what well to go to based on wellNumber used last time. move on from crowded wells
            //if well location is full, try next weld location

            //poor implementation
            int wellLoop = wellNumber;
            //System.out.println("wellNumber: " + wellNumber);
            for (MapLocation well : knownWellLocations) {
                if (wellLoop == 0){
                    //if well is crowded, go to next well
                    //System.out.println("  well Loop == 0, ");
                    if (countBotsAroundLocation(rc, well, RobotType.CARRIER, rc.getTeam()) >= 3){
                        //add one to wellNumber (go to next well)
                        //System.out.println("add to well number");
                        wellNumber++;
                        wellLoop++;
                    }
                    else{
                        //use this well number
                        //System.out.println("use this well: " + wellNumber);
                        wellLocation = well;
                    }
                    break;
                }
                wellLoop = wellLoop - 1;
            }
            Direction dir = getDirectionToLocation(rc, wellLocation);

            statusString.append("2Well:" + wellLocation + "," + dir + ". ");
            if (dir != Direction.CENTER) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }else {
                // else walk away from hq
                randomWalk(rc, statusString);
            }
        }
        else {
            // else walk away from hq
            randomWalk(rc, statusString);
        }
        //attack enemy if seen

        rc.setIndicatorString(statusString + " ...wtf after if well ");

        carrierAttack(rc, statusString);
        statusString.append("NoOp");
        rc.setIndicatorString(statusString.toString());
    }//end runCarrier2



    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        runCarrier2(rc);
        /*
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
            statusString.append("MyID:" + rc.getID() + ". ");
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
            depositToHQ(rc, statusString); //moves closer to hq if it has to, deposits
        }

        // Try to gather from squares around us.
        boolean didGather = gatherNearbyResources(rc, statusString);

        // Occasionally try out the carriers attack
        carrierAttack(rc, statusString);

        // If we can see a well, move towards it
        wellLogic2(rc, statusString);

        statusString.append("NoOP");
        rc.setIndicatorString(statusString.toString());
        */
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
        if (!islandLocs.isEmpty()) {
            MapLocation islandLocation = islandLocs.stream().findFirst().get();
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
     * anchor delivery
     */
    static void anchorDelivery2(RobotController rc, Set<MapLocation> knownIslandLocations, StringBuilder statusString) throws GameActionException {
        if (!knownIslandLocations.isEmpty()) {
            MapLocation islandLocation = knownIslandLocations.stream().findFirst().get();
            statusString.append("Moving my anchor towards " + islandLocation + ". ");
            while (!rc.getLocation().equals(islandLocation)) {
                Direction dir = getDirectionToLocation(rc, islandLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
            if (rc.canPlaceAnchor()) {
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
        statusString.append( "Mine. ");
        if(rc.getWeight()==GameConstants.CARRIER_CAPACITY){
            statusString.append("Full. ");
            return false;
        }
        for(WellInfo well : wells){
            if(rc.canCollectResource(well.getMapLocation(),-1)){
                rc.collectResource(well.getMapLocation() , -1 );
                statusString.append(well.getResourceType());
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
        if (rc.canTransferResource(hqLocation, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))) {
            //yes - deposit resources
            rc.transferResource(hqLocation, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
            statusString.append("DepoMa. ");
        }
        else if (rc.canTransferResource(hqLocation, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))) {
            //yes - deposit resources
            rc.transferResource(hqLocation, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
            statusString.append("DepoAd. ");
        }
        else if (rc.canTransferResource(hqLocation, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR))) {
            //yes - deposit resources
            rc.transferResource(hqLocation, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR));
            statusString.append("DepoEx. ");
        }
        else {
            //no - try to move towards hqLocation
            //check if we are close enough to deposit resources to hq and if not, try moving closer.
            int distanceToHQ = rc.getLocation().distanceSquaredTo(hqLocation);
            if (distanceToHQ > 2 && rc.isMovementReady()) {
                statusString.append("DepoWalk. ");
                Direction dir = getDirectionToLocation(rc , hqLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
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
     * detect number of robots of team around location
     */
    static int countNumBotsAroundLocation (RobotController rc, MapLocation location, Team team) throws GameActionException {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation testLocation = new MapLocation(location.x + dx, location.y + dy);
                if (rc.canSenseRobotAtLocation(testLocation)) {
                    RobotInfo detectedBot = rc.senseRobotAtLocation(testLocation);
                    if(detectedBot.getTeam() == team) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * launcher well logic here
     */
    static void wellLogic(RobotController rc, StringBuilder statusString) throws GameActionException {
        if (wellLocation == null){
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0) {
                statusString.append("Well Detected. ");
                WellInfo well_one = wells[0];
                wellLocation = well_one.getMapLocation();
                //add a random direction to the well location...twice
                Direction rdm = directions[rng.nextInt(directions.length)];
                Direction dir = getDirectionToLocation(rc, wellLocation.add(rdm).add(rdm));
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
            Direction rdm = directions[rng.nextInt(directions.length)];
            //if we are on a well, get off of it
            statusString.append("crap i'm on a well. ");
            Direction clear = findClearTile(rc);
            if (rc.getLocation() == wellLocation) {
                if (rc.canMove(clear)){
                    rc.move(clear);
                }
            }
            else{
                // or if we know a well, go to it.
                statusString.append("Well walk. ");
                //add a random direction to the well location
                Direction dir = getDirectionToLocation(rc, wellLocation.add(rdm).add(rdm));
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }

        }
    }

    /**
     * if the path is blocked, rotate right
     */
    private static Direction getDirectionToLocation(RobotController rc , MapLocation location ) throws GameActionException {
        if (location != null) {
            Direction dir = rc.getLocation().directionTo(location);
            //find a direction we can actually move to
            for (int dirs = 0; dirs < directions.length - 1; dirs++) {
                if (rc.canMove(dir)) {
                    break;
                } else {
                    dir = dir.rotateRight();
                }
            }
            return dir;
        }
        else{
            return Direction.CENTER;
        }
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
            Direction attackDir = getDirectionToLocation(rc, enemies[0].location);
            if (rc.canMove(attackDir)) {
                rc.move(attackDir);
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
     * find clear direction to walk
     */
    static Direction findClearTile(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 && dy != 0) {
                    MapLocation testLocation = new MapLocation(me.x + dx, me.y + dy);
                    Direction testDirection = me.directionTo(testLocation);
                    if(rc.canMove(testDirection)){
                        return testDirection;
                    }
                }
            }
        }
        //fail to random dir
        return directions[rng.nextInt(directions.length)];
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        StringBuilder statusString = new StringBuilder();
        // Try to attack someone and chase them
        launcherAttack(rc, statusString);


        //looking for islands/wells:
        //add wells in sight
        WellInfo[] visibleWells = rc.senseNearbyWells();
        if (visibleWells.length > 0) {
            for (WellInfo well : visibleWells) {
                MapLocation thisWellLocation = well.getMapLocation();
                if(thisWellLocation != null) {
                    if (!knownWellLocations.contains(thisWellLocation)) {
                        statusString.append("saveWell" + well.getResourceType() + "(" + thisWellLocation + "). ");
                        //System.out.println("savewell: " + well.getResourceType() + " at:(" + thisWellLocation + ")");
                        knownWellLocations.add(thisWellLocation);
                    }
                }
            }
        }

        //islands by team
        int[] islands = rc.senseNearbyIslands();
        Set<MapLocation> islandLocs = new HashSet<>(); //neutral islands = enum value of 2
        Set<MapLocation> ourIslandLocs = new HashSet<>();
        Set<MapLocation> enemyIslandLocs = new HashSet<>();
        for (int id : islands) {
            Team islandteam = rc.senseTeamOccupyingIsland(id);
            if (islandteam == rc.getTeam()){
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                ourIslandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            else if (islandteam != rc.getTeam()){
                if ( islandteam == Team.NEUTRAL ) { //neutral
                    MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                    islandLocs.addAll(Arrays.asList(thisIslandLocs));
                    //add islandLocs to knownIslandLocations
                    for (MapLocation island : islandLocs){
                        if (!knownIslandLocations.contains(island)) {
                            statusString.append("remIs(" + island + "). ");
                            knownIslandLocations.add(island); //todo might need to pick cloest one to hq
                            newislandLocation = island;
                        }
                    }
                }
                else { //enemy
                    MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                    enemyIslandLocs.addAll(Arrays.asList(thisIslandLocs));
                    //todo add these too?
                    statusString.append("seeEnemyIsland. ");
                }
            }
        }

        //report island we can see
        //if unclaimed island - report this island
        if (islandLocation != null) { //todo maybe follow the one we know about first?
            //remember island exists
            islandLocation = newislandLocation;
            //todo if we see an amplifier nearby, just say it
            //else return to hq to talk about it, (request anchor for island)
            statusString.append("repI("+ islandLocation +"). ");
            int distanceToHQ = rc.getLocation().distanceSquaredTo(hqLocation);
            //todo maybe a move request that we can decide on performing later?
            if (distanceToHQ > 10 && rc.isMovementReady()) {
                Direction dir = getDirectionToLocation(rc , hqLocation);
                if (rc.canMove(dir)) {
                    statusString.append("repMove. ");
                    rc.move(dir);
                }
            }
            if (distanceToHQ <= 9 ){
                //todo transmit about island location
                statusString.append("txIs. ");
                newislandLocation = null;
            }
        }

        rc.setIndicatorString(statusString + " ...wtf after report island ");


        randomWalk(rc, statusString);

        rc.setIndicatorString(statusString.toString());
    }

}

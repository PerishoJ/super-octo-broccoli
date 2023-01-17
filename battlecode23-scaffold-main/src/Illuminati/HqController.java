package Illuminati;

import battlecode.common.*;

import java.util.List;
import java.util.Random;

/*GOALS
    (Local Scan) 1. Wells 2. Islands 3. Storms
    (Dwarfen Urges) OH COOL! A well! Let's strip mine that bitch
    (Zerg Rush) How much money am I making? Spend that shit! No floating money
        Artillery
        Anchors
        Miners
    (Manifest Destiny) An island? Is it ours? No? Take that shit
        Yes? Let's defend our own
    (Explore) Do we know where anything is at? No? Scout that shit out
 */
public class HqController {
    public static final int AMPLIFIER = 2;
    public static final int MINING_REQUEST = 1;
    public static final int STANDARD_MINING_CREW_SIZE = 5;
    SimpleMap map;
    SimpleMapRadio mapRadio;
    RobotRadio robotRadio;
    static final Random rng = new Random(6257);

    public HqController(SimpleMap map, SimpleMapRadio mapRadio, RobotRadio robotRadio) {
        this.map = map;
        this.mapRadio = mapRadio;
        this.robotRadio = robotRadio;
    }

    public void run (RobotController rc, int turnCount) throws GameActionException {
        String indicatorString = "";
        HqUtils.cleanSharedArray(rc, turnCount,robotRadio,mapRadio); // cleanup work is for utils classes ... nothing to do with commanding armies!
        List<SimpleMap.SimplePckg> mapDIff = mapRadio.readAndCacheEmpty();
        List<RobotRequest> requests = robotRadio.readScoutRequest();

        handleIncomingRequests(rc, requests);
        scanForNewMapFeatures(rc, mapDIff, requests);
        map.update(mapDIff);
        buildOrder(rc, turnCount);

        rc.setIndicatorString(indicatorString);
    }

    private static void handleIncomingRequests(RobotController rc, List<RobotRequest> requests) throws GameActionException {
        for(RobotRequest request : requests){
            if( MINING_REQUEST == request.metadata[0]){
                //new Carriers SHOULD be looking for a job...which SHOULD be to go to the damn mine
                HqUtils.buildWherever(rc, RobotType.CARRIER);
            }
        }
    }

    private void handleNewMapFeatures(RobotController rc, List<RobotRequest> requests, SimpleMap.SimplePckg pckg) throws GameActionException {
        //Mine new wells if you can.
        if( isThisAResourceWell (pckg)){
            //queue up some workers
            //send a command to go to well
            // IF AND ONLY IF...someone hasn't beaten you to it.
            boolean shouldRequestMine = ! isMiningRequestAlreadyInSharedArray(requests, pckg);
            if(shouldRequestMine){
                int[] requestData= {MINING_REQUEST, 0, 0};
                //send 5 guys to mine
                robotRadio.sendRequest(pckg.location , STANDARD_MINING_CREW_SIZE ,requestData );
                rc.setIndicatorLine(rc.getLocation() , pckg.location , 0 , 255, 255);// mark the dot
            }
        } else {
            //TODO handle every other map feature that can be sent (anything in the SimpleMap.)
        }
    }

    private void buildOrder(RobotController rc, int turnCount) throws GameActionException {
        //broadcast all the wells nearby
        if(turnCount == 1){
            ScoutingUtils.senseForWellsAndBroadcast(rc,map,mapRadio);
        }
        //make some launchers
        if(rc.getResourceAmount(ResourceType.MANA) > 60 && turnCount < 10) {
            HqUtils.buildWherever(rc, RobotType.LAUNCHER);
        }
        //make some miners
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) > 60 && turnCount > 1 && turnCount < 15) {
            HqUtils.buildWherever(rc, RobotType.CARRIER);
        }
        //make some launchers
        if(rc.getResourceAmount(ResourceType.MANA) > 100 && turnCount > 18 && turnCount < 26 ) {
            HqUtils.buildWherever(rc, RobotType.LAUNCHER);
        }
        // we need scouts ...
        if(rc.getResourceAmount(ResourceType.MANA) > 40 && rc.getResourceAmount(ResourceType.ADAMANTIUM) > 40 && turnCount > 22 && turnCount < 32 && rng.nextBoolean() ){
            HqUtils.buildWherever(rc, RobotType.AMPLIFIER);
            int explorationPattern = AmplifierController.ExplorationPattern.TOTAL_RANDOM.ordinal();
            int[] metadata = {AMPLIFIER, explorationPattern ,0};
            robotRadio.sendRequest( new MapLocation(0,0) , 1, metadata);
        }
        //make some miners
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) > 80 && turnCount > 26 && turnCount < 100) {
            HqUtils.buildWherever(rc, RobotType.CARRIER);
        }
        // we need scouts ... if we have lots of money, we should make them
        if(rc.getResourceAmount(ResourceType.MANA) > 300 && rc.getResourceAmount(ResourceType.ADAMANTIUM) > 40 && turnCount > 20 && turnCount < 600 && rng.nextBoolean() ){
            HqUtils.buildWherever(rc, RobotType.AMPLIFIER);
            int explorationPattern = AmplifierController.ExplorationPattern.TOTAL_RANDOM.ordinal();
            int[] metadata = {AMPLIFIER, explorationPattern ,0};
            robotRadio.sendRequest( new MapLocation(0,0) , 1, metadata);
            //wave of launchers
            if(rc.getResourceAmount(ResourceType.MANA) > 40 ) {
                HqUtils.buildWherever(rc, RobotType.LAUNCHER);
            }
        }

        //build anchor
        if(rc.getResourceAmount(ResourceType.MANA) > 100 && rc.getResourceAmount(ResourceType.ADAMANTIUM) > 100 && turnCount > 54 && turnCount < 2000 && rng.nextBoolean()) {
            if (rc.getNumAnchors(Anchor.STANDARD) < 1){
                HqUtils.buildAnchorSTD(rc);
            }
        }
        //make some miners
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) > 60 && turnCount > 54 && rng.nextBoolean()) {
            HqUtils.buildWherever(rc, RobotType.CARRIER);
        }

    }

    private void scanForNewMapFeatures(RobotController rc, List<SimpleMap.SimplePckg> mapDIff, List<RobotRequest> requests) throws GameActionException {
        //Handle any new Map features coming in off the line
        if(!mapDIff.isEmpty()){
            for(SimpleMap.SimplePckg pckg : mapDIff){
                //only look at new stuff
                boolean isThisNewShit = !map.map.containsKey(pckg.location);
                if(isThisNewShit){
                    handleNewMapFeatures(rc, requests, pckg);
                }
            }
        }
    }

    private static boolean isMiningRequestAlreadyInSharedArray(List<RobotRequest> requests, SimpleMap.SimplePckg pckg) {
        boolean isDuplicate = false;
        for(RobotRequest request : requests){
            boolean isThisTheSameRequest =  MINING_REQUEST == request.metadata[0] || request.location == pckg.location;
            if( isThisTheSameRequest ){
                isDuplicate = true;
                break;
            }
        }
        return isDuplicate;
    }

    private static boolean isThisAResourceWell(SimpleMap.SimplePckg pckg) {
        return pckg.info == MapFeature.WELL_AD || pckg.info == MapFeature.WELL_MANA || pckg.info == MapFeature.WELL_ELIXER;
    }


}
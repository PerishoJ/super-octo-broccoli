package Illuminati;

import battlecode.common.*;

import java.util.*;

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

    int lastADtotal = 0;
    int lastMNtotal = 0;
    float avgAD = 0;
    float avgMN = 0;
    int[] historicalAD = new int[5];
    int[] historicalMN = new int[5];

    public static int[] updateHistorical(int adIncome, int[] historicalAD) {
        //shift everything throwing out oldest value.
        historicalAD[4] = historicalAD[3];
        historicalAD[3] = historicalAD[2];
        historicalAD[2] = historicalAD[1];
        historicalAD[1] = historicalAD[0];
        historicalAD[0] = adIncome;
        return historicalAD;
    }

    public static float calcAverage (int currentAD, int[] historicalAD, int turnCount) {
        int totalAD = 0;
        for(int i = 0; i < historicalAD.length; i++) {
            totalAD += historicalAD[i];
        }
        float avg = 0;
        switch (turnCount) {
            case 0:
                break;
            case 1:
                avg = currentAD;
                break;
            case 2:
                avg = totalAD / 2.0f;
                break;
            case 3:
                avg = totalAD / 3.0f;
                break;
            case 4:
                avg = totalAD / 4.0f;
                break;
            default:
                avg = totalAD / 5.0f;
                break;
        }
        return avg;
    }

    Set<MapLocation> HQLocations = new HashSet<>(8); //I'm guessing there's going to be less than 8 HQ's usually
    public HqController(SimpleMap map, SimpleMapRadio mapRadio, RobotRadio robotRadio) {
        this.map = map;
        this.mapRadio = mapRadio;
        this.robotRadio = robotRadio;
    }

    public void run (RobotController rc, int turnCount) throws GameActionException {
        String indicatorString = "";
        int adNow = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int adIncome = adNow - lastADtotal;
        historicalAD = updateHistorical(adIncome, historicalAD);
        avgAD = calcAverage(adNow, historicalAD, turnCount);
        lastADtotal = adNow; //for next turn
        indicatorString += "avgAD/MN:" + avgAD + "/";

        int mnNow = rc.getResourceAmount(ResourceType.MANA);
        int mnIncome = mnNow - lastMNtotal;
        historicalMN = updateHistorical(mnIncome, historicalMN);
        avgMN = calcAverage(mnNow, historicalMN, turnCount);
        lastMNtotal = mnNow; //for next turn
        indicatorString += avgMN + ", ";
        //then spend ad/mn


        HqUtils.cleanSharedArray(rc, turnCount,robotRadio,mapRadio); // cleanup work is for utils classes ... nothing to do with commanding armies!
        List<SimpleMap.SimplePckg> mapDIff = mapRadio.readAndCacheEmpty();
        List<RobotRequest> requests = robotRadio.readScoutRequest();

        handleIncomingRequests(rc, requests);
        scanForNewMapFeatures(rc, mapDIff, requests);
        map.update(mapDIff);
        buildOrder(rc, turnCount, avgAD, avgMN);

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
        switch (pckg.info){
            //Mine new wells if you can.
            case WELL_AD:
            case WELL_MANA:
            case WELL_ELIXER:
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
                break;
            case OUR_HQ: //These are broadcast by our HQ's on turn 1
                if(! HQLocations.contains(pckg.location))
                {
                    HQLocations.add(pckg.location);
                }
                break;
            case ISLD_NEUTRAL:
                //make anchor
                break;
        }
        //TODO handle every other map feature that can be sent (anything in the SimpleMap.)

    }

    private void buildOrder(RobotController rc, int turnCount, float avgAD, float avgMN) throws GameActionException {
        if(turnCount == 1){
            //broadcast wells
            ScoutingUtils.senseForWellsAndBroadcast(rc,map,mapRadio);
            //You are an HQ...Broadcast your location. Other HQ's will pick it up.
            mapRadio.writeBlock(new SimpleMap.SimplePckg(MapFeature.OUR_HQ , rc.getLocation()));
        }
        //make some launchers
        if(rc.getResourceAmount(ResourceType.MANA) > 60 && turnCount < 10 || rc.getResourceAmount(ResourceType.MANA) > 200 && rng.nextBoolean()) {
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
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) > 60 && rc.getResourceAmount(ResourceType.ADAMANTIUM) < 200  && turnCount > 54 && rng.nextBoolean()) {
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
        return false;
    }


}
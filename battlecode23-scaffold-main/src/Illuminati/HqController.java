package Illuminati;

import battlecode.common.*;

import java.util.List;
import java.util.Random;

public class HqController {
    public static final int AMPLIFIER = 2;
    public static final int GRACE_PERIOD = 1;
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

    int turnToClean = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
    public void run (RobotController rc, int turnCount) throws GameActionException {
        String indicatorString = "";
        // hard clean of all commands every so often...because there'll be crap requests that nobody listens to clogging things up.
        // TODO This could be better...alwell
        if(turnCount % 32  == 1){
            mapRadio.clearAll();
            robotRadio.cleanRequestArray();
            return; // any subsequest requests won't work, so just wait a little bit.
        }

        //read new map entries
        List<SimpleMap.SimplePckg> mapDIff = mapRadio.readAndCacheEmpty();

        //house cleaning
        if(turnCount >= turnToClean){
            mapRadio.clearAll();
            turnToClean = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
        }
        if( mapRadio.emptyArraySlots.isEmpty() ){
            turnToClean = turnCount + GRACE_PERIOD;
        }



        List<RobotRequest> requests = robotRadio.readScoutRequest();
        //If there is a mining request, build carriers until the request has been addressed
        for(RobotRequest request : requests){
            if( MINING_REQUEST == request.metadata[0]){
                //new Carriers SHOULD be looking for a job...which SHOULD be to go to the damn mine
                HqUtils.buildWherever(rc, RobotType.CARRIER);
            }
        }

        //dumb building below

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




        int newSTuff = 0;
        int rscWells = 0;
        int dupWells = 0;
        //Look at the updates on the map
        if(!mapDIff.isEmpty()){
            for(SimpleMap.SimplePckg pckg : mapDIff){
                //only look at new stuff
                boolean isThisNewShit = !map.map.containsKey(pckg.location);
                if(isThisNewShit){
                    newSTuff++;
                    //Mine new wells if you can.
                    if( isThisAResourceWell (pckg)){
                        rscWells++;
                        //queue up some workers
                        //send a command to go to well
                        // IF AND ONLY IF...someone hasn't beaten you to it.
                        boolean isDuplicate = isDuplicate(requests, pckg);
                        if(!isDuplicate){
                            int[] requestData= {MINING_REQUEST, 0, 0};
                            //send 5 guys to mine
                            robotRadio.sendRequest(pckg.location , STANDARD_MINING_CREW_SIZE ,requestData );
                            rc.setIndicatorLine(rc.getLocation() , pckg.location , 0 , 255, 255);// mark the dot
                        }
                    }
                }
            }
        }
        map.update(mapDIff);


        indicatorString += "known wells: " + rscWells + ". ";

        //make some miners if we found a well
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) > 50 && turnCount > 54 && rscWells > 0) {
            HqUtils.buildWherever(rc, RobotType.CARRIER);
        }


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
        rc.setIndicatorString(indicatorString);
    }

    private static boolean isDuplicate(List<RobotRequest> requests, SimpleMap.SimplePckg pckg) {
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
        return pckg.info == SimpleMap.BasicInfo.WELL_AD || pckg.info == SimpleMap.BasicInfo.WELL_MANA || pckg.info == SimpleMap.BasicInfo.WELL_ELIXER;
    }


}
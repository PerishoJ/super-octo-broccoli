package FreeMason;


import battlecode.common.RobotController;

/**
 * This class handles all the logic behind writing and reading blocks of map data to/from the
 * shared array.
 */
public class MappingRadio {

    static int MAPPING_START_INDEX;
    static int MAX_MESSAGES = 16;
    RobotController rc;
    public MappingRadio(RobotController rc){
        this.rc = rc;
        //we want to start using
        MAPPING_START_INDEX = RobotRadio.MAX_SCOUT_MSG_COUNT * RobotRequest.MSG_SIZE ; // should be 3*8 = 24
        //HARD assumption, there will only be 64 as the shared array size. THIS HAS NEVER HELD UP BEFORE! what ever
        // so we're going to use 64 - 24 = 40 would be max...we'll use 32, tho. Well probably need some more ...sometime


    }


}

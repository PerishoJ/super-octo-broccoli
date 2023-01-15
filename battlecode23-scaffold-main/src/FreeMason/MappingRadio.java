package FreeMason;


import battlecode.common.RobotController;

/**
 * This class handles all the logic behind writing and reading blocks of map data to/from the
 * shared array.
 */
public class MappingRadio {

    static int MAPPING_START_INDEX;

    RobotController rc;
    public MappingRadio(RobotController rc){
        this.rc = rc;
        //we want to start using
        MAPPING_START_INDEX = RobotRadio.MAX_SCOUT_MSG_COUNT * RobotRequest.MSG_SIZE ; // should be 3*8 = 24
    }


}

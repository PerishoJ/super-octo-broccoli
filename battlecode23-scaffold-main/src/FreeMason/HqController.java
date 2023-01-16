package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class HqController {
    SimpleMap map;
    SimpleMapRadio mapRadio;
    RobotRadio robotRadio;

    public HqController(SimpleMap map, SimpleMapRadio mapRadio, RobotRadio robotRadio) {
        this.map = map;
        this.mapRadio = mapRadio;
        this.robotRadio = robotRadio;
    }

    public void run (RobotController rc, int turnCount) throws GameActionException {

        if(turnCount == 2){


        }

    }




}

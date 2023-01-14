package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.lang.Math.log;
import static org.junit.Assert.*;

public class RadioTest {

    @Test
    public void testSend() throws GameActionException {
        MockRobotController rc = new MockRobotController();
        RobotRadio uut = new RobotRadio(rc);
        RobotRequest testRequest = new RobotRequest( new MapLocation(10,13) , 7  );
        uut.sendScoutRequest(testRequest.location,testRequest.numberOfRequestedBots);
        List<RobotRequest> requests = uut.readScoutRequest();
        assertEquals( 1 , requests.size());
        assertEquals(testRequest , requests.get(0));
    }

    @Test
    public void testSendMultiple() throws GameActionException {
        MockRobotController rc = new MockRobotController();
        RobotRadio uut = new RobotRadio(rc);
        RobotRequest testRequest = new RobotRequest( new MapLocation(10,13) , 7  );
        uut.sendScoutRequest(testRequest.location,testRequest.numberOfRequestedBots);
        RobotRequest testRequest2 = new RobotRequest( new MapLocation(15,78) , 12 );
        uut.sendScoutRequest(testRequest2.location,testRequest2.numberOfRequestedBots);
        List<RobotRequest> requests = uut.readScoutRequest();
        assertEquals( 2 , requests.size());
        assertTrue(requests.contains(testRequest));
        assertTrue(requests.contains(testRequest2));
    }

    @Test
    public void testSendMultipleAnswerOne() throws GameActionException {
        MockRobotController rc = new MockRobotController();
        RobotRadio uut = new RobotRadio(rc);
        RobotRequest testRequest = new RobotRequest( new MapLocation(10,13) , 1  );
        uut.sendScoutRequest(testRequest.location,testRequest.numberOfRequestedBots);
        RobotRequest testRequest2 = new RobotRequest( new MapLocation(15,78) , 1 );
        uut.sendScoutRequest(testRequest2.location,testRequest2.numberOfRequestedBots);
        List<RobotRequest> requests = uut.readScoutRequest();
        RobotRequest acceptedRequest = requests.get(0);
        uut.sendScoutAccept(acceptedRequest );
        requests = uut.readScoutRequest();
        assertEquals( 1 , requests.size());
        assertTrue(!requests.contains(acceptedRequest));
    }

    /**
     * testing our packing util test
     */
    @Test
    public void testPackingUtils(){
        //happy path
        int maxMapHeight = 60;
        int maxMapWidth = 60;
        int shareIntBits = 16;
        int maxSharedIntValue = (1<<shareIntBits)-1;
        assertTrue( RobotRadio.canPackMapLocation(maxSharedIntValue,maxMapHeight,maxMapWidth));
        //obviously wrong
        maxMapHeight = 1028;
        maxMapWidth = 1028;
        assertFalse( RobotRadio.canPackMapLocation(maxSharedIntValue,maxMapHeight,maxMapWidth));

        //borderline good
        maxMapHeight = (1<<8)-1;
        maxMapWidth = (1<<8)-1;
        assertTrue( RobotRadio.canPackMapLocation(maxSharedIntValue,maxMapHeight,maxMapWidth));

        //borderline near miss
        maxMapHeight = (1<<8);
        maxMapWidth = (1<<8);
        assertFalse( RobotRadio.canPackMapLocation(maxSharedIntValue,maxMapHeight,maxMapWidth));

        //even closer near miss
        maxMapHeight = (1<<8)-1;
        maxMapWidth = (1<<8);
        assertFalse( RobotRadio.canPackMapLocation(maxSharedIntValue,maxMapHeight,maxMapWidth));
    }

    @Test
    public void testPackingUtils_Can_We_Pack_for_real(){
        assertTrue( RobotRadio.canPackMapLocation
                (GameConstants.MAX_SHARED_ARRAY_VALUE,
                GameConstants.MAP_MAX_WIDTH,
                GameConstants.MAP_MAX_HEIGHT));

    }


    @Test
    public void bytePackingMapLocation(){
        int x = 10;
        int y = 30;
        MapLocation loc = new MapLocation( x , y);
        int packed = RobotRadio.packMapLocation(loc);
        MapLocation unpacked = RobotRadio.unpackMapLocation(packed);

        assertEquals(x, unpacked.x);
        assertEquals(y, unpacked.y);

    }
}

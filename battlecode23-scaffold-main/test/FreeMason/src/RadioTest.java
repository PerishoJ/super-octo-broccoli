package FreeMason.src;

import FreeMason.RobotRequest;
import FreeMason.RobotRadio;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}

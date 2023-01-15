package FreeMason;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SimpleMapRadioTest {

    public static MockRobotController rc = new MockRobotController();
    @Test
    public void testWrite() throws GameActionException {
        //GIVEN
        MapLocation loc = new MapLocation( 10 ,45);
        SimpleMap.SimplePckg pckg = new SimpleMap.SimplePckg( SimpleMap.BasicInfo.ISLD_OURS , loc);
        SimpleMapRadio radio = new SimpleMapRadio(rc);
        //need to init this damn thing bc we use invalid locations as flags
        radio.clearAll();

        //have to read radio first, to get empty blocks
        radio.readAndCacheEmpty();
        boolean didWrite = radio.writeBlock(pckg);

        //WHEN
        //emulate what another robot would read off the array
        List<SimpleMap.SimplePckg> diff = radio.readAndCacheEmpty();

        //THEN
        assertFalse(diff.isEmpty());
        assertTrue(didWrite);
        assertNotEquals( 0 , rc.SHARED_ARRAY[24]);
        assertEquals(pckg.location.x , diff.get(0).location.x);
        assertEquals(pckg.location.y , diff.get(0).location.y);
        assertEquals(pckg.info , diff.get(0).info);
    }
}

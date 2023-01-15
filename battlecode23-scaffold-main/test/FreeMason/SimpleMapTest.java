package FreeMason;

import battlecode.common.MapLocation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleMapTest {

    @Test
    public void testDeserSer(){
        SimpleMap.SimplePckg uut = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.ISLD_OURS,new MapLocation(3,6));
        int ser = uut.serialize();
        SimpleMap.SimplePckg rslt = new SimpleMap.SimplePckg(ser);
        assertEquals(uut.info , rslt.info);
        assertEquals(uut.location , rslt.location);

        //max values of enum
        uut = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.THEIR_HQ,new MapLocation(3,6));
        ser = uut.serialize();
        rslt = new SimpleMap.SimplePckg(ser);
        assertEquals(uut.info , rslt.info);
        assertEquals(uut.location , rslt.location);
    }
}

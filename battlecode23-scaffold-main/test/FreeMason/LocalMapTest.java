package FreeMason;

import battlecode.common.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LocalMapTest {

    @Test
    public void testSerializeMapCell() throws GameActionException {
        //GIVEN
        MapLocation location = new MapLocation(45,10);
        RobotType occupant = RobotType.LAUNCHER;
        Team occupantTeam = Team.B;

        LocalMap_Old.MapCell uut = new LocalMap_Old.MapCell(location,
                occupant,
                occupantTeam,
                ResourceType.ELIXIR,
                Direction.WEST,
                true,
                true,
                3);
        //WHEN
        int serialized = uut.serialize();
        LocalMap_Old.MapCell deserlzCell = new LocalMap_Old.MapCell(serialized);
        //THEN
        assertEquals(uut.resourceType  , deserlzCell.resourceType);
        assertEquals(uut.occupantTeam  , deserlzCell.occupantTeam);
        assertEquals(uut.occupantType  , deserlzCell.occupantType);
        assertEquals(uut.current       , deserlzCell.current);
        assertEquals(uut.isCloudy      , deserlzCell.isCloudy);
        assertEquals(uut.isPassable    , deserlzCell.isPassable);
        assertEquals(uut.temporalValue , deserlzCell.temporalValue);
    }

    @Test
    public void testSenseFromBlankSpaces(){
        LocalMap_Old.MapCell deserlzCell = new LocalMap_Old.MapCell(0);
        assertFalse(deserlzCell.isValidMessage());
    }
}
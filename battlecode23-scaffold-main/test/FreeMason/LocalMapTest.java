package FreeMason;

import battlecode.common.*;
import junit.framework.TestCase;
import org.junit.Test;

public class LocalMapTest extends TestCase {

    @Test
    public void testSerializeMapCell() throws GameActionException {
        //GIVEN
        MapLocation location = new MapLocation(45,10);
        RobotType occupant = RobotType.LAUNCHER;
        Team occupantTeam = Team.B;

        LocalMap.MapCell uut = new LocalMap.MapCell(location,
                occupant,
                occupantTeam,
                ResourceType.ELIXIR,
                Direction.WEST,
                true,
                true,
                3);
        //WHEN
        int serialized = uut.serialize();
        LocalMap.MapCell deserlzCell = new LocalMap.MapCell(serialized);
        //THEN
        assertEquals(uut.resourceType , deserlzCell.resourceType);
        assertEquals(uut.occupantTeam , deserlzCell.occupantTeam);
        assertEquals(uut.occupantType , deserlzCell.occupantType);
        assertEquals(uut.current , deserlzCell.current);
        assertEquals(uut.isCloudy,deserlzCell.isCloudy);
        assertEquals(uut.isPassable , deserlzCell.isPassable);
        assertEquals(uut.temporalValue , deserlzCell.temporalValue);
    }

}
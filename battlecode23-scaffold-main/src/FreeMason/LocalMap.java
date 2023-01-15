package FreeMason;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


/**
 * This nasty piece of class basically...just serializes some map data. So verbose. And so gross.
 * Just do yourself a favor and touch it as little as possible.
 *
 * TODO figure out a clever way to record modifiers w/ only 3 bits ( 0 - 7 )
 *
 */
public class LocalMap {

    public static final int TEMPORAL_VALUE_NEUTRAL = 3;
    /**
     * We can expand on this as we start implementing more complex features. Probably a good set for now
     */

    private Team ourTeam;
    static class MapCell{

        // there are four resources, we need 2 bits
        public static final int RSC_MASK = (int) (Math.pow(2, 2) - 1);
        public static final int OCCUPANT_TEAM_MASK = (int) (Math.pow(2, 2) - 1);//3 bits necessary for bot type
        public static final int ROBOT_TYPE_ORDIANL_MASK = (int) (Math.pow(2, 3) - 1);
        public static final int CURRENT_DIRECTION_ORD_MASK = (int) (Math.pow(2, 4) - 1);
        public static final int NULL_TYPE_ORDINAL = 7;
        public static final int INVALID_MESSAGE_FLAG = 0;
        public int temporalValue = 3; //TODO Time dialation. 0=INVALID MESSAGE, 4 = normal. 1 = slow. 7 = fast
        //We're going to throw a VERY SPECIAL flag onto the temporal value...value 0 means the whole block is invalid
        public boolean isCloudy = false;
        public boolean isPassable = true;
        MapLocation location;
        RobotType occupantType = null;
        Team occupantTeam = Team.NEUTRAL;
        Direction current = Direction.CENTER;
        ResourceType resourceType = null;
        int turnExplored = NOT_EXPLORED;
        private static final int NOT_EXPLORED = -1;
        private static final int turnsTillStale = 8; //after some time, a scanned robot has probably moved
        //Just an empty cell

        public MapCell(){}
        public MapCell( MapLocation location , RobotType occupantType, Team occupantTeam , ResourceType resourceType , Direction current , boolean isPassable , boolean isCloudy , int temporalValue) {
            this.location = location;
            this.resourceType = resourceType==null?ResourceType.NO_RESOURCE : resourceType;
            this.occupantType = occupantType;
            this.occupantTeam = occupantTeam;
            this.current = current==null? Direction.CENTER : current ;
            turnExplored = RobotPlayer.turnCount;
            this.isCloudy = isCloudy;
            this.isPassable = isPassable;
            this.temporalValue = clampTemporalValue(temporalValue);
        }

        public MapCell(int serialized){

            //get rsc
            int rscOrdinal = serialized & RSC_MASK;
            resourceType = ResourceType.values()[rscOrdinal];
            serialized = serialized>>2; // 2 bits resource type, done
            //get team
            int teamOrdinal = serialized & OCCUPANT_TEAM_MASK; // this really should ONLY need one, but NEUTRAL sucks
            occupantTeam = Team.values()[teamOrdinal];
            turnExplored = RobotPlayer.turnCount;
            serialized = serialized >>2;
            //get robot type
            int typeOrdinal = serialized & ROBOT_TYPE_ORDIANL_MASK;
            if(typeOrdinal == NULL_TYPE_ORDINAL){
                occupantType = null;
                occupantTeam = Team.NEUTRAL;
            } else {
                occupantType = RobotType.values()[typeOrdinal];
            }
            serialized = serialized >> 3;
            //get current direction
            int currentDirectionOrdianl = serialized & CURRENT_DIRECTION_ORD_MASK;
            current = Direction.values()[currentDirectionOrdianl];
            serialized = serialized >> 4;
            isPassable = ( (serialized&1)==1 );
            serialized = serialized >> 1;
            isCloudy = ((serialized & 1)==1);
            serialized = serialized>>1;
            temporalValue = serialized; //no more space left! the int is done.
        }

        public int serialize(){
            int ser = 0;
            ser = temporalValue;
            ser = ser << 1;
            ser = ser | (isCloudy?1:0);
            ser = ser << 1;
            ser = ser | (isPassable?1:0);
            ser = ser << 4 ;
            ser = ser | current.ordinal(); //4 bits
            ser = ser << 3;

            if(occupantType == null || occupantTeam == null){
                ser = ser | NULL_TYPE_ORDINAL ; //highest number a 3 bit digit can record
                ser = ser << 2;
                ser = ser | Team.NEUTRAL.ordinal(); //2 bits
            } else {
                ser = ser | occupantType.ordinal(); //3 bits
                ser = ser << 2;
                ser = ser | occupantTeam.ordinal(); //2 bits
            }

            ser  = ser << 2 ;
            ser = ser | resourceType.ordinal();//last on the stack

            return ser;
        }

        public boolean isExplored(){
            return (turnExplored != NOT_EXPLORED);
        }

        public boolean isStale(){
            return (turnExplored+turnsTillStale<RobotPlayer.turnCount);
        }

        /**
         * We ran out of bits, so we use the temporal values to ensure the
         * message is good before reading.
         * An HQ should zero out the array at startup, so this will always read invalid until we init it.
         * @return
         */
        public boolean isValidMessage(){
            return temporalValue != INVALID_MESSAGE_FLAG;
        }
    }

    static Map<MapLocation,MapCell> gameMap;

    public LocalMap(RobotController rc){
        //INCREDIBLY bytecode heavy...but super fast later.
        //Costs probably a couple MiB memory/unit. So if we have 1000 units, the game is gonna drag hard.
        gameMap = new HashMap<>(rc.getMapWidth() * rc.getMapHeight());
        // gameMap = new TreeMap<>(); //slower for algorithms...but WON't peg byte codes and memory
        ourTeam = rc.getTeam();
    }

    public MapCell getLocation(MapLocation location){
        return gameMap.get(location);
    };

    public MapCell getLocation(int x,int y){
        return getLocation(new MapLocation(x,y));
    }

    //charting is kinda expensive...we'll need to throttle this
    public void chartLocation ( RobotInfo bot , ResourceType resource , MapInfo info) throws GameActionException {
        //TODO add temporal values ... once we actually start using them. It adds a good chunk of bytecode
        int temporalValue = TEMPORAL_VALUE_NEUTRAL;
        // temporalValue = temporalValue + info.getNumBoosts(ourTeam) - info.getNumDestabilizers(ourTeam);
        temporalValue = clampTemporalValue(temporalValue);
        MapCell cell = new MapCell(
             info.getMapLocation(),
             bot.type,
             bot.team,
             resource,
             info.getCurrentDirection(),
             info.isPassable(),
             info.hasCloud(),
             temporalValue
        );
    }

    /**
     * keep the temporal value in range
     *   1 <= Value <=  7
     * @param tmplrVal
     * @return
     */
    private static int clampTemporalValue(int tmplrVal){
        tmplrVal = tmplrVal > 7 ? 7 : tmplrVal;
        tmplrVal = tmplrVal < 1 ? 1 : tmplrVal;
        return tmplrVal;
    }
}

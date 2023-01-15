package FreeMason;

import battlecode.common.MapLocation;

import java.util.Map;
import java.util.TreeMap;

public class SimpleMap {

    public enum BasicInfo {
        NEUTRAL,OURS,OURS_ACCEL,THEIRS,THEIRS_ACCEL,WELL_AD,WELL_MANA,WELL_ELIXER;
    }

    public Map<MapLocation, BasicInfo> map = new TreeMap<>(); //maybe should be hashmap, but fuck it

    public static int serialize(MapLocation location, BasicInfo info){
        int ser = 0;
        ser = location.x;
        ser = ser << 6;
        ser = location.y | ser;
        ser = ser << 6;
        ser = ser | info.ordinal();
        return ser;
    }

    static class SimplePckg {
        SimpleMap.BasicInfo info;
        MapLocation location;

        public int serialize(){
            return SimpleMap.serialize(location,info);
        }
        public SimplePckg(BasicInfo info, MapLocation location) {
            this.info = info;
            this.location = location;
        }

        public SimplePckg (int serializedPair){
            int infoOrdinal = (0b111 & serializedPair);
            BasicInfo info = BasicInfo.values()[infoOrdinal];
            serializedPair = serializedPair >> 3; //next
            int y = (0b111111 * serializedPair);
            serializedPair = serializedPair >> 6;
            int x = (0b111111 * serializedPair);
        }
    }
    public static SimplePckg deserialize(int serializedPair){
        return new SimplePckg(serializedPair);
    }

    public void put(SimplePckg pckg){
        map.put(pckg.location,pckg.info);
    }

    public BasicInfo get(MapLocation location){
        return map.get(location);
    }
}

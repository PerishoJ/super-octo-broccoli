package Illuminati;

import battlecode.common.MapLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SimpleMap {

    public enum BasicInfo {
        ISLD_NEUTRAL,ISLD_OURS,ISLD_OURS_ACCEL,ISLD_THEIRS,ISLD_THEIRS_ACCEL,
        WELL_AD,WELL_MANA,WELL_ELIXER,
        OUR_HQ,THEIR_HQ,
        STORM, CLOUDY, //only 3 left
        INVALID_BLOCK;// used for SharedArray plumbing
    }

    public Map<MapLocation, BasicInfo> map = new TreeMap<>(); //maybe should be hashmap, but fuck it

    public static int serialize(MapLocation location, BasicInfo info){
        int ser = 0;
        ser = location.x;
        ser = ser << 6;
        ser = location.y | ser;
        ser = ser << 4;
        ser = ser | info.ordinal();
        return ser;
    }

    static class SimplePckg {
        BasicInfo info;
        MapLocation location;

        public int serialize(){
            return SimpleMap.serialize(location,info);
        }
        public SimplePckg(BasicInfo info, MapLocation location) {
            this.info = info;
            this.location = location;
        }

        public SimplePckg (int serializedPair){
            int infoOrdinal = (0b1111 & serializedPair);
            if(infoOrdinal < BasicInfo.values().length){
                info = BasicInfo.values()[infoOrdinal];
            } else {
                info = BasicInfo.INVALID_BLOCK;
            }serializedPair = serializedPair >> 4; //next
            int y = (0b111111 & serializedPair);
            serializedPair = serializedPair >> 6;
            int x = (0b111111 & serializedPair);
            location = new MapLocation(x,y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimplePckg that = (SimplePckg) o;
            return info == that.info && Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(info, location);
        }
    }
    public static SimplePckg deserialize(int serializedPair){
        return new SimplePckg(serializedPair);
    }

    public void update(List<SimplePckg> diff){
        for(SimplePckg pckg : diff){
            put(pckg);
        }
    }

    public void put(SimplePckg pckg){
        map.put(pckg.location,pckg.info);
    }

    public BasicInfo get(MapLocation location){
        return map.get(location);
    }
}

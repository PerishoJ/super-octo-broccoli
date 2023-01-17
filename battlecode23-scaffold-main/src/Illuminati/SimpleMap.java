package Illuminati;

import battlecode.common.MapLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SimpleMap {

    public Map<MapLocation, MapFeature> map = new TreeMap<>(); //maybe should be hashmap, but fuck it

    public static int serialize(MapLocation location, MapFeature info){
        int ser = 0;
        ser = location.x;
        ser = ser << 6;
        ser = location.y | ser;
        ser = ser << 4;
        ser = ser | info.ordinal();
        return ser;
    }

    static class SimplePckg {
        MapFeature info;
        MapLocation location;

        public int serialize(){
            return SimpleMap.serialize(location,info);
        }
        public SimplePckg(MapFeature info, MapLocation location) {
            this.info = info;
            this.location = location;
        }

        public SimplePckg (int serializedPair){
            int infoOrdinal = (0b1111 & serializedPair);
            if(infoOrdinal < MapFeature.values().length){
                info = MapFeature.values()[infoOrdinal];
            } else {
                info = MapFeature.INVALID_BLOCK;
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

    public MapFeature get(MapLocation location){
        return map.get(location);
    }
}

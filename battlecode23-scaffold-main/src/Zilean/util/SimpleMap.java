package Zilean.util;

import battlecode.common.MapLocation;

import java.util.List;
import java.util.Map;
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

    public static MapPckg deserialize(int serializedPair){
        return new MapPckg(serializedPair);
    }

    public void update(List<MapPckg> diff){
        for(MapPckg pckg : diff){
            put(pckg);
        }
    }

    public void put(MapPckg pckg){
        map.put(pckg.location,pckg.info);
    }

    public MapFeature get(MapLocation location){
        return map.get(location);
    }
}

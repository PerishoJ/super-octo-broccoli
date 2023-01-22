package Zilean.util;

import battlecode.common.MapLocation;

import java.util.Objects;

public class MapPckg {
    public MapFeature info;
    public MapLocation location;

    public int serialize() {
        return SimpleMap.serialize(location, info);
    }

    public MapPckg(MapFeature info, MapLocation location) {
        this.info = info;
        this.location = location;
    }

    public MapPckg(int serializedPair) {
        int infoOrdinal = (0b1111 & serializedPair);
        if (infoOrdinal < MapFeature.values().length) {
            info = MapFeature.values()[infoOrdinal];
        } else {
            info = MapFeature.INVALID_BLOCK;
        }
        serializedPair = serializedPair >> 4; //next
        int y = (0b111111 & serializedPair);
        serializedPair = serializedPair >> 6;
        int x = (0b111111 & serializedPair);
        location = new MapLocation(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapPckg that = (MapPckg) o;
        return info == that.info && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, location);
    }
}

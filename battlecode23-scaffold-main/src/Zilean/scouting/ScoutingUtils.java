package Zilean.scouting;

import Zilean.util.MapFeature;
import Zilean.util.MapPckg;
import Zilean.util.SimpleMap;
import Zilean.util.SimpleMapRadio;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;

public class ScoutingUtils {

    public static void senseForWellsAndBroadcast(RobotController rc , SimpleMap map, SimpleMapRadio mapRadio) {
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length>0){
            for(WellInfo wellInfo : wells){
                MapFeature mappedValue = map.get(wellInfo.getMapLocation());
                switch (wellInfo.getResourceType()){
                    case ADAMANTIUM:
                        if(MapFeature.WELL_AD != mappedValue){
                            //if it's wrong or missing, map it and
                            MapPckg updatePckg = new MapPckg(MapFeature.WELL_AD,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 0 , 0, 255);
                        } else {
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 255, 255, 155);
                        }
                        break;
                    case MANA:
                        if(MapFeature.WELL_MANA != mappedValue){
                            //if it's wrong or missing, map it and
                            MapPckg updatePckg = new MapPckg(MapFeature.WELL_MANA,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 0 , 0, 255);
                        } else {
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 255, 255, 155);
                        }
                        break;
                    case ELIXIR:
                        if(MapFeature.WELL_ELIXER != mappedValue){
                            //if it's wrong or missing, map it and
                            MapPckg updatePckg = new MapPckg(MapFeature.WELL_ELIXER,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 0 , 0, 255);
                        } else {
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 255, 255, 155);
                        }
                        break;
                }
            }
        }
    }
}

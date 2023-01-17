package Illuminati;

import battlecode.common.RobotController;
import battlecode.common.WellInfo;

public class ScoutingUtils {

    public static void senseForWellsAndBroadcast(RobotController rc, StringBuilder indicatorString , SimpleMap map, SimpleMapRadio mapRadio) {
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length>0){
            for(WellInfo wellInfo : wells){
                SimpleMap.BasicInfo mappedValue = map.get(wellInfo.getMapLocation());
                switch (wellInfo.getResourceType()){
                    case ADAMANTIUM:
                        if(SimpleMap.BasicInfo.WELL_AD != mappedValue){
                            //if it's wrong or missing, map it and
                            SimpleMap.SimplePckg updatePckg = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.WELL_AD,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                            indicatorString.append("NEW AD well ");
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 0 , 0, 255);
                        } else {
                            indicatorString.append("see old AD well ");
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 255, 255, 155);
                        }
                        break;
                    case MANA:
                        if(SimpleMap.BasicInfo.WELL_MANA != mappedValue){
                            //if it's wrong or missing, map it and
                            SimpleMap.SimplePckg updatePckg = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.WELL_MANA,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                            indicatorString.append("NEW MANA well ");
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 0 , 0, 255);
                        } else {
                            indicatorString.append("see old MANA well ");
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 255, 255, 155);
                        }
                        break;
                    case ELIXIR:
                        if(SimpleMap.BasicInfo.WELL_ELIXER != mappedValue){
                            //if it's wrong or missing, map it and
                            SimpleMap.SimplePckg updatePckg = new SimpleMap.SimplePckg(SimpleMap.BasicInfo.WELL_ELIXER,wellInfo.getMapLocation());
                            map.put(updatePckg);
                            mapRadio.writeBlock(updatePckg);
                            indicatorString.append("NEW ELIXER well ");
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 0 , 0, 255);
                        } else {
                            indicatorString.append("see old ELIXER well ");
                            rc.setIndicatorDot(wellInfo.getMapLocation() , 255, 255, 155);
                        }
                        break;
                }
            }
        }
    }
}

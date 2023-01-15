package FreeMason;


import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all the logic behind writing and reading blocks of map data to/from the
 * shared array.
 */
public class MappingRadio {

    public static final int BLOCK_NOT_FULL = 0;
    public static final int MAPPING_VOLUME_SIZE = 32; // number of
    public static final int NUMBER_OF_BLOCKS = 4;
    public static final int MAP_MESSAGE_SIZE = 2;
    public static final int MESSAGES_PER_BLOCK = MAPPING_VOLUME_SIZE / (NUMBER_OF_BLOCKS * MAP_MESSAGE_SIZE);
    public static final int PACKED_NULL_MSG = 0;
    static int MAPPING_START_OFFSET;
    public static final int MAX_MESSAGES = MAPPING_VOLUME_SIZE / MAP_MESSAGE_SIZE ; //maybe somebody will use this.
    RobotController rc;
    private int currentBlock = 0 ;
    /**
     * Keeps track of which blocks are full, and how many turns before it should be removed.
     */
    private int[] nextGroomingTurn= new int[] {BLOCK_NOT_FULL,BLOCK_NOT_FULL,BLOCK_NOT_FULL, BLOCK_NOT_FULL};
    private int gracePeriod = 2;

    private LocalMap.MapCell[] blockData;
    public MappingRadio(RobotController rc){
        this.rc = rc;
        //we want to start using
        MAPPING_START_OFFSET = RobotRadio.MAX_SCOUT_MSG_COUNT * RobotRequest.MSG_SIZE ; // should be 3*8 = 24
        //HARD assumption, there will only be 64 as the shared array size. THIS HAS NEVER HELD UP BEFORE! what ever
        // so we're going to use 64 - 24 = 40 would be max...we'll use 32, tho. Well probably need some more ...sometime

    }

    /**
     * To be used by HEADQUARTERs or other authorities to groom the Map volume on the shared array.
     * Takes a bit of byte code, because reading the whole volume.
     * Suggest only running this every few turns
     * @param turn - the current turn of the game
     * @return
     * @throws GameActionException
     */
    public List<LocalMap.MapCellAndLoc> groomMapVolume(int turn) throws GameActionException {
        List<LocalMap.MapCellAndLoc> fullVolume = new ArrayList<>(MAPPING_VOLUME_SIZE);
        for(int blockNumber = 0 ; blockNumber < NUMBER_OF_BLOCKS ; blockNumber++) {
            if(shouldCleanBlock(blockNumber,turn)){ // after grace period, delete full blocks
                cleanBlock(blockNumber);
            } else {
                List<LocalMap.MapCellAndLoc> block = readBlock(blockNumber);
                boolean isBlockFull = block.size() == MESSAGES_PER_BLOCK;
                if(isBlockFull){
                    scheduleGrooming(blockNumber, turn); // wait a turn or two before deleting to give bots a chance to read
                }

            }
        }
        return fullVolume;
    }

    // TODO write logic to keep adding blocks to volume
    //to write, simply check the full 32 block array, and write to the first available blocks
    public List<LocalMap.MapCellAndLoc> readBlock(int blockNumber) throws GameActionException {
        //exception handling
        if(blockNumber > 3 || blockNumber < 0) {
            throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,
                    "Block number must be between 0 and " + (NUMBER_OF_BLOCKS-1) );
        }
        //figure out where the block is, grab the map info and location for each valid place you can.
        List<LocalMap.MapCellAndLoc> block = new ArrayList<>(MESSAGES_PER_BLOCK);
        int blockOffset = blockNumber * MESSAGES_PER_BLOCK * MAP_MESSAGE_SIZE;
        for(int i = 0; i< MESSAGES_PER_BLOCK; i++){
            readCellFromSharedArray(block, blockOffset, i);
        }
        return block;
    }

    public List<LocalMap.MapCellAndLoc> readVolume() throws GameActionException {
        //figure out where the block is, grab the map info and location for each valid place you can.
        List<LocalMap.MapCellAndLoc> block = new ArrayList<>(MESSAGES_PER_BLOCK);
        int blockOffset = 0;
        for(int i = 0; i< MAX_MESSAGES; i++){
            readCellFromSharedArray(block, blockOffset, i);
        }
        return block;
    }

    private void readCellFromSharedArray(List<LocalMap.MapCellAndLoc> block, int blockOffset, int msgIndex) throws GameActionException {
        int infoBlockLocation = MAPPING_START_OFFSET + blockOffset + (msgIndex * MAP_MESSAGE_SIZE);
        int compressedMapCell = rc.readSharedArray( infoBlockLocation );
        boolean isCellGood = LocalMap.MapCell.isValidMessage(compressedMapCell); // very quickly tell if the message is any good.
        if(isCellGood){
            LocalMap.MapCellAndLoc mapCellAndLoc = new LocalMap.MapCellAndLoc();
            mapCellAndLoc.cell = new LocalMap.MapCell(compressedMapCell); //deserialize fully if good msg
            //map location is 1 after the map info
            mapCellAndLoc.location = RobotRadio.readMapLocationFromArray(infoBlockLocation+1);
            block.add(mapCellAndLoc);
        }
    }

    public boolean isBlockFull(int blockNumber){
        if(blockNumber<0 || blockNumber > 3){return false;}//probably should be exception, but whatever
        return nextGroomingTurn[blockNumber] != BLOCK_NOT_FULL ;
    }

    private boolean shouldCleanBlock(int blockNumber , int turnNumber){
        if(blockNumber<0 || blockNumber > 3){return false;}//probably should be exception, but whatever
        return nextGroomingTurn[blockNumber] >= turnNumber ;
    }

    private void cleanBlock(int blockNumber) throws GameActionException {
        List<LocalMap.MapCell> block = new ArrayList<>(MESSAGES_PER_BLOCK);
        int blockOffset = blockNumber * MESSAGES_PER_BLOCK * MAP_MESSAGE_SIZE;
        for (int i = 0; i < MESSAGES_PER_BLOCK; i++) {
            int infoBlockLocation = MAPPING_START_OFFSET +blockOffset + (i* MAP_MESSAGE_SIZE);
            rc.writeSharedArray(infoBlockLocation , PACKED_NULL_MSG); // it's zero...
        }
        nextGroomingTurn[blockNumber] = BLOCK_NOT_FULL;
    }
    private void scheduleGrooming(int blockNumber , int turnNumber){
        nextGroomingTurn[blockNumber] = turnNumber + gracePeriod;
    }

}

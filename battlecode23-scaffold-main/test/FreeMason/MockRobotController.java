package FreeMason;

import battlecode.common.*;
import sun.security.provider.SHA;

public class MockRobotController implements RobotController
{


    public int[] SHARED_ARRAY = new int[64];

    {
        for(int i = 0 ; i<SHARED_ARRAY.length ; i++){
            SHARED_ARRAY[i] = 0;
        }
    }

    @Override
    public int getRoundNum() {
        return 0;
    }

    @Override
    public int getMapWidth() {
        return 0;
    }

    @Override
    public int getMapHeight() {
        return 0;
    }

    @Override
    public int getIslandCount() {
        return 0;
    }

    @Override
    public int getRobotCount() {
        return 0;
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public Team getTeam() {
        return null;
    }

    @Override
    public RobotType getType() {
        return null;
    }

    @Override
    public MapLocation getLocation() {
        return null;
    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public int getResourceAmount(ResourceType rType) {
        return 0;
    }

    @Override
    public Anchor getAnchor() {
        return null;
    }

    @Override
    public int getNumAnchors(Anchor anchorType) {
        return 0;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        return false;
    }

    @Override
    public boolean canSenseLocation(MapLocation loc) {
        return false;
    }

    @Override
    public boolean canActLocation(MapLocation loc) {
        return false;
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        return false;
    }

    @Override
    public boolean canSenseRobotAtLocation(MapLocation loc) {
        return false;
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        return null;
    }

    @Override
    public boolean canSenseRobot(int id) {
        return false;
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        return null;
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return new RobotInfo[0];
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared) throws GameActionException {
        return new RobotInfo[0];
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) throws GameActionException {
        return new RobotInfo[0];
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) throws GameActionException {
        return new RobotInfo[0];
    }

    @Override
    public boolean sensePassability(MapLocation loc) throws GameActionException {
        return false;
    }

    @Override
    public double senseCooldownMultiplier(MapLocation loc) throws GameActionException {
        return 0;
    }

    @Override
    public int senseDestabilizeTurns(MapLocation loc) throws GameActionException {
        return 0;
    }

    @Override
    public int senseBoostTurns(MapLocation loc) throws GameActionException {
        return 0;
    }

    @Override
    public int senseIsland(MapLocation loc) throws GameActionException {
        return 0;
    }

    @Override
    public int[] senseNearbyIslands() {
        return new int[0];
    }

    @Override
    public MapLocation[] senseNearbyIslandLocations(int idx) throws GameActionException {
        return new MapLocation[0];
    }

    @Override
    public MapLocation[] senseNearbyIslandLocations(int radiusSquared, int idx) throws GameActionException {
        return new MapLocation[0];
    }

    @Override
    public MapLocation[] senseNearbyIslandLocations(MapLocation center, int radiusSquared, int idx) throws GameActionException {
        return new MapLocation[0];
    }

    @Override
    public Team senseTeamOccupyingIsland(int islandIdx) throws GameActionException {
        return null;
    }

    @Override
    public int senseAnchorPlantedHealth(int islandIdx) throws GameActionException {
        return 0;
    }

    @Override
    public Anchor senseAnchor(int islandIdx) throws GameActionException {
        return null;
    }

    @Override
    public WellInfo senseWell(MapLocation loc) throws GameActionException {
        return null;
    }

    @Override
    public WellInfo[] senseNearbyWells() {
        return new WellInfo[0];
    }

    @Override
    public WellInfo[] senseNearbyWells(int radiusSquared) throws GameActionException {
        return new WellInfo[0];
    }

    @Override
    public WellInfo[] senseNearbyWells(MapLocation center, int radiusSquared) throws GameActionException {
        return new WellInfo[0];
    }

    @Override
    public WellInfo[] senseNearbyWells(ResourceType resourceType) {
        return new WellInfo[0];
    }

    @Override
    public WellInfo[] senseNearbyWells(int radiusSquared, ResourceType resourceType) throws GameActionException {
        return new WellInfo[0];
    }

    @Override
    public WellInfo[] senseNearbyWells(MapLocation center, int radiusSquared, ResourceType resourceType) throws GameActionException {
        return new WellInfo[0];
    }

    @Override
    public MapInfo senseMapInfo(MapLocation loc) throws GameActionException {
        return null;
    }

    @Override
    public MapInfo[] senseNearbyMapInfos() {
        return new MapInfo[0];
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(int radiusSquared) throws GameActionException {
        return new MapInfo[0];
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(MapLocation center) throws GameActionException {
        return new MapInfo[0];
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(MapLocation center, int radiusSquared) throws GameActionException {
        return new MapInfo[0];
    }

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return null;
    }

    @Override
    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared) throws GameActionException {
        return new MapLocation[0];
    }

    @Override
    public boolean isActionReady() {
        return false;
    }

    @Override
    public int getActionCooldownTurns() {
        return 0;
    }

    @Override
    public boolean isMovementReady() {
        return false;
    }

    @Override
    public int getMovementCooldownTurns() {
        return 0;
    }

    @Override
    public boolean canMove(Direction dir) {
        return false;
    }

    @Override
    public void move(Direction dir) throws GameActionException {

    }

    @Override
    public boolean canBuildRobot(RobotType type, MapLocation loc) {
        return false;
    }

    @Override
    public void buildRobot(RobotType type, MapLocation loc) throws GameActionException {

    }

    @Override
    public boolean canAttack(MapLocation loc) {
        return false;
    }

    @Override
    public void attack(MapLocation loc) throws GameActionException {

    }

    @Override
    public boolean canBoost() {
        return false;
    }

    @Override
    public void boost() throws GameActionException {

    }

    @Override
    public boolean canDestabilize(MapLocation loc) {
        return false;
    }

    @Override
    public void destabilize(MapLocation loc) throws GameActionException {

    }

    @Override
    public boolean canCollectResource(MapLocation loc, int amount) {
        return false;
    }

    @Override
    public void collectResource(MapLocation loc, int amount) throws GameActionException {

    }

    @Override
    public boolean canTransferResource(MapLocation loc, ResourceType rType, int amount) {
        return false;
    }

    @Override
    public void transferResource(MapLocation loc, ResourceType rType, int amount) throws GameActionException {

    }

    @Override
    public boolean canBuildAnchor(Anchor anchor) {
        return false;
    }

    @Override
    public void buildAnchor(Anchor anchor) throws GameActionException {

    }

    @Override
    public boolean canTakeAnchor(MapLocation loc, Anchor anchorType) {
        return false;
    }

    @Override
    public void takeAnchor(MapLocation loc, Anchor anchorType) throws GameActionException {

    }

    @Override
    public boolean canReturnAnchor(MapLocation loc) {
        return false;
    }

    @Override
    public void returnAnchor(MapLocation loc) throws GameActionException {

    }

    @Override
    public boolean canPlaceAnchor() {
        return false;
    }

    @Override
    public void placeAnchor() throws GameActionException {

    }

    @Override
    public int readSharedArray(int index) throws GameActionException {
        if(index<0 || index >= SHARED_ARRAY.length){ throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,"index out of rng");}
        return SHARED_ARRAY[index];
    }

    @Override
    public boolean canWriteSharedArray(int index, int value) {
        return index<64 && index>=0;
    }

    @Override
    public void writeSharedArray(int index, int value) throws GameActionException {
        if(index<0 || index >= SHARED_ARRAY.length){ throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,"index out of rng");}
        SHARED_ARRAY[index] = value;
    }

    @Override
    public void disintegrate() {

    }

    @Override
    public void resign() {

    }

    @Override
    public void setIndicatorString(String string) {

    }

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {

    }

    @Override
    public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {

    }
}

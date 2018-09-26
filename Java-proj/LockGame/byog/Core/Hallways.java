package byog.Core;

import byog.TileEngine.TETile;

import java.util.ArrayList;
import java.util.Random;

public class Hallways {

    private final int posX;
    private final int posY;
    private final int length;
    private final boolean isVertical;
    //returns true if it's generated in postive direction
    //thus, the old room is from its tail, the new room should be at its head.
    private final boolean isPositiveDir;
    //we never change WORLD here! we only add room to RAL
    private TETile[][] WORLD;

    private Random ran;



    Hallways(int pX, int pY, int l, boolean isVert, boolean posDir, TETile[][] world, Random r) {
        posX = pX;
        posY = pY;
        length = l;
        isVertical = isVert;
        isPositiveDir = posDir;
        WORLD = world;
        ran = r;
    }


    public int getPositionX() {
        return posX;
    }

    public int getPositionY() {
        return posY;
    }

    public int getLength() {
        return length;
    }

    public boolean getDirection() {
        return isVertical;
    }

    public boolean getPostivity() {
        return isPositiveDir;
    }


    Room decideRoom() {

        if (!ran.nextBoolean() && !ran.nextBoolean()) {
            return null;
        }
        int trial = 0;
        while (trial < 3) {
            int width = ran.nextInt(11) + 5;   //the room is between 5*5 and 15*15
            int height = ran.nextInt(11) + 5;
            Room returnRoom = createRoom(width, height);
            if (returnRoom.roomSizeValid()) {
                return returnRoom;
            }
            trial++;
        }
        return null;
    }

    private Room createRoom(int width, int height) {
        Room returnRoom;
        int roomPosX;
        int roomPosY;
        if (isVertical) {
            if (isPositiveDir) {
                roomPosY = posY + length;
                roomPosX = ran.nextInt(width) + (posX - width + 1);
            } else {
                roomPosY = posY - height;
                roomPosX = ran.nextInt(width) + (posX - width + 1);
            }
        } else {
            if (isPositiveDir) {
                roomPosY = ran.nextInt(height) + (posY - height + 1);
                roomPosX = posX + length;
            } else {
                roomPosY = ran.nextInt(height) + (posY - height + 1);
                roomPosX = posX - width;
            }
        }
        returnRoom = new Room(roomPosX, roomPosY, width, height, WORLD, ran);
        return returnRoom;
    }

    ArrayList<Hallways> decideHW() {
        ArrayList<Hallways> hwAL = new ArrayList<>();
        //the following 4 conditions randomly create hw on 4 edges of the room

        if (!isVertical) {
            if (ran.nextBoolean() || ran.nextBoolean()) {
                Hallways hw = createVerticalHW(false);
                if (hw != null) {
                    hwAL.add(hw);
                }
            }

            if (ran.nextBoolean() || ran.nextBoolean()) {
                Hallways hw2 = createVerticalHW(true);
                if (hw2 != null) {
                    hwAL.add(hw2);
                }
            }
        } else {
            if (ran.nextBoolean() || ran.nextBoolean()) {
                Hallways hw = createHorizontalHW(false);
                if (hw != null) {
                    hwAL.add(hw);
                }
            }

            if (ran.nextBoolean() || ran.nextBoolean()) {
                Hallways hw2 = createHorizontalHW(true);
                if (hw2 != null) {
                    hwAL.add(hw2);
                }
            }
        }
        return hwAL;
    }

    //generate a vertical HW with fixed yCoord, but with random length and random xCoord
    //the boolean var determines return true if we know the tail of the hw,
    //but we want to generate the head
    //false otherwise.

    //NOTE that these 2 methods are different from those in Room, slightly
    private Hallways createVerticalHW(boolean isPositiveDirection) {
        //because the random HW might go out of bounds often,
        //we want to randomize this process 3 times before it returns null;
        int yCoord;
        Hallways returnHW;
        for (int trial = 0; trial < 3; trial++) {
            int xCoord = ran.nextInt(length - 2) + posX + 1;

            int len = ran.nextInt(20) + 5;
            if (isPositiveDirection) {
                yCoord = posY + 1;
            } else {
                yCoord = posY - len;
            }
            returnHW = new Hallways(xCoord, yCoord, len, true, isPositiveDirection, WORLD, ran);
            if (returnHW.hallwaySizeValid()) {
                return returnHW;
            }
        }
        return null;
    }

    //generate a horizontal HW with fixed xCoord, but with random length and random yCoord
    private Hallways createHorizontalHW(boolean isPositiveDirection) {
        int xCoord;
        Hallways returnHW;
        for (int trial = 0; trial < 3; trial++) {
            int yCoord = ran.nextInt(length - 2) + posY + 1;

            int len = ran.nextInt(20) + 5;
            if (isPositiveDirection) {
                xCoord = posX + 1;
            } else {
                xCoord = posX - len;
            }
            returnHW = new Hallways(xCoord, yCoord, len, false, isPositiveDirection, WORLD, ran);
            if (returnHW.hallwaySizeValid()) {
                return returnHW;
            }
        }
        return null;
    }

    boolean hallwaySizeValid() {
        int worldHEIGHT = WORLD[0].length;
        int worldWIDTH = WORLD.length;
        if (isVertical) {
            if (posY < 4 || posY + length - 1 > worldHEIGHT - 5) {
                return false;
            }
        } else {
            if (posX < 4 || posX + length - 1 > worldWIDTH - 5) {
                return false;
            }
        }
        return true;
    }
}

package byog.Core;

import byog.TileEngine.TETile;

import java.util.ArrayList;
import java.util.Random;

public class Room {
    private final int posX;
    private final int posY;
    private final int width;
    private final int height;

    //we never change WORLD here! we only add room to RAL
    private TETile[][] WORLD;

    private Random ran;



    Room(int pX, int pY, int w, int h, TETile[][] world, Random r) {
        posX = pX;
        posY = pY;
        width = w;
        height = h;
        WORLD = world;
        ran = r;



    }


    public int getPositionX() {
        return posX;
    }

    public int getPositionY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    //package private
    //decideHW will decide the location of the hws
    //and instantiate the hws by calling hallways constructor
    //the outcome is an HW arrayList.
    ArrayList<Hallways> decideHW() {
        ArrayList<Hallways> hwAL = new ArrayList<>();
        //the following 4 conditions randomly create hw on 4 edges of the room
        if (ran.nextBoolean() || ran.nextBoolean()) {
            Hallways hw = createVerticalHW(posY - 1, false);
            if (hw != null) {
                hwAL.add(hw);
            }
        }
        if (ran.nextBoolean() || ran.nextBoolean()) {
            Hallways hw = createVerticalHW(posY + height, true);
            if (hw != null) {
                hwAL.add(hw);
            }
        }
        if (ran.nextBoolean() || ran.nextBoolean()) {
            Hallways hw = createHorizontalHW(posX - 1, false);
            if (hw != null) {
                hwAL.add(hw);
            }
        }
        if (ran.nextBoolean() || ran.nextBoolean()) {
            Hallways hw = createHorizontalHW(posX + width, true);
            if (hw != null) {
                hwAL.add(hw);
            }
        }
        return hwAL;
    }

    //generate a vertical HW with fixed yCoord, but with random length and random xCoord
    //the boolean var determines return true if we know the tail of the hw,
    //but we want to generate the head
    //false otherwise.

    //NOTE that these 2 methods are different from those in Hallways
    private Hallways createVerticalHW(int yCoordinate, boolean isPositiveDirection) {
        //because the random HW might go out of bounds often,
        //we want to randomize this process 3 times before it returns null;
        int yCoord;
        Hallways returnHW;
        for (int trial = 0; trial < 3; trial++) {
            int xCoord = ran.nextInt(width) + posX;

            int length = ran.nextInt(20) + 5;
            if (isPositiveDirection) {
                yCoord = yCoordinate;
            } else {
                yCoord = yCoordinate - length + 1;
            }
            returnHW = new Hallways(xCoord, yCoord, length, true, isPositiveDirection, WORLD, ran);
            if (returnHW.hallwaySizeValid()) {
                return returnHW;
            }
        }
        return null;
    }

    //generate a horizontal HW with fixed xCoord, but with random length and random yCoord
    private Hallways createHorizontalHW(int xCoordinate, boolean isPositiveDirection) {
        int xCoord;
        Hallways returnHW;
        for (int trial = 0; trial < 3; trial++) {
            int yCoord = ran.nextInt(height) + posY;

            int length = ran.nextInt(20) + 5;
            if (isPositiveDirection) {
                xCoord = xCoordinate;
            } else {
                xCoord = xCoordinate - length + 1;
            }
            returnHW = new Hallways(xCoord, yCoord, length, false, isPositiveDirection, WORLD, ran);
            if (returnHW.hallwaySizeValid()) {
                return returnHW;
            }
        }
        return null;
    }









    /*
    overlap method compares two rooms location and size and
     determine if they overlap with each other
    the adjacent walls are put into consideration.

    returns True if they do overlap
    False otherwise.

    Some mathematical explanation:
    if we look at only X-axis, based on posX and width
    there are only 6 possible cases.
    And overlap will return False iff
    r.(posX+width) < (posX - 1)  OR r.posX > this.(posX + width + 1)

    However, iff room r's BOTH x and y range are invalid, then the room will overlap.

    boolean overlap(Room r) {
        //boolean xRangeValid;
        //boolean yRangeValid;
        if( (r.posX + r.width) < (this.posX - 1) ||
                r.posX > (this.posX + this.width + 1)) {
            return false;
        } else if ( (r.posY + r.height) < (this.posY - 1) ||
                r.posY > (this.posY + this.height + 1)) {
            return false;
        }

        return true;
    }
    */

    boolean roomSizeValid() {
        //the x range must be between index 4 and 75,
        // for the sake of the wall and the 3 columns at each end
        //same rule applies to y
        int worldHEIGHT = WORLD[0].length;
        int worldWIDTH = WORLD.length;
        if (posX < 4 || posX + width - 1 > worldWIDTH - 5) {
            return false;
        } else if (posY < 4 || posY + height - 1 > worldHEIGHT - 5) {
            return false;
        }
        return true;
    }

    /*
    private boolean isRoomFull() {
        return roomArrayList.size() >= maxNumberR;
    }
    */
}

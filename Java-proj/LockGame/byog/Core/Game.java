package byog.Core;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class Game implements Serializable {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;
    public static final int NPCNUM = 4;
    private boolean playing = false;

    //private static Map<Long, TETile[][]> seedMap = new HashMap<>();

    static int maxNumberR;
    static int maxNumberHW;
    int lockNum;

    String moveString = "";

    private static long seed;
    Random RANDOM; //for some reason, this cannot be final????

    static ArrayList<Room> roomArrayList = new ArrayList<>();
    static ArrayList<Hallways> hallwayArrayList = new ArrayList<>();

    ArrayList<NPC> npcList = new ArrayList<>();

    TETile[][] WORLD;
    private Player player;
    Portal portalA;
    Portal portalB;

    private static ArrayList<TETile> ALTR = new ArrayList<>();
    private static ArrayList<TETile> ALTHW = new ArrayList<>();
    static String[] menuOptions = new String[]{"New Game", "N", "Load", "L", "Quit and Save", "Q"};

    private int lockedDoorNumber = 0;
    private static ArrayList<Position> positionArrayList = new ArrayList<>();

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT);
        StdDraw.clear(Color.BLACK);
        playWithInputString("N" + enterSeed() + "S");
        playing = true;
        playerGo();
    }

    private String enterSeed() {
        String input = "";

        while (true) {
            drawSeed(input);
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char key = StdDraw.nextKeyTyped();
            if (key == 's') {
                return input;
            } else if (key == '\b') {
                input = input.substring(0, input.length() - 1);
            } else {
                input += String.valueOf(key);
            }
        }
    }

    private void generateRandomWorld() {

        //fill the board with nothing
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                WORLD[x][y] = Tileset.NOTHING;
            }
        }

        maxNumberR = RANDOM.nextInt(11) + 10; //the max#Room is between 10 and 20
        maxNumberHW = RANDOM.nextInt(11) + 15;  //the max@HW is between 15 and 25


        //make the first room
        int startRoomPosX = RANDOM.nextInt(WIDTH - 20) + 5;
        int startRoomPosY = RANDOM.nextInt(HEIGHT - 20) + 5;

        int startRoomWidth = RANDOM.nextInt(11) + 5;   //the room is between 5*5 and 15*15
        int startRoomHeight = RANDOM.nextInt(11) + 5;

        Room firstRoom = new Room(startRoomPosX, startRoomPosY, startRoomWidth,
                startRoomHeight, WORLD, RANDOM);

        /*if (!firstRoom.roomSizeValid()) {
            System.out.println("firstroom size!!!!");
        }*/

        makeRoom(firstRoom);

        //System.out.println("There are " + roomArrayList.size() + " rooms");
        //System.out.println("There are " + hallwayArrayList.size() + " hw");

        checkDiagonal();
        makeWalls();

        drawDoor();

        putWand();
        putPortal();
    }

    /*
    makeHW takes a Hallways as input, check if there is space
    and then DRAW the hw and finally make its adjacent room and hws.
     */
    private void makeHW(Hallways hw) {
        if (!isHallwayFull()) {
            drawHallways(hw);
            hallwayArrayList.add(hw);


            //System.out.println("Hallway Number " + hallwayArrayList.size() + " is drawed");
            // System.out.println("Coordinates: " + hw.getPositionX() + " "
            // + hw.getPositionY() + " length: " + hw.getLength());
            // System.out.println("vertical: " + hw.getDirection() + " "
            // +  " Positive: " + hw.getPostivity());

            Room r = hw.decideRoom();
            if (r != null) {
                makeRoom(r);

                hallwayAddDoorPosition(hw);
            }


            ArrayList<Hallways> hwArray = hw.decideHW();
            for (Hallways hallway: hwArray) {
                makeHW(hallway);
            }

        }
    }

    private void drawHallways(Hallways hw) {
        int posX = hw.getPositionX();
        int posY = hw.getPositionY();
        int length = hw.getLength();
        boolean isVert = hw.getDirection();

        if (isVert) {
            for (int y = 0; y < length; y += 1) {
                WORLD[posX][posY + y] = Tileset.FLOOR;
            }
        } else {
            for (int x = 0; x < length; x += 1) {
                WORLD[posX + x][posY] = Tileset.FLOOR;
            }
        }

    }

    //return true if the door created is locked, false otherwise
    private void hallwayAddDoorPosition(Hallways hallway) {
        int posX = hallway.getPositionX();
        int posY = hallway.getPositionY();
        int length = hallway.getLength();

        if (hallway.getPostivity()) {
            if (hallway.getDirection()) {
                Position pos = new Position(posX, posY + length - 1);
                if (isValidDoor(pos)) {
                    positionArrayList.add(pos);
                }
            } else {
                Position pos = new Position(posX + length - 1, posY);
                if (isValidDoor(pos)) {
                    positionArrayList.add(pos);
                }
            }
        } else {
            Position pos = new Position(posX, posY);
            if (isValidDoor(pos)) {
                positionArrayList.add(pos);
            }
        }
    }

    /*
    makeRoom takes a Room as input, check if there is any room (no pun intended here)
    and then DRAW the room and make its adjacent hws.
     */
    private void makeRoom(Room r) {
        //check if it reaches the max Room Capacity here
        // is probably not a good idea.
        //TBH, it should be checked in "decide" method
        //but I don't want to pass another 2 vars into Room and Hallways anymore.

        /*
        Actually, I cannot give HW and Room their arraylist
        because AL must not be static, otherwise it's shared by all seeds' rooms/HW,
        which is not allowed.
        AL must be non-static, then when I construct a new Room/HW,
        I need to pass that seed-shared AL.
        first of all, it's hard from room to give hw its AL because room does not have it.
        then I basically need to give the hwAL from game to hw's constructor.
        Then it seems pretty redundant to even give HW class the ability to have HWAL.



        So ROOM and HW class builds themselves, determine their valid size and location
        check if it's out of scope
        and then decide its adjacent HW/R.

         But Game checks if Room/HW can be applied to the game,
         this includes whether it's beyond R/HW cap, or overlapped with others,

         BECAUSE only the game has the AL, R/HW only has the WORLD.
         */

        if (!isRoomFull() && !overlap(r, roomArrayList)) {
            drawRoom(r);
            roomArrayList.add(r);

            //System.out.println("Room Number " + roomArrayList.size() + " is drawed");
            // System.out.println("Coordinates: " + r.getPositionX() + " " + r.getPositionY()
            // + " size: " + r.getWidth() + " " + r.getHeight());


            ArrayList<Hallways> hwArray = r.decideHW();
            for (Hallways hw: hwArray) {
                makeHW(hw);

                roomAddDoorPosition(hw);
            }
            //System.out.println();
            // System.out.println("Room Number " + roomArrayList.size() + " has "
            // + hwArray.size() + " hws");
            // System.out.println();



        }

    }

    private void drawRoom(Room r) {
        int posX = r.getPositionX();
        int posY = r.getPositionY();
        int width = r.getWidth();
        int height = r.getHeight();
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                WORLD[posX + x][posY + y] = Tileset.FLOOR;
            }
        }
    }

    //return true if the door created is locked, false otherwise
    private void roomAddDoorPosition(Hallways hallway) {
        int posX = hallway.getPositionX();
        int posY = hallway.getPositionY();
        int length = hallway.getLength();

        if (hallway.getPostivity()) {
            Position pos = new Position(posX, posY);
            if (isValidDoor(pos)) {
                positionArrayList.add(pos);
            }
        } else {
            if (hallway.getDirection()) {
                Position pos = new Position(posX, posY + length - 1);
                if (isValidDoor(pos)) {
                    positionArrayList.add(pos);
                }
            } else {
                Position pos = new Position(posX + length - 1, posY);
                if (isValidDoor(pos)) {
                    positionArrayList.add(pos);
                }
            }
        }
    }

    private void drawDoor() {
        //System.out.println();
        //System.out.println("There are " + positionArrayList.size() + " doors");

        for (Position pos: positionArrayList) {
            int posX = pos.xPos;
            int posY = pos.yPos;
            if (RANDOM.nextBoolean()) {
                WORLD[posX][posY] = Tileset.LOCKED_DOOR;
                putKey();
            } else {
                WORLD[posX][posY] = Tileset.UNLOCKED_DOOR;
            }
            //System.out.println("Coordinates: " + posX + " "
            // + posY);


        }

    }

    private boolean isValidDoor(Position pos) {
        TETile currentTile = WORLD[pos.xPos][pos.yPos];
        if (currentTile.equals(Tileset.WALL)) {
            return false;
        }
        return isBetweenWalls(pos);
    }

    //True if pos is between 2 walls and 2 floors ONLY!
    private boolean isBetweenWalls(Position pos) {
        TETile topTile = WORLD[pos.xPos][pos.yPos + 1];
        TETile downTile = WORLD[pos.xPos][pos.yPos - 1];
        TETile leftTile = WORLD[pos.xPos - 1][pos.yPos];
        TETile rightTile = WORLD[pos.xPos + 1][pos.yPos];

        if (topTile.equals(Tileset.NOTHING) && downTile.equals(Tileset.NOTHING)
                && leftTile.equals(Tileset.FLOOR) && rightTile.equals(Tileset.FLOOR)) {
            return true;
        } else if (topTile.equals(Tileset.FLOOR) && downTile.equals(Tileset.FLOOR)
                && leftTile.equals(Tileset.NOTHING) && rightTile.equals(Tileset.NOTHING)) {
            return true;
        }
        return false;
    }

    private int countLock() {
        int num = 0;
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (WORLD[x][y] == Tileset.LOCKED_DOOR) {
                    num += 1;
                }
            }
        }
        return num;
    }

    private boolean isRoomFull() {
        return roomArrayList.size() >= maxNumberR;
    }

    private boolean isHallwayFull() {
        return hallwayArrayList.size() >= maxNumberHW;
    }

    /*
      overlap method compares the targetRoom with all the rooms in the RAL.
      False if targetRoom does not overlap with any room, thus it's valid.
      True if targetRoom overlaps even one of the existing rooms.
    */
    static boolean overlap(Room targetRoom, ArrayList<Room> roomList) {
        //boolean xRangeValid;
        //boolean yRangeValid;
        for (Room r : roomList) {
            if (overlapHelper(targetRoom, r)) {
                return true;
            }
        }
        return false;
    }

    /*
    overlapHelper checks if the targetRoom overlaps with the existed r.
    return True if it does, thus the room is invalid, False otherwise.
     Some mathematical explanation:
       if we look at only X-axis, based on posX and width
       there are only 6 possible cases.
       And overlap will return False iff
       r.(posX+width) < (posX - 1)  OR r.posX > this.(posX + width + 1)

       However, iff room r's BOTH x and y range are invalid, then the room will overlap.
     */
    static boolean overlapHelper(Room targetRoom, Room r) {
        int posXTarget = targetRoom.getPositionX();
        int posYTarget = targetRoom.getPositionY();
        int widthTarget = targetRoom.getWidth();
        int heightTarget = targetRoom.getHeight();

        int posXR = r.getPositionX();
        int posYR = r.getPositionY();
        int widthR = r.getWidth();
        int heightR = r.getHeight();
        if ((posXTarget + widthTarget) < (posXR)
                || posXTarget > (posXR + widthR)) {
            return false;
        } else if ((posYTarget + heightTarget) < (posYR)
                || posYTarget > (posYR + heightR)) {
            return false;
        }

        return true;

    }

    //I don't want to put this method in Room class
    //because there is no need to even instantiate that room
    //if the location is invalid

    /*
    Because of the fact we add our walls last,
    we don't want one case to happen:
    think about a 2 by 2 matrix,
    one diagonal entries are floor and one diagonal entries are nothing
    because those nothing tiles will be replaced by walls
    it will look pretty confusing
    because the 2 spaces(either room or HW) will seem to be connected at their corners.
     */
    private void checkDiagonal() {
        int height = WORLD[0].length;
        int width = WORLD.length;

        for (int x = 3; x < width - 4; x += 1) {
            for (int y = 3; y < height - 4; y += 1) {
                //let tileUL, tileUR, tileBL, tileBR be the corresponding tile where
                //L = left, R = right, U = upper, B = bottom
                TETile tileUL = WORLD[x][y + 1];
                TETile tileUR = WORLD[x + 1][y + 1];
                TETile tileBL = WORLD[x][y];
                TETile tileBR = WORLD[x + 1][y];
                if (tileUL.equals(Tileset.FLOOR) && tileBR.equals(Tileset.FLOOR)) {
                    if (tileBL.equals(Tileset.NOTHING) && tileUR.equals(Tileset.NOTHING)) {
                        if (RANDOM.nextBoolean()) {
                            WORLD[x + 1][y + 1] = Tileset.FLOOR;
                        } else {
                            WORLD[x][y] = Tileset.FLOOR;
                        }

                    }
                } else if (tileBL.equals(Tileset.FLOOR) && tileUR.equals(Tileset.FLOOR)) {
                    if (tileUL.equals(Tileset.NOTHING) && tileBR.equals(Tileset.NOTHING)) {
                        if (RANDOM.nextBoolean()) {
                            WORLD[x][y + 1] = Tileset.FLOOR;
                        } else {
                            WORLD[x + 1][y] = Tileset.FLOOR;
                        }
                    }

                }
            }
        }

    }

    /*
    makeWalls checks all NOTHING tiles and check if it has adjacent FLOOR tile
    if it does, change the nothing tile into wall tile.
     */

    private void makeWalls() {
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                if (WORLD[x][y].equals(Tileset.NOTHING)) {
                    if (makeWallsHelpers(x, y)) {
                        WORLD[x][y] = Tileset.WALL;
                    }
                }
            }
        }
    }

    //this helper returns true if a tile has an adjacent FLOOR tile.
    //we just need
    private boolean makeWallsHelpers(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (WORLD[x + i][y + j].equals(Tileset.FLOOR)) {
                    return true;
                }
            }
        }
        return false;
    }

    //putKey() put a key at a random floor
    private boolean putKey() {
        while (true) {
            int keyPosX = RANDOM.nextInt(WIDTH - 4) + 2;
            int keyPosY = RANDOM.nextInt(HEIGHT - 4) + 2;
            TETile targetTile = WORLD[keyPosX][keyPosY];
            if (targetTile.equals(Tileset.FLOOR)) {
                WORLD[keyPosX][keyPosY] = Tileset.KEY;
                return true;
            }
        }
    }

    private void putWand() {
        int x = RANDOM.nextInt(WIDTH);
        int y = RANDOM.nextInt(HEIGHT);

        while (!WORLD[x][y].equals(Tileset.FLOOR)) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }
        WORLD[x][y] = Tileset.WAND;
    }

    private void putPortal() {
        portalA = new Portal(RANDOM, WORLD);
        portalB = new Portal(RANDOM, WORLD);
        portalA.pairUp(portalB);
        portalB.pairUp(portalA);
    }

    private void putNPC() {
        int tmp = 0;
        while (tmp < NPCNUM) {
            npcList.add(new NPC(RANDOM, WORLD, player));
            tmp += 1;
        }
    }

    public void drawSeed(String s) {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear(Color.black);

        // Draw the GUI
        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, HEIGHT - 1, "Welcome to the game!");
        StdDraw.line(0, HEIGHT - 2, WIDTH, HEIGHT - 2);

        Font bigFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(bigFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.text(midWidth, midHeight, s);
        StdDraw.show();
    }

    private void drawOver() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear(Color.black);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.white);

        StdDraw.text(midWidth, HEIGHT * 0.75, "Game Over!");

        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, midHeight - 2, "New Game (N)");
        StdDraw.text(midWidth, midHeight - 4, "Quit (Q)");

        StdDraw.show();
    }

    private void drawWin() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear(Color.black);
        Font font = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.white);

        StdDraw.text(midWidth, HEIGHT * 0.75, "You Won the Game!");

        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, midHeight - 2, "New Game (N)");
        StdDraw.text(midWidth, midHeight - 4, "Quit (Q or Esc)");

        StdDraw.show();
    }

    private void drawPause() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear(Color.black);
        Font font = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.white);

        StdDraw.text(midWidth, HEIGHT * 0.75, "Pause");

        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, midHeight - 2, "Go on (G)");
        StdDraw.text(midWidth, midHeight - 4, "Quit (Q)");

        StdDraw.show();
    }

    void playerGo() {
        int delay = 0;
        while (player.getLife() > 0 && lockNum > 0) {
            if (delay > 15) {
                for (int i = 0; i < npcList.size(); i++) {
                    npcList.get(i).move();
                }
                delay = 0;
            }
            delay += 1;

            if (StdDraw.hasNextKeyTyped()) {
                char d = StdDraw.nextKeyTyped();

                if (Integer.valueOf(d) == 27) {
                    System.exit(0);
                } else if (d == ':') {
                    break;
                } else if (d == 'j' & player.hasWand) {
                    player.hasWand = false;
                    putWand();
                } else {
                    lockNum -= player.move(d, portalA, portalB);
                }
            }
            ter.renderFrame(WORLD, player);
        }
        if (player.getLife() <= 0) {
            drawOver();
            startOver();
        } else if (lockNum <= 0) {
            drawWin();
            startOver();
        } else {
            drawPause();
            pause();
        }

    }

    private void playerGoWithString(String move) {
        boolean memo = player.move(moveString, portalA, portalB);
        if (memo) {
            saveGame(this);
        }
    }

    private void startOver() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (Integer.valueOf(c) == 27) {
                    System.exit(0);
                }
                switch (c) {
                    case 'N':
                        playWithKeyboard();
                        break;
                    case 'n':
                        playWithKeyboard();
                        break;
                    case 'q':
                        break;
                    case 'Q':
                        break;
                    default: break;
                }
            }
        }
    }

    private void pause() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                switch (c) {
                    case 'g':
                        playerGo();
                        break;
                    case 'q':
                        Main.saveGame(this);
                        System.exit(0);
                        break;
                    default: break;
                }
                break;
            }
        }
    }

    /*
    Tasks:
    1. random number of hallways and rooms
    2. random location of those
    3. random length of hallways
    4. size of the room should be random
    5. some hallways should have turns.
    */
    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // Fill out this method to run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
        if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
            String seedString = "";
            int index = 1;
            while (index < input.length() && input.charAt(index) != 'S'
                    && input.charAt(index) != 's') {
                seedString = seedString + Character.toString(input.charAt(index));
                index += 1;
            }

            if (index + 1 < input.length()) {
                moveString = input.substring(index + 1, input.length());
            }

            seed = Long.parseLong(seedString);
            WORLD = new TETile[WIDTH][HEIGHT];
            RANDOM = new Random(seed);
            putBack();
            generateRandomWorld();
            player = new Player(RANDOM, WORLD);
            putNPC();
            lockNum = countLock();

            if (moveString.length() > 0) {
                playerGoWithString(moveString);
            }
        } else if (input.charAt(0) == 'L' || input.charAt(0) == 'l') {
            Game tmp = loadGame();
            copy(tmp);

            moveString = input.substring(1, input.length());
            playerGoWithString(moveString);
        }
        return WORLD;
    }

    private void putBack() {
        roomArrayList = new ArrayList<>();
        hallwayArrayList = new ArrayList<>();
        npcList = new ArrayList<>();
        ALTR = new ArrayList<>();
        ALTHW = new ArrayList<>();
        positionArrayList = new ArrayList<>();
    }

    private void copy(Game g) {
        this.WORLD = g.WORLD;
        this.player = g.player;
        this.RANDOM = g.RANDOM;
        this.portalA = g.portalA;
        this.portalB = g.portalB;
        this.npcList = g.npcList;
        this.lockNum = g.lockNum;
    }

    private static Game loadGame() {
        File f = new File("./gameString.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                Game loadGame = (Game) os.readObject();
                os.close();
                return loadGame;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }

        /* In the case no World has been saved yet, we return a new one. */
        return new Game();
    }

    private static void saveGame(Game g) {
        File f = new File("./gameString.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(g);
            os.close();
        }  catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}

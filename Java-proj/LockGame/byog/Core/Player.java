package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable {
    protected Position currentPos;
    private int life;
    protected TETile[][] world;
    private int keyNum;
    protected boolean hasWand;

    public Player(Random seed, TETile[][] w) {
        world = w;
        int x = seed.nextInt(world.length);
        int y = seed.nextInt(world[0].length);

        while (!world[x][y].equals(Tileset.FLOOR)) {
            x = seed.nextInt(world.length);
            y = seed.nextInt(world[0].length);
        }

        currentPos = new Position(x, y, w, Tileset.PLAYER);
        keyNum = 1;
        hasWand = false;
        life = 3;
    }

    public void walk() {
        if (hasWand) {
            currentPos.put(Tileset.FLOWER);
        } else {
            currentPos.put(Tileset.FLOOR);
        }
    }

    public int move(char dir, Portal a, Portal b) {
        int unlockNum = 0;
        walk();
        currentPos.move(dir);

        if (currentPos.getItem().equals(Tileset.KEY)) {
            keyNum += 1;
        }

        if (currentPos.checkLock()) {
            unlockNum = unlock(dir);
        }

        if (currentPos.checkPortal()) {
            if (currentPos.equals(a.POS) && !b.POS.checkWall(dir)) {
                a.teleport(this);
                currentPos.move(dir);
            } else if (!a.POS.checkWall(dir)) {
                b.teleport(this);
                currentPos.move(dir);
            } else {
                currentPos.moveBack();
            }
        }

        if (currentPos.getItem().equals(Tileset.UNLOCKED_DOOR)) {
            currentPos.move(dir);
        }

        if (currentPos.getItem().equals(Tileset.WAND)) {
            hasWand = true;
        }

        currentPos.put(Tileset.PLAYER);
        return unlockNum;
    }

    public boolean move(String dirs, Portal a, Portal b) {
        int index = 0;
        while (index < dirs.length()) {
            char dir = dirs.charAt(index);
            if (dir == ':') {
                return true;
            }
            move(dir, a, b);
            index += 1;
        }
        return false;
    }

    public int unlock(char dir) {
        if (keyNum > 0) {
            currentPos.put(Tileset.UNLOCKED_DOOR);
            keyNum -= 1;
            currentPos.move(dir);
            return 1;
        } else {
            currentPos.moveBack();
            return 0;
        }
    }

    public String getInfo() {
        return currentPos.preItem.description();
    }

    public Position getPos() {
        return currentPos;
    }

    public int getLife() {
        return life;
    }

    public int getKeyNum() {
        return keyNum;
    }

    public void changePos(Position p) {
        currentPos = p;
    }

    public void loseLife() {
        life -= 1;
    }

}

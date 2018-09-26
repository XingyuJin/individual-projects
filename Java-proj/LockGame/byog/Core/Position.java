package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;

public class Position implements Serializable {
    int xPos;
    int yPos;
    int xPre;
    int yPre;
    TETile preItem;
    TETile[][] world;

    public Position(int x, int y, TETile[][] w, TETile item) {
        xPos = x;
        yPos = y;
        world = w;
        preItem = Tileset.FLOOR;
        put(item);
    }

    public Position(int x, int y) {
        xPos = x;
        yPos = y;

    }

    public boolean equals(Position pos) {
        return xPos == pos.xPos && yPos == pos.yPos;
    }

    public void put(TETile item) {
        world[xPos][yPos] = item;
    }

    public TETile getItem() {
        return world[xPos][yPos];
    }

    public void move(char dir) {
        if (!checkWall(dir)) {
            xPre = xPos;
            yPre = yPos;
            preItem = world[xPos][yPos];
            switch (dir) {
                case 'w': yPos += 1; break;
                case 'W': yPos += 1; break;
                case 'a': xPos -= 1; break;
                case 'A': xPos -= 1; break;
                case 's': yPos -= 1; break;
                case 'S': yPos -= 1; break;
                case 'd': xPos += 1; break;
                case 'D': xPos += 1; break;
                default: return;
            }
        }
    }

    public void moveBack() {
        world[xPre][yPre] = Tileset.PLAYER;
        xPos = xPre;
        yPos = yPre;
    }

    boolean checkWall(char dir) {
        int p;
        switch (dir) {
            case 'w': p = yPos + 1; return world[xPos][p].equals(Tileset.WALL);
            case 'W': p = yPos + 1; return world[xPos][p].equals(Tileset.WALL);
            case 'a': p = xPos - 1; return world[p][yPos].equals(Tileset.WALL);
            case 'A': p = xPos - 1; return world[p][yPos].equals(Tileset.WALL);
            case 's': p = yPos - 1; return world[xPos][p].equals(Tileset.WALL);
            case 'S': p = yPos - 1; return world[xPos][p].equals(Tileset.WALL);
            case 'd': p = xPos + 1; return world[p][yPos].equals(Tileset.WALL);
            case 'D': p = xPos + 1; return world[p][yPos].equals(Tileset.WALL);
            default: return true;
        }
    }

    boolean checkLock() {
        return world[xPos][yPos].equals(Tileset.LOCKED_DOOR);
    }

    boolean checkPortal() {
        return world[xPos][yPos].equals(Tileset.PORTAL);
    }
}

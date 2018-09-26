package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Portal implements Serializable {
    final Position POS;
    private int life;
    private TETile[][] world;
    private Portal pair;

    public Portal(Random seed, TETile[][] w) {
        world = w;
        int x = seed.nextInt(world.length);
        int y = seed.nextInt(world[0].length);

        while (!world[x][y].equals(Tileset.FLOOR)) {
            x = seed.nextInt(world.length);
            y = seed.nextInt(world[0].length);
        }

        POS = new Position(x, y, w, Tileset.PORTAL);
    }

    void pairUp(Portal p) {
        pair = p;
    }

    void teleport(Player p) {
        p.changePos(pair.POS);
    }
}

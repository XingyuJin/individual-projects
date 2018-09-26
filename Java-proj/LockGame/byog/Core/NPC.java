package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class NPC extends Player implements Serializable {

    private int damage;
    private Player target;

    public NPC(Random seed, TETile[][] w, Player p) {
        super(seed, w);
        damage = 1;
        currentPos.put(Tileset.NPC);
        target = p;
    }

    @Override
    public void walk() {
        currentPos.put(Tileset.FLOOR);
    }

    public void move() {
        walk();
        int rand = (int) (4 * Math.random());
        char dir;
        switch (rand) {
            case 0: dir = 'a'; break;
            case 1: dir = 'w'; break;
            case 2: dir = 's'; break;
            case 3: dir = 'd'; break;
            default: dir = '1';
        }

        currentPos.move(dir);

        if (currentPos.equals(target.currentPos)) {
            attack(target);
        }

        if (!currentPos.getItem().equals(Tileset.FLOOR)) {
            currentPos.move(dir);
        }

        currentPos.put(Tileset.NPC);
    }

    public void attack(Player p) {
        p.loseLife();
    }
}

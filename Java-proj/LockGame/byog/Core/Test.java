package byog.Core;

import byog.TileEngine.TETile;

public class Test {
    public static void main(String[] args) {
        Game game = new Game();
        TETile[][] a;
        TETile[][] b;
        a = game.playWithInputString("n3415218040718096461ssdsddaddaad");
        System.out.println(TETile.toString(a));

        Game game2 = new Game();
        game2.playWithInputString("n3415218040718096461ssdsddaddaa:q");

        Game game3 = new Game();
        b = game3.playWithInputString("ld");
        System.out.println(TETile.toString(b));

        int w = a.length;
        int l = a[0].length;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < l; j++) {
                if (!a[i][j].equals(b[i][j])) {
                    System.out.println("Coodinates: " + i + " " + j);
                }
            }
        }
    }
}

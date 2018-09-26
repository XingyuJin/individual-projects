package byog.Core;

import edu.princeton.cs.introcs.StdDraw;
import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.ArrayList;

public class Menu implements Serializable {

    private static ArrayList<String> option = new ArrayList<>();
    private static ArrayList<String> shortCut = new ArrayList<>();
    private static boolean success;
    private int width;
    private int height;

    public Menu(String[] input, int w, int h) {
        if (input.length % 2 != 0) {
            success = false;
        } else {
            for (int i = 0; i < input.length; i += 2) {
                option.add(input[i]);
                shortCut.add(" (" + input[i + 1] + ")");
            }
            success = true;
        }
        height = h;
        width = w;

        StdDraw.setCanvasSize(width * 16, height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    public void show() {
        int midWidth = width / 2;
        int midHeight = height / 2;

        StdDraw.clear(Color.black);

        // Draw the Main menu
        Font bigFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(bigFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.text(midWidth, height * 0.75, "CS 61B: Lock Game!");

        // Draw the Main options
        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(Color.white);

        for (int i = 0; i < option.size(); i++) {
            StdDraw.text(midWidth, midHeight - 2 * i, option.get(i) + shortCut.get(i));
        }

        StdDraw.show();
    }

    public void chooseOption(String input) {
        switch (input) {
            case "N": break;
            case "L": break;
            case "Q": StdDraw.clear(Color.BLACK); break;
            default: break;
        }
    }
}

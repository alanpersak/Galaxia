package org.templegalaxia.patterns.alanpersak;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

import java.util.Random;
import org.templegalaxia.patterns.TemplePattern;

/**
 * An animation that converts a series of messages into binary waveforms that travel up the petals. If the pattern is
 * accepted I would like to invite people to submit messages to drive the animation. It would be a nice way to leave a
 * temple offering, especially if they can't make it in person.
 */
@LXCategory("Alan Persak")
public class GameOfLifePattern extends TemplePattern {

    private static final double TAU = Math.PI * 2;

    private BoundedParameter rateParam = new BoundedParameter("Period", 10, 1, 360).setDescription("Rate of transmission in bits per second.");
    private BoundedParameter angleParam = new BoundedParameter("Fade", 1, 0, TAU).setDescription("Rate of transmission in bits per second.");

    private double period;
    private double fadeAngle;
    private int numRows;
    private int numCols;
    private boolean[][] grid;
    private boolean[][] nextGrid;
    private boolean[][] lastGrid;
    private double[] angles;
    private double angle = 0;
    private double timer = 0;
    private int numPoints;
    private Random random = new Random();

    public GameOfLifePattern(LX lx) {
        super(lx);

        addParameter(rateParam);

        numCols = model.petals.size();
        numRows = model.petals.get(0).size;
        grid = new boolean[numRows][numCols];
        nextGrid = new boolean[numRows][numCols];
        lastGrid = new boolean[numRows][numCols];
        numPoints = model.points.length;
        angles = new double[numPoints];

        for (int i=0; i<numPoints; i++) {
            LXPoint point = model.points[i];
            angles[i] = angle(point);
        }

        seed(grid, 0, 0, numRows, numCols, random, 2);
    }

    int debugCounter = 0;
    int seedCounter = 0;
    int nextSeed = 100;
    int seedSize = 16;

    public void run(double deltaMs) {
        //period = rateParam.getValue();
        //fadeAngle = angleParam.getValue();
        fadeAngle = 0;

        period = 1;
        double fadeTime = .5;

        angle += deltaMs / 1000 / period * TAU;

        // Turn everything off
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.hsb(0, 0, 0);
        }

        timer += deltaMs / 1000;
        if (timer > period) {
            timer = 0;
            angle = 0;
            nextGeneration();

            if(seedCounter++ > nextSeed){
                seedCounter = 0;
                nextSeed = random.nextInt(100);
                int r = random.nextInt(numRows);
                int c = random.nextInt(numCols);
                seed(grid, r, c, seedSize, seedSize, random, 2);
            }

            /*System.out.println("next");
            for(int r=0; r<8; r++) {
                for (int c = 0; c < 8; c++) {
                    System.out.print(grid[r][c] ? "1" : "0");
                }
                System.out.print("\n");
            }*/
        }

        /*for(int r=0; r<numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int pi = petalIndexToPointIndex(r, c);
                double diffAngle = clampAngle(angle - angles[pi]);
                if (diffAngle < fadeAngle){
                    double oldBrightness = lastGrid[r][c] ? 100 : 20;
                    double newBrightness = grid[r][c] ? 100 : 20;
                    if (oldBrightness == newBrightness){
                        colors[pi] = LXColor.hsb(0, 0, newBrightness);
                    } else {
                        double brightness = Util.linearInterpolate(diffAngle, 0, oldBrightness, fadeAngle, newBrightness);
                        colors[pi] = LXColor.hsb(0, 0, brightness);
                    }
                } else {
                    double brightness = grid[r][c] ? 100 : 20;
                    colors[pi] = LXColor.hsb(0, 0, brightness);
                }

            }
        }*/

        for(int r=0; r<numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int pi = petalIndexToPointIndex(r, c);
                if (timer < fadeTime) {
                    double oldBrightness = lastGrid[r][c] ? 100 : 20;
                    double newBrightness = grid[r][c] ? 100 : 20;
                    /*if (oldBrightness == newBrightness){
                        colors[pi] = LXColor.hsb(0, 0, newBrightness);
                    } else {
                        double brightness = Util.linearInterpolate(fadeTime - timer, 0, oldBrightness, fadeTime, newBrightness);
                        colors[pi] = LXColor.hsb(0, 0, brightness);
                    }*/
                    double brightness = Util.linearInterpolate(timer, 0, oldBrightness, fadeTime, newBrightness);
                    colors[pi] = LXColor.hsb(0, 0, brightness);
                    /*if (r == 1 && c == 1 && debugCounter++ % 60 == 0){
                        System.out.println("fading " + lastGrid[r][c] + " " + grid[r][c] + " " + brightness);
                    }*/
                } else {
                    double brightness = grid[r][c] ? 100 : 20;
                    colors[pi] = LXColor.hsb(0, 0, brightness);
                    /*if (r == 1 && c == 1 && debugCounter++ % 60 == 0) {
                        System.out.println("static " + grid[r][c] + " " + brightness);
                    }*/
                }
            }
        }
    }

    private void nextGeneration() { // make it so, number one
        for(int r=0; r<numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int numNeighbors = 0;
                for (int dr = -1; dr < 2; dr++) {
                    for (int dc = -1; dc < 2; dc++) {
                        int nr = wrap(r + dr, numRows);
                        int nc = wrap(c + dc, numCols);
                        if ((nr != r || nc != c) && grid[nr][nc]) {
                            numNeighbors++;
                        }
                    }
                }
                if (grid[r][c]) {
                    if (numNeighbors < 2) {
                        nextGrid[r][c] = false;
                    } else if (numNeighbors < 4) {
                        nextGrid[r][c] = true;
                    } else {
                        nextGrid[r][c] = false;
                    }
                } else if (numNeighbors == 3) {
                    nextGrid[r][c] = true;
                } else {
                    nextGrid[r][c] = false;
                }
            }
        }
        boolean[][] temp = lastGrid;
        lastGrid = grid;
        grid = nextGrid;
        nextGrid = temp;
    }

    private static int wrap(int i, int max) {
        if (i<0) {
            return max - 1;
        }
        if (i>= max) {
            return 0;
        }
        return i;
    }

    // Returns the angle from a point to the origin;
    private static double angle(LXPoint point) {
        return Math.atan2(point.z, point.x) + Math.PI;
    }

    // Limits an angle to range [0, 2PI).
    private static double clampAngle(double angle) {
        while(angle < 0){
            angle += TAU;
        }
        while (angle >= TAU){
            angle -= TAU;
        }
        return angle;
    }

    private static void seed(boolean[][] grid, int row, int col, int numRows, int numCols, Random random, int prob){
        int maxRows = grid.length;
        int maxCols = grid[0].length;
        for(int r=row; r < row + numRows; r++) {
            for (int c = col; c < col + numCols; c++) {
                int wr = r % maxRows;
                int wc = c % maxCols;
                if (random.nextInt(prob) == 0) {
                    grid[wr][wc] = true;
                } else {
                    grid[wr][wc] = false;
                }
            }
        }
    }
}


import com.sun.org.apache.xml.internal.serialize.LineSeparator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class Main {
    private static final int X_1 = 0;
    private static final int X_2 = 100;
    private static final int WALL_LENGTH = 100;

    private static int y1;
    private static double k, b;

    private static BufferedReader bufferedReader;
    private static List<Tree> treeList;
    private static final Logger LOGGER;

    static {
        try {
            LogManager.getLogManager().readConfiguration(
                    Main.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.getMessage());
        }
        LOGGER = Logger.getLogger(Main.class.getName());
    }

    public static void main(String[] args) {
        if (args != null && args.length == 2) {
            File inputFile = new File(args[0]);
            File outputFile = new File(args[1]);

            try (Writer writer = new FileWriter(outputFile)) {
                bufferedReader = new BufferedReader(new FileReader(inputFile));
                Integer numTrees = readFirstLine();
                if (numTrees != null) {
                    treeList = getTrees(numTrees);
                    if (!treeList.isEmpty()) {
                        List<Integer> pointsList = getPoints();
                        if (!pointsList.isEmpty()) {
                            for (Integer i : pointsList) {
                                writer.write(i + LineSeparator.Windows);
                            }
                        } else {
                            writer.write("NO SIGNAL");
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        } else {
            LOGGER.log(Level.SEVERE, "Wrong arguments!");
        }
    }

    private static Integer readFirstLine() throws IOException {
        String firstLine = bufferedReader.readLine();
        if (firstLine.matches("\\d+\\s+\\d+")) {
            StringTokenizer stringTokenizer = new StringTokenizer(firstLine);
            y1 = Integer.parseInt(stringTokenizer.nextToken());
            int numTrees = Integer.parseInt(stringTokenizer.nextToken());
            if (y1 <= WALL_LENGTH) {
                LOGGER.log(Level.SEVERE, "Signal: (0;" + y1 + "). Threes: " + numTrees);
                return numTrees;
            }
        }
        LOGGER.log(Level.SEVERE, "Mistake in first row");
        return null;
    }

    private static List<Tree> getTrees(int numTrees) throws IOException {
        List<Tree> treeList = new ArrayList<>(numTrees * 2);
        for (int i = 1; i <= numTrees; i++) {
            String nextLine = bufferedReader.readLine();
            if (nextLine != null && nextLine.matches("\\d+\\s+-?\\d+\\s+\\d+")) {
                StringTokenizer stringTokenizer = new StringTokenizer(nextLine);
                int x0 = Integer.parseInt(stringTokenizer.nextToken());
                int y0 = Integer.parseInt(stringTokenizer.nextToken());
                int r = Integer.parseInt(stringTokenizer.nextToken());
                if (x0 <= X_2 && r <= X_2 / 2) {
                    treeList.add(new Tree(x0, y0, r));
                } else {
                    LOGGER.log(Level.SEVERE, "Invalid coordinates in row " + (i + 1));
                }
            } else {
                LOGGER.log(Level.SEVERE, "Wrong format in row " + (i + 1));
            }
        }
        LOGGER.log(Level.SEVERE, "There are " + treeList.size() + " trees between walls.");
        return treeList;
    }

    private static List<Integer> getPoints() {
        boolean cross;
        List<Integer> pointsList = new ArrayList<>(WALL_LENGTH * 2);
        for (int y2 = 0; y2 <= WALL_LENGTH; y2++) {
            cross = false;
            k = ((double) y2 - y1) / (X_2 - X_1);
            b = y1 - X_1 * k;
            LOGGER.log(Level.SEVERE,
                    "Point " + y2 + ": (" + X_2 + ";" + y2 + "). " +
                            "Line: y = " + k + " * x " + (b < 0 ? b : "+ " + b));
            for (Tree tree : treeList) {
                if (discriminant(tree) >= 0) {
                    cross = true;
                    break;
                }
            }
            if (!cross) {
                pointsList.add(y2);
            }
        }
        LOGGER.log(Level.SEVERE, "We have got " + pointsList.size() + " points.");
        return pointsList;
    }

    private static double discriminant(Tree tree) {
        double d = 4 * (Math.pow(k * (b - tree.getY0()) - tree.getX0(), 2) - (1 + k * k) *
                (Math.pow(tree.getX0(), 2) - Math.pow(tree.getR(), 2) + Math.pow(b - tree.getY0(), 2)));
        LOGGER.log(Level.SEVERE, "Discriminant = " + d);
        return d;
    }

    private static class Tree {
        final int x0;
        final int y0;
        final int r;

        Tree(int x0, int y0, int r) {
            this.x0 = x0;
            this.y0 = y0;
            this.r = r;
        }

        int getX0() {
            return x0;
        }

        int getY0() {
            return y0;
        }

        int getR() {
            return r;
        }
    }
}
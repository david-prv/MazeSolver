package com.davdprv.mazeSolver.commands;

import com.davdprv.mazeSolver.utils.SHA256;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class MazeSolveCommandExecutor implements CommandExecutor {
    private final File saveFile;
    private String[][] maze;
    private ArrayList<String> byteIds;
    int anchorX, anchorZ, yLevel;

    public MazeSolveCommandExecutor(File saveFile) {
        this.saveFile = saveFile;
        this.byteIds = new ArrayList<>();
        this.maze = null;
        loadMaze();
    }

    private void loadMaze() {
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            int rows = 0;
            int cols = 0;
            while ((line = reader.readLine()) != null) {
                if (rows == 0) {
                    cols = line.split(",").length;
                }
                rows++;
            }

            BufferedReader readerAgain = new BufferedReader(new FileReader(saveFile));
            maze = new String[rows][cols];
            int row = 0;
            while ((line = readerAgain.readLine()) != null) {
                maze[row] = line.split(",");
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length != 3) {
                sender.sendMessage("Usage: /mazesolve <anchor_x> <anchor_z> <y_level>");
                return false;
            }

            try {
                anchorX = Integer.parseInt(args[0]);
                anchorZ = Integer.parseInt(args[1]);
                yLevel = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Both coordinates must be valid integers.");
                return false;
            }

            int startX = -1, startY = maze.length - 1;
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[startY][col].equals("P")) {
                    startX = col;
                    break;
                }
            }

            if (startX == -1) {
                sender.sendMessage("No valid starting point found in the bottom row.");
                return false;
            }

            int endX = -1, endY = 0;
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[endY][col].equals("P")) {
                    endX = col;
                    break;
                }
            }

            if (endX == -1) {
                sender.sendMessage("No valid end point found in the top row.");
                return false;
            }

            String path = findPath(startX, startY, endX, endY);

            if (path != null) {
                sender.sendMessage("Path found: " + path);

                Collections.reverse(byteIds);
                Bukkit.getLogger().info("Bytes: " + String.join(" ", byteIds));
            } else {
                sender.sendMessage("No path found.");
            }

            return true;
        }
        return false;
    }

    private String findPath(int startX, int startY, int endX, int endY) {
        // Movements
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        boolean[][] visited = new boolean[maze.length][maze[0].length];
        int[][] parentX = new int[maze.length][maze[0].length];
        int[][] parentY = new int[maze.length][maze[0].length];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currX = current[0];
            int currY = current[1];

            if (currX == endX && currY == endY) {
                return reconstructPath(parentX, parentY, startX, startY, endX, endY);
            }

            for (int i = 0; i < 4; i++) {
                int newX = currX + dx[i];
                int newY = currY + dy[i];

                if (newX >= 0 && newX < maze[0].length && newY >= 0 && newY < maze.length &&
                        maze[newY][newX].equals("P") && !visited[newY][newX]) {
                    queue.add(new int[]{newX, newY});
                    visited[newY][newX] = true;
                    parentX[newY][newX] = currX;
                    parentY[newY][newX] = currY;
                }
            }
        }

        return null; // Impossible solution
    }

    private String reconstructPath(int[][] parentX, int[][] parentY, int startX, int startY, int endX, int endY) {
        StringBuilder path = new StringBuilder();
        int currentX = endX;
        int currentY = endY;

        while (currentX != startX || currentY != startY) {
            int worldX = this.calculateX(currentY, this.anchorX);
            int worldZ = this.calculateZ(currentX, maze[0].length, this.anchorZ);

            placeMarker(worldX, yLevel, worldZ);
            accumulateBytes(worldX, yLevel, worldZ);

            path.insert(0, String.format("(%d,%d) ", worldX, worldZ));
            int tempX = parentX[currentY][currentX];
            int tempY = parentY[currentY][currentX];
            currentX = tempX;
            currentY = tempY;
        }

        int worldX = this.calculateX(startY, this.anchorX);
        int worldZ = this.calculateZ(startX, maze[0].length, this.anchorZ);

        placeMarker(worldX, yLevel, worldZ);
        accumulateBytes(worldX, yLevel, worldZ);

        path.insert(0, String.format("(%d,%d) ", worldX, worldZ));

        return path.toString();
    }

    public void placeMarker(int x, int y, int z) {
            World world = Bukkit.getWorld("world");
            Location location = new Location(world, x, y + 2, z);
            Block block = world.getBlockAt(location);
            block.setType(Material.RED_WOOL);
    }

    public void accumulateBytes(int x, int y, int z) {
        World world = Bukkit.getWorld("world");
        Location location = new Location(world, x, y - 1, z);
        Block block = world.getBlockAt(location);
        String hashCode = SHA256.toHexString(SHA256.getSHA(block.getType().toString()));

        // Bukkit.getLogger().info(block.getType() + " | " + hashCode);

        byteIds.add(hashCode.substring(0, 2));
    }

    private int calculateZ(int arrayX, int mazeWidth, int anchorZ) {
        return (mazeWidth - arrayX - 1) + anchorZ;
    }

    private int calculateX(int arrayY, int anchorX) {
        return anchorX + arrayY;
    }
}

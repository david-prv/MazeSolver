package com.davdprv.mazeSolver.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MazeCommandExecutor implements CommandExecutor {
    File saveFile;

    public MazeCommandExecutor(File saveFile) {
        this.saveFile = saveFile;
        if (!this.saveFile.getParentFile().exists()) {
            this.saveFile.getParentFile().mkdirs();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 7) {
            player.sendMessage("Usage: /maze <corner_bl_x> <corner_bl_z> <corner_tr_x> <corner_tr_z> <y_level> <m> <n>");
            return false;
        }

        try {
            int cornerBlX = Integer.parseInt(args[0]);
            int cornerBlZ = Integer.parseInt(args[1]);
            int cornerTrX = Integer.parseInt(args[2]);
            int cornerTrZ = Integer.parseInt(args[3]);
            int yLevel = Integer.parseInt(args[4]);
            int m = Integer.parseInt(args[5]);
            int n = Integer.parseInt(args[6]);

            String[][] maze = scanMaze(cornerBlX, cornerBlZ, cornerTrX, cornerTrZ, yLevel, m, n);
            storeMaze(maze);

        } catch (NumberFormatException | IOException e) {
            player.sendMessage("Invalid number format or file exception.");
            return false;
        }

        return true;
    }

    private void storeMaze(String[][] maze) throws IOException {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < maze.length; i++)
        {
            for(int j = 0; j < maze.length; j++)
            {
                builder.append(maze[i][j] + "");
                if(j < maze.length - 1)
                    builder.append(",");
            }
            builder.append("\n");
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.saveFile));
        writer.write(builder.toString());
        writer.close();
    }

    private String[][] scanMaze(int cornerBlX, int cornerBlZ, int cornerTrX, int cornerTrZ, int yLevel, int m, int n) {
        String[][] maze = new String[m][n];

        for (int x = 0; x < m; x++) {
            for (int z = 0; z < n; z++) {
                int worldX = cornerTrX + x;
                int worldZ = cornerTrZ + z;

                Location loc = new Location(Bukkit.getWorld("world"), worldX, yLevel, worldZ);
                Material blockType = loc.getBlock().getType();

                // Bukkit.getLogger().info("Block hash: " + loc.getBlock().hashCode());
                // Bukkit.getLogger().info("Scanning: X=" + worldX + ", Z=" + worldZ);

                if (blockType == Material.STONE) {
                    maze[x][n-z-1] = "W"; // Wall
                } else if (blockType == Material.AIR) {
                    maze[x][n-z-1] = "P"; // Path
                } else {
                    maze[x][n-z-1] = " "; // Unknown
                }
            }
        }

        return maze;
    }
}

package com.davdprv.mazeSolver;

import com.davdprv.mazeSolver.commands.MazeCommandExecutor;
import com.davdprv.mazeSolver.commands.MazeInspectCommandExecutor;
import com.davdprv.mazeSolver.commands.MazeSolveCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MazeSolver extends JavaPlugin {
    @Override
    public void onEnable() {
        String serverPath = "C:\\Users\\David\\Desktop\\archive"
                .replaceAll("\\\\", "/");
        File pluginPath = new File(serverPath, this.getDataFolder().toString());
        File saveFile = new File(pluginPath, "scanned_maze.txt");

        MazeCommandExecutor hMaze = new MazeCommandExecutor(saveFile);
        MazeSolveCommandExecutor hMazeSolve = new MazeSolveCommandExecutor(saveFile);
        MazeInspectCommandExecutor hMazeInspect = new MazeInspectCommandExecutor();

        this.getCommand("maze").setExecutor(hMaze);
        this.getCommand("mazesolve").setExecutor(hMazeSolve);
        this.getCommand("mazeinspect").setExecutor(hMazeInspect);

        Bukkit.getLogger().info("Maze Plugin enabled!");
        Bukkit.getLogger().info("Save File: " + saveFile.toString());
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Maze Plugin disabled!");
    }
}

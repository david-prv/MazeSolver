package com.davdprv.mazeSolver.commands;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class MazeInspectExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 3) {
            player.sendMessage("Usage: /mazeinspect <block_x> <block_y> <block_z>");
            return true;
        }

        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            World world = player.getWorld();
            Block block = world.getBlockAt(x, y, z);

            player.sendMessage("Block Information:");
            inspectBlock(player, block);

        } catch (NumberFormatException e) {
            player.sendMessage("Coordinates must be integers.");
        } catch (Exception e) {
            player.sendMessage("An error occurred while inspecting the block.");
            e.printStackTrace();
        }

        return true;
    }

    private void inspectBlock(Player player, Block block) {
        Class<?> blockClass = block.getClass();

        for (Method method : blockClass.getMethods()) {
            if (isGetter(method)) {
                try {
                    Object value = method.invoke(block);
                    String attributeName = method.getName().substring(3); // remove prefix
                    player.sendMessage(attributeName + ": " + (value != null ? value.toString() : "null"));
                } catch (Exception e) {
                    player.sendMessage("Failed to retrieve value for " + method.getName());
                }
            }
        }
    }

    private boolean isGetter(Method method) {
        if (method.getName().startsWith("get") && method.getParameterCount() == 0 && !method.getReturnType().equals(Void.TYPE)) {
            return true;
        }
        return false;
    }
}
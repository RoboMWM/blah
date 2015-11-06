package me.robomwm.blah;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import me.ryanhamshire.GriefPrevention.DataStore;

import static org.bukkit.Bukkit.getServer;

public class ChatListener implements Listener
{
    GriefPrevention gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
    DataStore ds = gp.dataStore;
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void commandPreprocess (PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        String [] args = message.split(" ");
        String command = args[0].toLowerCase();

        //If a player is softmuted in GriefPrevention

        if (ds.isSoftMuted((event.getPlayer().getUniqueId())))
        {
            //If the command is a whisper command and contains a recipient and message...

            if ((gp.config_eavesdrop_whisperCommands.contains(command) || command.equals("/minecraft:tell")) && args.length > 2)
            {
                //No need to do anything if recipient player is not online

                if (getServer().getPlayerExact(args[1]) != null)
                {
                    Player targetPlayer = getServer().getPlayerExact(args[1]);

                    //No need to do anything if recipient is sender or if recipient is softmuted as well.

                    if (targetPlayer != event.getPlayer() && !ds.isSoftMuted(targetPlayer.getUniqueId()))
                    {
                        String pm = ":";
                        for (int i = 2; i < args.length; i++)
                            pm += (" " + args[i]);
                        //If you know a better way to do the formatting below, please let me know, thanks :)
                        event.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You whisper to " + getServer().getPlayer(args[1]).getName() + pm);
                        event.setCancelled(true);
                    }
                    else
                        return;

                }
                else
                    return;
            }
            else if (command.equals("/me") || command.equals("/minecraft:me"))
            {
                if (args.length > 1)
                {
                    String pm = "";
                    for (int i = 1; i < args.length; i++)
                        pm += (" " + args[i]);
                    event.getPlayer().sendMessage("* " + event.getPlayer().getName() + pm);
                    event.setCancelled(true);
                }
            }
        }
    }
}

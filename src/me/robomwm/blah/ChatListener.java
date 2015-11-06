package me.robomwm.blah;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
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
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        String [] args = message.split(" ");
        String command = args[0].toLowerCase();
        if (ds.isSoftMuted((event.getPlayer().getUniqueId())))
        {
            if (command.equals("/tell") || command.equals("/msg") || command.equals("/w") || command.equals("/t") || command.equals("/pm"))
            {
                {
                    if (args.length > 2)
                    {
                        if (getServer().getPlayerExact(args[1]) != null && getServer().getPlayerExact(args[1]) != event.getPlayer())
                        {
                            String pm = ":";
                            for (int i = 2; i < args.length; i++)
                                pm += (" " + args[i]);
                            //If you know a better way to do the formatting below, please let me know, thanks :)
                            event.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You whisper to " + getServer().getPlayer(args[1]).getName() + pm);
                            event.setCancelled(true);
                        }
                    }
                }
            }
            else if (command.equals("/me"))
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

package me.robomwm.blah;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import me.ryanhamshire.GriefPrevention.DataStore;

public class ChatListener implements Listener
{
    GriefPrevention gp;
    DataStore ds = gp.dataStore;
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        String [] args = message.split(" ");
        String command = args[0].toLowerCase();
        if (ds.isSoftMuted((event.getPlayer().getUniqueId())))
        {
            if (command.equals("/me") || command.equals("/tell") || command.equals("/msg") || command.equals("/w") || command.equals("/t") || command.equals("/pm"))
            {
                event.setCancelled(true);
            }
        }

    }

}

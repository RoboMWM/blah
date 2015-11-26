package me.robomwm.blah;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import me.ryanhamshire.GriefPrevention.DataStore;

import static org.bukkit.Bukkit.getServer;

public class CommandListener implements Listener
{
    GriefPrevention gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
    DataStore ds = gp.dataStore;
    private boolean didIUnCancel = false;
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        //There's no reason to do anything if we didn't uncancel the event since now GP cancels everything we check for
        if (!didIUnCancel)
            return;

        String message = event.getMessage();
        String [] args = message.split(" ");
        String command = args[0].toLowerCase();
        Player sender = event.getPlayer();

        //If it's a whisper (with a recipient and message)
        if ((gp.config_eavesdrop_whisperCommands.contains(command) || command.equals("/minecraft:tell")) && args.length > 2)
        {
            //Always try ignore first (overrides softmute)
            if (trySoftIgnore(sender, args))
            {}
            //Check if sender is softmuted in GriefPrevention
            else if (ds.isSoftMuted((sender.getUniqueId())))
            {
                trySoftMessage(sender, args);
            }
        }
        //If not a whisper, check if it's a /me command
        else if (command.equals("/me") || command.equals("/minecraft:me"))
        {
            if (ds.isSoftMuted((sender.getUniqueId())))
            {
                softMe(sender, args);
            }
            //TODO: include ignores if not expensive/stupid
            //for loop of online players, checking to see if sender isIgnored
        }

        event.setCancelled(true); //reset event status
    }

    //Uncancels GriefPrevention's simple cancelling of, well everything
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void unCancelPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
        {
            didIUnCancel = true;
            event.setCancelled(false);
        }
        else
            didIUnCancel = false;
    }

    // ------------ Methods ---------------- //

    // ------------ Methods that send messages to the sender ---------------- //

    public void sendSoftMessage(Player sender, String[] args, Player target)
    {
        String pm = ":";
        for (int i = 2; i < args.length; i++)
            pm += (" " + args[i]);
        //If you know a better way to do the formatting below, please let me know, thanks :)
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You whisper to " + target.getName() + pm);
    }
    public void softMe(Player sender, String[] args)
    {
        String pm = "";
        for (int i = 1; i < args.length; i++)
            pm += (" " + args[i]);
        sender.sendMessage("* " + sender.getName() + pm);
        //TODO: logic to getonline players, then ask GP if they're softmuted in a for loop. If yes, send message.
        //Would this be expensive on larger servers? Or I could just not care and not do this, since they're muted for a reason.
    }

    // ------------ Method that checks if player target is ignoring sender (or vice versa) ---------------- //

    //Basically a copy of
    //https://github.com/ryanhamshire/GriefPrevention/blob/7067db624de85e153488cbe41afbcc1c8e948754/src/me/ryanhamshire/GriefPrevention/PlayerEventHandler.java#L532
    //As he does not have this as a separate method to API into (would be nice if there was).
    public int isIgnored(Player sender, Player target)
    {
        PlayerData playerData = ds.getPlayerData(target.getPlayer().getUniqueId());
        if (playerData.ignoredPlayers.containsKey(sender.getUniqueId()))
            return 1;
        playerData = ds.getPlayerData(sender.getPlayer().getUniqueId());
        if (playerData.ignoredPlayers.containsKey(target.getUniqueId()))
        {

            return 2;
        }
        else
            return 0;
    }

    // ------------ Methods that execute the above methods accordingly, and return success or failure ---------------- //

    //Checks if a player qualifies to receive a soft message.
    public boolean trySoftMessage(Player sender, String[] args)
    {
        Player target;
        //Check if recipient is online
        if (getServer().getPlayerExact(args[1]) != null)
        {
            target = getServer().getPlayerExact(args[1]);
        }
        //Otherwise we don't care
        else
            return false;
        //We don't care if sender is also the recipient
        //or if receiver is also softmuted
        if ((sender != target) && !ds.isSoftMuted(target.getUniqueId()))
        {
            sendSoftMessage(sender, args, target);
            return true;
        }
        else
            return false;
    }
    public boolean trySoftIgnore(Player sender, String[] args)
    {
        Player target;
        //Check if recipient is online
        if (getServer().getPlayerExact(args[1]) != null) {
            target = getServer().getPlayerExact(args[1]);
        }
        //Otherwise we don't care
        else
            return false;

        //First check if either player is ignoring the other
        int ignoring = isIgnored(sender, target);
        if (ignoring == 1)
        {
            sendSoftMessage(sender, args, target);
            return true;
        }
        //if player is ignoring their recipient
        else if (ignoring == 2)
        {
            //Tell them they need to unignore them first
            sender.sendMessage(ChatColor.RED + "You need to " + ChatColor.GOLD + "/unignore " + target.getName() + ChatColor.RED + " to send them a whisper.");
            return true;
        }
        else
            return false;
    }
}

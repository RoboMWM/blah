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
    private boolean shouldICheck = true;
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (shouldICheck)
        {
            String message = event.getMessage();
            String [] args = message.split(" ");
            String command = args[0].toLowerCase();
            Player sender = event.getPlayer();

            //If the command is not a whisper, check if it is a /me command
            if (gp.config_eavesdrop_whisperCommands.contains(command) || command.equals("/minecraft:tell") && args.length > 2)
            {
                if (trySoftMessage(sender, args))
                    event.setCancelled(true);
            }

            //Me command softmute, only if event is not cancelled and sender is softmuted
            else if (ds.isSoftMuted((sender.getUniqueId())))
            {
                if (command.equals("/me") || command.equals("/minecraft:me") && args.length > 1)
                {
                    softMe(sender, args);
                    event.setCancelled(true);
                }
            }
        }

    }

    //Checks if something other than GriefPrevention cancelled the event beforehand.
    //e.g. A player is muted in another plugin, blocking /plugin:command syntax, etc.
    //This is to workaround GriefPrevention's simple cancelling of /tell and /me
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess2(PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
            shouldICheck = false;
        else
            shouldICheck = true;
    }
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
        //Would this be expensive on larger servers?
    }
    //Basically a copy of
    //https://github.com/ryanhamshire/GriefPrevention/blob/7067db624de85e153488cbe41afbcc1c8e948754/src/me/ryanhamshire/GriefPrevention/PlayerEventHandler.java#L532
    //As he does not have this as a separate method to API into (would be nice if there was).
    public boolean isIgnored(Player sender, Player target)
    {
        PlayerData playerData = ds.getPlayerData(target.getPlayer().getUniqueId());
        if (playerData.ignoredPlayers.containsKey(sender.getUniqueId()))
            return true;
        playerData = ds.getPlayerData(sender.getPlayer().getUniqueId());
        if (playerData.ignoredPlayers.containsKey(target.getUniqueId()))
        {
            //Send info message if player is ignoring their recipient
            sender.sendMessage(ChatColor.RED + "You need to " + ChatColor.GOLD + "/unignore " + sender.getName() +
            ChatColor.RED + " to send them a whisper.");
            return false;
        }
        else
            return false;
    }

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

        //First check if either player is ignoring the other
        if (isIgnored(sender, target))
        {
            sendSoftMessage(sender, args, target);
            return true;
        }

        //Otherwise see if sender is softmuted in GriefPrevention (if event is not cancelled)
        if (ds.isSoftMuted((sender.getUniqueId())))
        {
            //We don't care if sender is also the recipient
            //or if both sender and receiver are softmuted
            if (target != sender && !ds.isSoftMuted(target.getUniqueId()))
            {
                sendSoftMessage(sender, args, target);
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
}

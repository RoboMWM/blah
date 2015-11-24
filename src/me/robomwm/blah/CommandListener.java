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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        String [] args = message.split(" ");
        //We don't care if it's just a command without arguments
        if (args.length < 2)
            return;
        String command = args[0].toLowerCase();

        //Check if the command is a whisper
        if ((gp.config_eavesdrop_whisperCommands.contains(command) || command.equals("/minecraft:tell")) && args.length > 2)
        {
            Player sender = event.getPlayer();
            //if we uncancelled (and nothing else cancelled it),
            if (didIUnCancel)
            {
                trySoftIgnore(sender, args); //try the softIgnore feature,
                event.setCancelled(true); //reset event status,
                return; //and get out
            }

            //Otherwise check if sender is softmuted in GriefPrevention
            else if (ds.isSoftMuted((sender.getUniqueId())))
            {
                if (trySoftMessage(sender, args))
                    event.setCancelled(true);
            }
        }

        //If command is not a whisper and we uncancelled it
        else if (didIUnCancel)
        {
            event.setCancelled(true); //set it back to cancelled,
            return; //and get out.
        }

        //Otherwise, check if it's a /me command
        else if ((command.equals("/me") || command.equals("/minecraft:me")) && args.length > 1)
        {
            Player sender = event.getPlayer();
            if (ds.isSoftMuted((sender.getUniqueId())))
            {
                softMe(sender, args);
                event.setCancelled(true);
            }
            //TODO: include ignores if not expensive/stupid
            //for loop of online players, checking to see if sender isIgnored
        }
    }

    //Uncancels GriefPrevention's simple cancelling of /tell when either
    //player is ignoring the other, allowing us to trySoftIgnore
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
            sender.sendMessage(ChatColor.RED + "You need to " + ChatColor.GOLD + "/unignore " + target.getName() +
            ChatColor.RED + " to send them a whisper.");
            return false; //BEWARE TO COPY-PASTERS (i.e. myself, of my own code of course): Returns false so trySoftIgnore() doesn't sendSoftMessage().
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
        if (isIgnored(sender, target))
        {
            sendSoftMessage(sender, args, target);
            return true;
        }
        else
            return false;
    }
}

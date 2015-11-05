package me.robomwm.blah;

import org.bukkit.plugin.java.JavaPlugin;
public class Main extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
    }

    @Override
    public void onDisable()
    {}
}

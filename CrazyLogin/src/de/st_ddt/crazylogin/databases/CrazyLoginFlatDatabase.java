package de.st_ddt.crazylogin.databases;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyutil.databases.FlatPlayerDataDatabase;

public final class CrazyLoginFlatDatabase extends FlatPlayerDataDatabase<LoginPlayerData>
{

	public CrazyLoginFlatDatabase(final JavaPlugin plugin, final ConfigurationSection config)
	{
		super(LoginPlayerData.class, new String[] { "name", "password", "ips", "lastAction" }, "accounts.db", plugin, config);
	}

	public CrazyLoginFlatDatabase(final JavaPlugin plugin, final String path)
	{
		super(LoginPlayerData.class, new String[] { "name", "password", "ips", "lastAction" }, plugin, path);
	}
}

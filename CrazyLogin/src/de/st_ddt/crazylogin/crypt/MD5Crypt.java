package de.st_ddt.crazylogin.crypt;

import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public class MD5Crypt extends DefaultCrypt
{

	public MD5Crypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config) throws NoSuchAlgorithmException
	{
		super(plugin, config);
	}

	public MD5Crypt(final LoginPlugin<? extends LoginData> plugin, final String[] args) throws CrazyException
	{
		super(plugin, args);
	}

	@Override
	public String getAlgorithm()
	{
		return "MD5";
	}
}

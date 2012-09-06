package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.crypt.CrazyCrypt1;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyplugin.listener.CrazyPluginMessageListener;

public class CrazyLoginMessageListener extends CrazyPluginMessageListener
{

	protected final CrazyLogin plugin;
	protected final CrazyCrypt1 encryptor = new CrazyCrypt1();

	public CrazyLoginMessageListener(final CrazyLogin plugin)
	{
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	protected void pluginMessageQuerryRecieved(final String channel, final Player player, final String header, final String args)
	{
		if (channel.equals(plugin.getName()))
		{
			if (header.equals("sAuth"))
			{
				sendPluginMessage(player, "A_sAuth " + encryptID(player));
				return;
			}
			else if (header.equals("PlrIP"))
			{
				sendPluginMessage(player, "A_PlrIP " + player.getAddress().getAddress().getHostAddress());
				return;
			}
			if (header.equals("State"))
			{
				sendPluginMessage(player, "A_State " + (plugin.hasPlayerData(player) ? "1" : "0") + " " + (plugin.isLoggedIn(player) ? "1" : "0"));
				return;
			}
			if (header.startsWith("Login"))
			{
				if (!plugin.hasPlayerData(player))
				{
					sendPluginMessage(channel, player, "A_Login NOACCOUNT");
					return;
				}
				try
				{
					if (!player.hasPermission("crazylogin.login.channel"))
						throw new CrazyCommandPermissionException();
					plugin.playerLogin(player, args);
				}
				catch (final CrazyException e)
				{
					e.print(player, plugin.getChatHeader());
					sendPluginMessage(channel, player, "A_Login ERROR");
					return;
				}
				sendPluginMessage(channel, player, "A_Login " + (plugin.isLoggedIn(player) ? "true" : "false"));
				return;
			}
			if (header.startsWith("ChgPW "))
			{
				if (plugin.hasPlayerData(player))
					if (!plugin.isLoggedIn(player))
					{
						sendPluginMessage(channel, player, "A_ChgPW LOGIN");
						return;
					}
				try
				{
					if (!plugin.hasPlayerData(player))
						if (!player.hasPermission("crazylogin.register.channel"))
							throw new CrazyCommandPermissionException();
					plugin.playerPassword(player, args);
				}
				catch (final CrazyException e)
				{
					e.print(player, plugin.getChatHeader());
					sendPluginMessage(channel, player, "A_ChgPW ERROR");
					return;
				}
				sendPluginMessage(channel, player, "A_ChgPW true");
				return;
			}
			else if (header.equals("Logout"))
			{
				try
				{
					if (!player.hasPermission("crazylogin.logout.channel"))
						throw new CrazyCommandPermissionException();
					plugin.playerLogout(player);
				}
				catch (final CrazyException e)
				{
					e.print(player, plugin.getChatHeader());
					sendPluginMessage(channel, player, "A_Logout ERROR");
					return;
				}
				sendPluginMessage(channel, player, "A_Logout true");
				return;
			}
		}
	}

	@Override
	protected void pluginMessageAnswerRecieved(final String channel, final Player player, final String header, final String args)
	{
	}

	@Override
	protected void pluginMessageRawRecieved(final String channel, final Player player, final String header, final byte[] bytes)
	{
	}

	protected String encryptID(final Player player)
	{
		try
		{
			return encryptor.encrypt(player.getName() + " " + player.getAddress().getAddress().getHostAddress(), null, plugin.getUniqueIDKey());
		}
		catch (final Exception e)
		{
			return "";
		}
	}
}

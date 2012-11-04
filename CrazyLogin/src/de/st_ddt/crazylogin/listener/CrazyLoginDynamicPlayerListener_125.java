package de.st_ddt.crazylogin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChatEvent;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;

@SuppressWarnings("deprecation")
public class CrazyLoginDynamicPlayerListener_125 extends CrazyLoginDynamicPlayerListener_132
{

	public CrazyLoginDynamicPlayerListener_125(CrazyLogin plugin, CrazyLoginPlayerListener playerListener)
	{
		super(plugin, playerListener);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerChat(final PlayerChatEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.hasPlayerData(player))
		{
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (playerdata != null)
				if (playerdata.isLoggedIn())
				{
					playerdata.notifyAction();
					plugin.getCrazyDatabase().save(playerdata);
					return;
				}
		}
		else if (!plugin.isBlockingGuestChatEnabled())
			return;
		plugin.getCrazyLogger().log("ChatBlocked", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to execute", event.getMessage());
		event.setCancelled(true);
		plugin.requestLogin(event.getPlayer());
	}
}

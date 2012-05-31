package de.st_ddt.crazylogin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;

public class CrazyLoginCrazyListener implements Listener
{

	protected final CrazyLogin plugin;

	public CrazyLoginCrazyListener(CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	public LoginPlugin getPlugin()
	{
		return plugin;
	}

	@EventHandler
	public void CrazyPlayerRemoveEvent(CrazyPlayerRemoveEvent event)
	{
		if (plugin.deletePlayerData(event.getPlayer()))
			event.markDeletion(plugin);
	}
}

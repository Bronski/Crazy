package de.st_ddt.crazysquads.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.st_ddt.crazychats.CrazyChats;
import de.st_ddt.crazychats.channels.SquadChannel;
import de.st_ddt.crazychats.data.ChatPlayerData;
import de.st_ddt.crazysquads.CrazySquads;
import de.st_ddt.crazysquads.data.Squad;
import de.st_ddt.crazysquads.events.CrazySquadsSquadCreateEvent;
import de.st_ddt.crazysquads.events.CrazySquadsSquadJoinEvent;
import de.st_ddt.crazysquads.events.CrazySquadsSquadLeaveEvent;

public class CrazySquadsCrazyChatsListener implements Listener
{

	private final CrazySquads plugin;
	private final CrazyChats chatsPlugin;
	private final Map<Squad, SquadChannel> chats = new HashMap<Squad, SquadChannel>();

	public CrazySquadsCrazyChatsListener(final CrazySquads plugin)
	{
		super();
		this.plugin = plugin;
		this.chatsPlugin = CrazyChats.getPlugin();
	}

	@EventHandler
	public void SquadCreate(final CrazySquadsSquadCreateEvent event)
	{
		chats.put(event.getSquad(), new SquadChannel(plugin, event.getSquad(), event.getMembers()));
	}

	@EventHandler
	public void SquadJoin(final CrazySquadsSquadJoinEvent event)
	{
		final ChatPlayerData data = chatsPlugin.getPlayerData(event.getNewMember());
		if (data != null)
			data.getAccessibleChannels().add(chats.get(event.getSquad()));
	}

	@EventHandler
	public void SquadLeave(final CrazySquadsSquadLeaveEvent event)
	{
		final Squad squad = event.getSquad();
		final ChatPlayerData data = chatsPlugin.getPlayerData(event.getLeftMember());
		if (data != null)
			data.getAccessibleChannels().remove(chats.get(squad));
		if (squad.getMembers().isEmpty())
			chats.remove(squad);
	}
}
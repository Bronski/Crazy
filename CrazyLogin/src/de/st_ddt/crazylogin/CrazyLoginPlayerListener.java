package de.st_ddt.crazylogin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.st_ddt.crazyutil.PairList;

public class CrazyLoginPlayerListener implements Listener
{

	private final CrazyLogin plugin;
	private final PairList<String, PlayerData> datas;
	private final PairList<Player, Location> savelogin = new PairList<Player, Location>();

	public CrazyLoginPlayerListener(CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
		this.datas = plugin.getPlayerData();
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		savelogin.setDataVia1(player, player.getLocation());
		PlayerData playerdata = datas.findDataVia1(player.getName().toLowerCase());
		if (playerdata == null)
		{
			plugin.sendLocaleMessage("REGISTER.HEADER", player);
			plugin.sendLocaleMessage("REGISTER.MESSAGE", player);
			return;
		}
		if (!playerdata.hasIP(player.getAddress().getAddress().getHostAddress()))
			playerdata.logout();
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
		int autoKick = plugin.getAutoKick();
		if (autoKick >= 60)
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("LOGIN.REQUEST")), autoKick * 20);
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		if (plugin.isAutoLogoutEnabled())
		{
			Player player = event.getPlayer();
			PlayerData playerdata = datas.findDataVia1(player.getName().toLowerCase());
			if (playerdata != null)
				playerdata.logout();
		}
	}

	@EventHandler
	public void PlayerDropItem(PlayerDropItemEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.sendLocaleMessage("LOGIN.REQUEST", event.getPlayer());
	}

	@EventHandler
	public void PlayerInteract(PlayerInteractEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.sendLocaleMessage("LOGIN.REQUEST", event.getPlayer());
	}

	@EventHandler
	public void PlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (plugin.isLoggedIn(event.getPlayer()))
			return;
		event.setCancelled(true);
		plugin.sendLocaleMessage("LOGIN.REQUEST", event.getPlayer());
	}

	@EventHandler
	public void PlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		if (savelogin.findDataVia1(player) != null)
			if (savelogin.findDataVia1(player).distance(event.getTo()) < 5)
				return;
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerTeleport(PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		if (event.getCause() == TeleportCause.PLUGIN)
			return;
		Location target = event.getTo();
		if (target.distance(target.getWorld().getSpawnLocation()) < 10)
		{
			savelogin.setDataVia1(player, event.getTo());
			return;
		}
		if (player.getBedSpawnLocation() != null)
			if (target.distance(player.getBedSpawnLocation()) < 10)
			{
				savelogin.setDataVia1(player, event.getTo());
				return;
			}
		event.setCancelled(true);
		plugin.sendLocaleMessage("LOGIN.REQUEST", event.getPlayer());
	}

	@EventHandler
	public void PlayerDamage(EntityDamageByBlockEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getBedSpawnLocation();
		if (location == null)
			location = player.getWorld().getSpawnLocation();
		player.teleport(location, TeleportCause.PLUGIN);
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (plugin.isLoggedIn(player))
			return;
		Location location = player.getBedSpawnLocation();
		if (location == null)
			location = player.getWorld().getSpawnLocation();
		player.teleport(location, TeleportCause.PLUGIN);
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerPreCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		String message = event.getMessage().toLowerCase();
		if (message.startsWith("/"))
		{
			if (message.startsWith("/login") || message.startsWith("/crazylogin password") || message.startsWith("/crazylanguage") || message.startsWith("/language"))
				return;
			event.setCancelled(true);
			plugin.sendLocaleMessage("LOGIN.REQUEST", player);
			return;
		}
	}

	@EventHandler
	public void PlayerCommand(PlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isLoggedIn(player))
			return;
		event.setCancelled(true);
		plugin.sendLocaleMessage("LOGIN.REQUEST", player);
		return;
	}
}

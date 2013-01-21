package de.st_ddt.crazylogin.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.metadata.Authenticated;
import de.st_ddt.crazylogin.tasks.ScheduledKickTask;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.PlayerSaver;
import de.st_ddt.crazyutil.locales.Localized;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;

public class CrazyLoginPlayerListener implements Listener
{

	protected final CrazyLogin plugin;
	private final Map<String, Location> movementBlocker = new HashMap<String, Location>();
	private final Map<String, Location> savelogin = new HashMap<String, Location>();
	private final Map<String, PlayerSaver> hiddenInventory = new HashMap<String, PlayerSaver>();
	private final Map<Player, Set<Player>> hiddenPlayers = new HashMap<Player, Set<Player>>();
	private final Map<Player, String> joinMessages = new HashMap<Player, String>();
	private final Set<String> kicked = new HashSet<String>();

	public CrazyLoginPlayerListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	@Localized("CRAZYLOGIN.KICKED.BANNED.UNTIL $BannedUntil$")
	public void PlayerLoginBanCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isTempBanned(event.getAddress().getHostAddress()))
		{
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.BANNED.UNTIL", plugin.getTempBannedString(event.getAddress().getHostAddress())));
			plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of a temporary ban");
			return;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	@Localized("CRAZYLOGIN.KICKED.NAME.INVALIDCHARS")
	public void PlayerLoginNameCharCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.checkNameChars(player.getName()))
			return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NAME.INVALIDCHARS"));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid chars");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	@Localized("CRAZYLOGIN.KICKED.NAME.INVALIDCASE")
	public void PlayerLoginNameCaseCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.checkNameCase(player.getName()))
			return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NAME.INVALIDCASE"));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid name case");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	@Localized("CRAZYLOGIN.KICKED.NAME.INVALIDLENGTH $MinLength$ $MaxLength$")
	public void PlayerLoginNameLengthCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.checkNameLength(event.getPlayer().getName()))
			return;
		event.setResult(Result.KICK_OTHER);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NAME.INVALIDLENGTH", plugin.getMinNameLength(), plugin.getMaxNameLength()));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of invalid name length");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	@Localized({ "CRAZYLOGIN.KICKED.SESSION.DUPLICATE", "CRAZYLOGIN.SESSION.DUPLICATEWARN $Name$ $IP$" })
	public void PlayerLoginSessionCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isForceSingleSessionEnabled())
			if (player.isOnline())
			{
				if (plugin.isForceSingleSessionSameIPBypassEnabled())
				{
					final LoginPlayerData data = plugin.getPlayerData(player);
					if (data != null)
						if (event.getAddress().getHostAddress().equals(data.getLatestIP()))
							return;
				}
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.SESSION.DUPLICATE"));
				plugin.broadcastLocaleMessage(true, "crazylogin.warnsession", true, "SESSION.DUPLICATEWARN", player.getName(), event.getAddress().getHostAddress());
				plugin.sendLocaleMessage("SESSION.DUPLICATEWARN", player, event.getAddress().getHostAddress(), player.getName());
				plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of a player with this name being already online");
				return;
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	@Localized("CRAZYLOGIN.KICKED.CONNECTIONS.TOMUCH")
	public void PlayerLoginConnectionCheck(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		final int maxOnlinesPerIP = plugin.getMaxOnlinesPerIP();
		if (maxOnlinesPerIP != -1)
			if (plugin.getOnlinePlayersPerIP(event.getAddress().getHostAddress()).size() >= maxOnlinesPerIP)
			{
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.CONNECTIONS.TOMUCH"));
				plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of to many connections for this IP");
				return;
			}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	@Localized("CRAZYLOGIN.KICKED.NOACCOUNT")
	public void PlayerLoginDataUpdate(final PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		final LoginPlayerData data = plugin.getCrazyDatabase().updateEntry(player.getName());
		if (!plugin.isBlockingGuestJoinEnabled() || data != null)
			return;
		event.setResult(Result.KICK_WHITELIST);
		event.setKickMessage(plugin.getLocale().getLocaleMessage(player, "KICKED.NOACCOUNT"));
		plugin.getCrazyLogger().log("AccessDenied", "Denied access for player " + player.getName() + " @ " + event.getAddress().getHostAddress() + " because of he has no account!");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void PlayerJoin(final PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		if (player.hasMetadata("NPC"))
			return;
		PlayerJoin(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void PlayerJoinMessageSet(final PlayerJoinEvent event)
	{
		if (plugin.isUsingCustomJoinQuitMessagesEnabled())
			event.setJoinMessage("CRAZYLOGIN.JOIN");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	@Localized("CRAZYLOGIN.BROADCAST.JOIN $Name$")
	public void PlayerJoinMessageGet(final PlayerJoinEvent event)
	{
		final String message = event.getJoinMessage();
		if (message == null)
			return;
		final Player player = event.getPlayer();
		if (plugin.isDelayingJoinQuitMessagesEnabled() && !plugin.isLoggedIn(player))
		{
			joinMessages.put(player, message);
			event.setJoinMessage(null);
		}
		else if (message.equals("CRAZYLOGIN.JOIN"))
		{
			ChatHelper.sendMessage(Bukkit.getOnlinePlayers(), "", plugin.getLocale().getLanguageEntry("BROADCAST.JOIN"), player.getName());
			event.setJoinMessage(null);
		}
	}

	public void sendPlayerJoinMessage(final Player player)
	{
		final String message = joinMessages.remove(player);
		if (message == null)
			return;
		if (message.equals("CRAZYLOGIN.JOIN"))
			ChatHelper.sendMessage(Bukkit.getOnlinePlayers(), "", plugin.getLocale().getLanguageEntry("BROADCAST.JOIN"), player.getName());
		else
			ChatHelper.sendMessage(Bukkit.getOnlinePlayers(), "", message);
	}

	@Localized({ "CRAZYLOGIN.REGISTER.HEADER", "CRAZYLOGIN.REGISTER.HEADER2", "CRAZYLOGIN.REGISTER.REQUEST", "CRAZYLOGIN.LOGIN.REQUEST" })
	public void PlayerJoin(final Player player)
	{
		if (movementBlocker.get(player.getName().toLowerCase()) != null)
			player.teleport(movementBlocker.get(player.getName().toLowerCase()), TeleportCause.PLUGIN);
		if (plugin.isHidingPlayerEnabled())
			hidePlayer(player);
		if (plugin.hasPlayerData(player))
		{
			// Registered
			// Session active?
			final LoginPlayerData playerdata = plugin.getPlayerData(player);
			if (!playerdata.hasIP(player.getAddress().getAddress().getHostAddress()))
				playerdata.logout();
			playerdata.checkTimeOut();
			if (playerdata.isLoggedIn())
			{
				player.setMetadata("Authenticated", new Authenticated(plugin, player));
				plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server. (Verified)");
				return;
			}
			else
				plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server.");
			// Default Protection
			Location location = player.getLocation().clone();
			if (plugin.isForceSaveLoginEnabled())
			{
				triggerSaveLogin(player);
				location = player.getWorld().getSpawnLocation().clone();
			}
			if (plugin.isHidingInventoryEnabled())
				triggerHidenInventory(player);
			if (movementBlocker.get(player.getName().toLowerCase()) == null)
				movementBlocker.put(player.getName().toLowerCase(), location);
			// Message
			plugin.sendLocaleMessage("LOGIN.REQUEST", player);
			// AutoKick
			final int autoKick = plugin.getAutoKick();
			if (autoKick >= 10)
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("LOGIN.REQUEST"), plugin.getAutoTempBan()), autoKick * 20);
			plugin.registerDynamicHooks();
		}
		else
		{
			// Unregistered
			plugin.getCrazyLogger().log("Join", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " joined the server (No Account)");
			if (plugin.isAlwaysNeedPassword() || PermissionModule.hasPermission(player, "crazylogin.requirepassword"))
			{
				// Default Protection
				Location location = player.getLocation().clone();
				if (plugin.isForceSaveLoginEnabled())
				{
					triggerSaveLogin(player);
					location = player.getWorld().getSpawnLocation().clone();
				}
				if (plugin.isHidingInventoryEnabled())
					triggerHidenInventory(player);
				if (movementBlocker.get(player.getName().toLowerCase()) == null)
					movementBlocker.put(player.getName().toLowerCase(), location);
				// Message
				plugin.sendLocaleMessage("REGISTER.HEADER", player);
			}
			else if (!plugin.isAvoidingSpammedRegisterRequestsEnabled() || new Date().getTime() - player.getFirstPlayed() < 60000)
				plugin.sendLocaleMessage("REGISTER.HEADER2", player);
			// AutoKick
			final int autoKick = plugin.getAutoKickUnregistered();
			if (autoKick != -1)
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ScheduledKickTask(player, plugin.getLocale().getLanguageEntry("REGISTER.REQUEST"), true), autoKick * 20);
			plugin.registerDynamicHooks();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerQuit(final PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		if (player.hasMetadata("NPC"))
			return;
		if (kicked.remove(event.getPlayer().getName()))
			return;
		if (plugin.isUsingCustomJoinQuitMessagesEnabled())
			event.setQuitMessage("CRAZYLOGIN.QUIT");
		if (!plugin.isLoggedIn(player) && plugin.isDelayingJoinQuitMessagesEnabled())
			event.setQuitMessage(null);
		PlayerQuit(player, false);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			@Override
			public void run()
			{
				plugin.unregisterDynamicHooks();
			}
		}, 5);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	@Localized("CRAZYLOGIN.BROADCAST.QUIT $Name$")
	public void PlayerQuitMessage(final PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isUsingCustomJoinQuitMessagesEnabled())
			if (event.getQuitMessage() != null)
				if (event.getQuitMessage().equals("CRAZYLOGIN.QUIT"))
				{
					ChatHelper.sendMessage(Bukkit.getOnlinePlayers(), "", plugin.getLocale().getLanguageEntry("BROADCAST.QUIT"), player.getName());
					event.setQuitMessage(null);
				}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerKick(final PlayerKickEvent event)
	{
		final Player player = event.getPlayer();
		if (player.hasMetadata("NPC"))
			return;
		if (plugin.isUsingCustomJoinQuitMessagesEnabled())
		{
			kicked.add(event.getPlayer().getName());
			event.setLeaveMessage("CRAZYLOGIN.KICK");
		}
		if (!plugin.isLoggedIn(player) && plugin.isDelayingJoinQuitMessagesEnabled())
			event.setLeaveMessage(null);
		PlayerQuit(player, true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			@Override
			public void run()
			{
				plugin.unregisterDynamicHooks();
			}
		}, 5);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	@Localized("CRAZYLOGIN.BROADCAST.KICK $Name$")
	public void PlayerKickMessage(final PlayerKickEvent event)
	{
		final Player player = event.getPlayer();
		if (plugin.isUsingCustomJoinQuitMessagesEnabled())
			if (event.getLeaveMessage() != null)
				if (event.getLeaveMessage().equals("CRAZYLOGIN.KICK"))
				{
					ChatHelper.sendMessage(Bukkit.getOnlinePlayers(), "", plugin.getLocale().getLanguageEntry("BROADCAST.KICK"), player.getName());
					event.setLeaveMessage(null);
				}
	}

	public void PlayerQuit(final Player player, final boolean kicked)
	{
		plugin.getCrazyLogger().log("Quit", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " left the server." + (kicked ? " (Kicked)" : ""));
		disableSaveLogin(player);
		disableHidenInventory(player);
		unhidePlayerQuit(player);
		joinMessages.remove(player);
		final LoginPlayerData playerdata = plugin.getPlayerData(player);
		if (playerdata == null)
		{
			if (plugin.isRemovingGuestDataEnabled())
				new CrazyPlayerRemoveEvent(player).callEvent();
		}
		else
		{
			if (!playerdata.isLoggedIn())
				return;
			playerdata.notifyAction();
			if (plugin.isInstantAutoLogoutEnabled())
				playerdata.logout();
			plugin.getCrazyDatabase().saveWithoutPassword(playerdata);
		}
	}

	public void PlayerQuit2(final Player player)
	{
		plugin.getCrazyLogger().log("Quit", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " left the server");
		disableSaveLogin(player);
		disableHidenInventory(player);
		unhidePlayer(player);
		joinMessages.remove(player);
		final LoginPlayerData playerdata = plugin.getPlayerData(player);
		if (playerdata != null)
		{
			if (!playerdata.isLoggedIn())
				return;
			playerdata.notifyAction();
			if (plugin.isInstantAutoLogoutEnabled())
				playerdata.logout();
			plugin.getCrazyDatabase().saveWithoutPassword(playerdata);
		}
	}

	public void addToMovementBlocker(final Player player)
	{
		addToMovementBlocker(player.getName(), player.getLocation());
	}

	public void addToMovementBlocker(final String player, final Location location)
	{
		movementBlocker.put(player.toLowerCase(), location);
	}

	public boolean removeFromMovementBlocker(final OfflinePlayer player)
	{
		return removeFromMovementBlocker(player.getName());
	}

	public boolean removeFromMovementBlocker(final String player)
	{
		return movementBlocker.remove(player.toLowerCase()) != null;
	}

	public void clearMovementBlocker(final boolean guestsOnly)
	{
		if (guestsOnly)
		{
			for (final String name : movementBlocker.keySet())
				if (!plugin.hasPlayerData(name))
					movementBlocker.remove(name);
		}
		else
			movementBlocker.clear();
	}

	public Map<String, Location> getMovementBlocker()
	{
		return movementBlocker;
	}

	public void triggerSaveLogin(final Player player)
	{
		if (savelogin.get(player.getName().toLowerCase()) == null)
			savelogin.put(player.getName().toLowerCase(), player.getLocation().clone());
		player.teleport(plugin.getSaveLoginLocations(player), TeleportCause.PLUGIN);
	}

	public void disableSaveLogin(final Player player)
	{
		final Location location = savelogin.remove(player.getName().toLowerCase());
		if (location == null)
			return;
		player.teleport(location, TeleportCause.PLUGIN);
	}

	public void triggerHidenInventory(final Player player)
	{
		if (hiddenInventory.get(player.getName().toLowerCase()) == null)
		{
			final PlayerSaver saver = new PlayerSaver(player, true);
			hiddenInventory.put(player.getName().toLowerCase(), saver);
		}
	}

	public void disableHidenInventory(final Player player)
	{
		final PlayerSaver saver = hiddenInventory.remove(player.getName().toLowerCase());
		if (saver == null)
			return;
		saver.restore(player);
	}

	public boolean dropPlayerData(final String player)
	{
		return (savelogin.remove(player.toLowerCase()) != null) || (hiddenInventory.remove(player.toLowerCase()) != null);
	}

	public void hidePlayer(final Player player)
	{
		final boolean loggedIn = plugin.isLoggedIn(player);
		if (loggedIn)
			synchronized (hiddenPlayers)
			{
				if (PermissionModule.hasPermission(player, "crazylogin.bypasshidePlayer"))
					return;
				for (final Player other : Bukkit.getOnlinePlayers())
					if (player != other)
					{
						final Set<Player> hides2 = hiddenPlayers.get(other);
						if (hides2 != null)
							if (player.canSee(other))
							{
								player.hidePlayer(other);
								hides2.add(player);
							}
					}
			}
		else
			synchronized (hiddenPlayers)
			{
				final Set<Player> hides = new HashSet<Player>();
				hiddenPlayers.put(player, hides);
				for (final Player other : Bukkit.getOnlinePlayers())
					if (player != other)
					{
						if (PermissionModule.hasPermission(player, "crazylogin.bypasshidePlayer"))
							if (other.canSee(player))
							{
								other.hidePlayer(player);
								hides.add(other);
							}
						final Set<Player> hides2 = hiddenPlayers.get(other);
						if (hides2 != null)
							if (player.canSee(other))
							{
								player.hidePlayer(other);
								hides2.add(player);
							}
					}
			}
	}

	public void unhidePlayer(final Player player)
	{
		synchronized (hiddenPlayers)
		{
			final Set<Player> hides = hiddenPlayers.remove(player);
			if (hides != null)
				for (final Player other : hides)
					other.showPlayer(player);
			if (PermissionModule.hasPermission(player, "crazylogin.bypasshidePlayer"))
				for (final Entry<Player, Set<Player>> other : hiddenPlayers.entrySet())
					if (other.getValue().remove(player))
						player.showPlayer(other.getKey());
		}
	}

	public void unhidePlayerQuit(final Player player)
	{
		synchronized (hiddenPlayers)
		{
			hiddenPlayers.remove(player);
			for (final Entry<Player, Set<Player>> hides : hiddenPlayers.entrySet())
				hides.getValue().remove(player);
		}
	}
}

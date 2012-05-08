package de.st_ddt.crazyutil.geo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.SphereRegionSelector;

public class WorldEditBridge
{

	protected static WorldEditPlugin plugin;
	protected final WorldEditAPI we;

	public static WorldEditPlugin getWorldEditPlugin()
	{
		if (plugin == null)
			plugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		return plugin;
	}

	public static WorldEditBridge getWorldEditBridge()
	{
		WorldEditPlugin plugin = getWorldEditPlugin();
		if (plugin == null)
			return null;
		return new WorldEditBridge(plugin);
	}

	private WorldEditBridge(WorldEditPlugin plugin)
	{
		we = new WorldEditAPI(plugin);
	}

	public Geo getSavePlayerSelection(Player player)
	{
		try
		{
			return getPlayerSelection(player);
		}
		catch (WorldEditException e)
		{
			return null;
		}
	}

	public Geo getPlayerSelection(Player player) throws WorldEditException
	{
		LocalWorld localWorld = we.getSession(player).getSelectionWorld();
		Region region = we.getSession(player).getSelection(localWorld);
		Geo geo = null;
		World world = player.getWorld();
		if (region instanceof CuboidRegion)
		{
			CuboidRegion region2 = (CuboidRegion) region;
			Vector v1 = region2.getPos1();
			Vector v2 = region2.getPos2();
			Location location1 = new Location(world, v1.getX(), v1.getY(), v1.getZ());
			Location location2 = new Location(world, v2.getX(), v2.getY(), v2.getZ());
			geo = new Cuboid(world, location1, location2);
		}
		else if (region instanceof EllipsoidRegion)
		{
			EllipsoidRegion region2 = (EllipsoidRegion) region;
			Vector v1 = region2.getCenter();
			Vector v2 = region2.getRadius();
			Location location1 = new Location(world, v1.getX(), v1.getY(), v1.getZ());
			geo = new Sphere(location1, v2.length());
		}
		return geo;
	}

	public void setPlayerSelection(Player player, Geo geo)
	{
		BukkitWorld bukkitWorld = new BukkitWorld(geo.getWorld());
		RegionSelector selector = null;
		if (geo instanceof Cuboid)
		{
			Cuboid region = (Cuboid) geo;
			selector = new CuboidRegionSelector(bukkitWorld);
			Location location1 = region.getLocation1();
			Location location2 = region.getLocation2();
			Vector v1 = new Vector(location1.getX(), location1.getY(), location1.getZ());
			Vector v2 = new Vector(location2.getX(), location2.getY(), location2.getZ());
			selector.selectPrimary(v1);
			selector.selectSecondary(v2);
		}
		else if (geo instanceof Sphere)
		{
			Sphere region = (Sphere) geo;
			selector = new SphereRegionSelector(bukkitWorld);
			Location location1 = region.getCenter();
			Vector v1 = new Vector(location1.getX(), location1.getY(), location1.getZ());
			Vector v2 = new Vector(region.getRadius(), 0, 0);
			selector.selectPrimary(v1);
			selector.selectSecondary(v2);
		}
		we.getSession(player).setRegionSelector(bukkitWorld, selector);
	}
}
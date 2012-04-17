package de.st_ddt.crazyarena.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

import de.st_ddt.crazyarena.arenas.Arena;

public class ScoreList
{

	protected final HashMap<String, Score> scores = new HashMap<String, Score>();
	protected final Arena arena;
	protected final String[] stringnames;
	protected final String[] valuenames;
	protected boolean modded = false;

	public ScoreList(Arena arena, String[] stringnames, String[] valuenames)
	{
		super();
		this.arena = arena;
		this.stringnames = stringnames;
		this.valuenames = valuenames;
	}

	public Score addScore(String name)
	{
		Score score = new Score(name, arena, stringnames, valuenames);
		scores.put(name, score);
		return score;
	}

	public Score getScore(String name)
	{
		return scores.get(name);
	}

	public Score[] getScore()
	{
		return scores.values().toArray(new Score[0]);
	}

	public Score[] getSortedScore()
	{
		Score[] scores = getScore();
		Arrays.sort(scores);
		return scores;
	}

	public boolean hasStringColumn(String name)
	{
		for (String string : stringnames)
			if (string.equals(name))
				return true;
		return false;
	}

	public Score[] getXSortedScore(String name)
	{
		if (hasStringColumn(name))
			return getStringSortedScore(name);
		else
			return getDoubleSortedScore(name);
	}

	public Score[] getStringSortedScore(String name)
	{
		Score[] scores = getScore();
		Arrays.sort(scores, new ScoreStringSorter(name));
		return scores;
	}

	public Score[] getDoubleSortedScore(String name)
	{
		Score[] scores = getScore();
		Arrays.sort(scores, new ScoreDoubleSorter(name));
		return scores;
	}

	public String[][] getSignEntries(int anz, String sort, String[] columns)
	{
		String[][] entries = new String[anz][];
		Score[] scores = getXSortedScore(sort);
		for (int i = scores.length; i < anz; i++)
			entries[i] = new String[columns.length];
		anz = Math.min(anz, scores.length);
		for (int i = 0; i < anz; i++)
			entries[i] = scores[i].getSignRow(columns);
		return entries;
	}

	public String[][] getSignEntries(int anz, String sort, List<String> columns)
	{
		String[][] entries = new String[anz][];
		Score[] scores = getXSortedScore(sort);
		for (int i = scores.length; i < anz; i++)
			entries[i] = new String[columns.size()];
		anz = Math.min(anz, scores.length);
		for (int i = 0; i < anz; i++)
			entries[i] = scores[i].getSignRow(columns);
		return entries;
	}

	public void updateSign(Location location)
	{
		// Ausrichtung suchen
		Vector vector = null;
		if (location.clone().add(1, 0, 0).getBlock().getType() == Material.WALL_SIGN)
			vector = new Vector(1, 0, 0);
		else if (location.clone().add(-1, 0, 0).getBlock().getType() == Material.WALL_SIGN)
			vector = new Vector(-1, 0, 0);
		else if (location.clone().add(0, 1, 0).getBlock().getType() == Material.WALL_SIGN)
			vector = new Vector(0, 1, 0);
		else if (location.clone().add(0, -1, 0).getBlock().getType() == Material.WALL_SIGN)
			vector = new Vector(0, -1, 0);
		else
			return;
		// Spalten suchen
		ArrayList<String> columns = new ArrayList<String>();
		Location search = location.clone();
		int depth = checkColumn(location);
		String sort = "name";
		columns.add("name");
		while (search.add(vector).getBlock().getType() == Material.WALL_SIGN)
		{
			String[] lines = ((Sign) search.add(vector).getBlock()).getLines();
			if (lines[2].equals("sort"))
				sort = lines[3];
			columns.add(lines[3]);
			depth = Math.min(depth, checkColumn(search));
		}
		// Eintr�ge holen
		String[][] entrylist = getSignEntries(depth * 4, sort, columns);
		// Eintr�ge anzeigen
		int columnsAnz = columns.size();
		location.add(0, -1, 0);
		for (int i = 0; i < depth; i++)
		{
			search = location.clone();
			search.add(0, -i, 0);
			for (int j = 0; j < columnsAnz; j++)
			{
				Sign sign = ((Sign) search.add(vector).getBlock());
				sign.setLine(0, entrylist[i * 4][j]);
				sign.setLine(0, entrylist[i * 4 + 1][j + 1]);
				sign.setLine(0, entrylist[i * 4 + 2][j + 2]);
				sign.setLine(0, entrylist[i * 4 + 3][j + 3]);
			}
		}
	}

	private int checkColumn(Location location)
	{
		Location search = location.clone();
		int anz = -1;
		while (search.getBlock().getType() == Material.WALL_SIGN)
		{
			search.add(0, -1, 0);
			anz++;
		}
		return anz;
	}

	public void updateSignList(List<Location> locations)
	{
		for (Location location : locations)
			updateSign(location);
	}

	private class ScoreStringSorter implements Comparator<Score>
	{

		private final String sort;

		public ScoreStringSorter(String sort)
		{
			super();
			this.sort = sort;
		}

		@Override
		public int compare(Score score1, Score score2)
		{
			return score1.getString(sort).compareTo(score2.getString(sort));
		}
	}

	private class ScoreDoubleSorter implements Comparator<Score>
	{

		private final String sort;

		public ScoreDoubleSorter(String sort)
		{
			super();
			this.sort = sort;
		}

		@Override
		public int compare(Score score1, Score score2)
		{
			return score1.getValue(sort).compareTo(score2.getValue(sort));
		}
	}
}

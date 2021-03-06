package de.st_ddt.crazyarena.commands.race;

import de.st_ddt.crazyarena.CrazyArenaRace;
import de.st_ddt.crazyarena.arenas.race.RaceArena;
import de.st_ddt.crazyarena.command.ArenaPlayerCommandExecutor;

public abstract class RacePlayerCommandExecutor extends ArenaPlayerCommandExecutor<RaceArena>
{

	public RacePlayerCommandExecutor(final RaceArena arena)
	{
		super(arena);
	}

	@Override
	public CrazyArenaRace getArenaPlugin()
	{
		return CrazyArenaRace.getPlugin();
	}
}

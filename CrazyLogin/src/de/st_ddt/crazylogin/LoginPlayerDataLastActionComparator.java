package de.st_ddt.crazylogin;

public class LoginPlayerDataLastActionComparator implements LoginPlayerDataComparator
{

	@Override
	public int compare(LoginPlayerData o1, LoginPlayerData o2)
	{
		return o1.getLastActionTime().compareTo(o2.getLastActionTime());
	}
}

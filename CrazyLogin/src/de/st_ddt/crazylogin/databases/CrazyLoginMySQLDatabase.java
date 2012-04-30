package de.st_ddt.crazylogin.databases;

import de.st_ddt.crazylogin.LoginPlayerData;
import de.st_ddt.crazyutil.databases.MySQLColumn;
import de.st_ddt.crazyutil.databases.MySQLConnection;
import de.st_ddt.crazyutil.databases.MySQLDatabase;

public class CrazyLoginMySQLDatabase extends MySQLDatabase<LoginPlayerData>
{

	public CrazyLoginMySQLDatabase(MySQLConnection connection, String table, String colName, String colPassword, String colIPs, String colLastAction)
	{
		super(LoginPlayerData.class, connection, table, getColumns(colName, colPassword, colIPs, colLastAction), 0);
		checkTable();
	}

	private static MySQLColumn[] getColumns(String colName, String colPassword, String colIPs, String colLastAction)
	{
		MySQLColumn[] columns = new MySQLColumn[4];
		columns[0] = new MySQLColumn(colName, "CHAR(50)", true, false);
		columns[1] = new MySQLColumn(colPassword, "CHAR(255)", null, false, false);
		columns[2] = new MySQLColumn(colIPs, "CHAR(255)", null, false, false);
		columns[3] = new MySQLColumn(colLastAction, "TIMESTAMP", null, false, false);
		return columns;
	}
}

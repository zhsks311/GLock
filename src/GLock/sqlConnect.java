package GLock;

import java.net.InetAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

class sqlConnect {
	
	Connection con;
	Statement stmt;
	ResultSet rs;
	String url;
	String user_index;
	String dbId = "root";
	String dbPwd = "12345678";
	
	public sqlConnect() {
		// TODO Auto-generated constructor stub
		// a url which indicates server/dbname
		url = "jdbc:mysql://218.150.181.86:3306/kanglab_db";
		con = null;
		stmt = null;
		rs = null;
	}
	

	// Need to call this function when compile this program on linux
	// does not need on windows
	public void jdbcDriverLoad()
	{
		try{
			Class.forName("org.gjt.mm.mysql.Driver");
			System.out.println("jdbc driver load Successed!!!");
		} catch(ClassNotFoundException e){
			System.out.println(e.getMessage());
		}
	}
	
	
	public void connectToMysql()
	{
		try{
			
			System.out.println("try-catch test statement!");
			
			// database id and password
			con = (Connection) DriverManager.getConnection(url, dbId, dbPwd);
			System.out.println("mysql access Successed!!!");
			stmt = (Statement) con.createStatement();
			
			// query test code
//			
//			ResultSet rs = stmt.executeQuery("select * from test");
//			System.out.println("Got result : ");
//			while(rs.next()) {
//				String no = rs.getString(2);
//				String tblname = rs.getString(1);
//				System.out.println("1 : " + no + "\n");
//				System.out.println("2 : " + tblname + "\n");
//			}

		} catch(java.lang.Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void closeConnection()
	{
		try {
			stmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	// send local date to db

	public void sendLog(boolean success)
	{
		
		try {
			if(success)
				stmt.executeUpdate("insert into log (enter_time, access_check, user_index) " 
						+ "values('" + networking.getTime() + "', 1, '" + user_index + "');");
			
			else
				stmt.executeUpdate("insert into log (enter_time, access_check, user_index) " 
						+ "values('" + networking.getTime() + "', 0, '" + user_index + "');");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// send local ip to db
	public void sendIp(String id)
	{
		
		try {
			String localIp = InetAddress.getLocalHost().getHostAddress();
			stmt.executeUpdate("update users set ip_address = '" + localIp + "' where id = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void removeIp(String id)
	{
		try {
			
			stmt.executeUpdate("update user set ip_address = 'NULL' where id = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}
	
	//check user's id and password
	public boolean isJoinedUser(String id,String pwd){
		
		int result = 0;
		String query = "select count(*) from user where id ='" + id + "' and id_password = '" + pwd + "';";
		String idQuery = "select index_user from user where id = '" + id + "';";
		try {
			
			rs = stmt.executeQuery(query);
			if(rs.next())
				result = rs.getInt(1);
			
		} catch (SQLException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// if id and passwords correct then true
		if(result>0)
		{
			// Get user index
			try {
				
				rs = stmt.executeQuery(idQuery);
				
				if(rs.next())
					user_index = rs.getString(1);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		
		}
		
		// else false
		return false;
	}
	
}// class sqlConnect end;
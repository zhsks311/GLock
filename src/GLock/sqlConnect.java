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
	
	
	public sqlConnect() {
		// TODO Auto-generated constructor stub
		// a url which indicates server/dbname
		url = "jdbc:mysql://192.168.0.9:3306/test";
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
			con = (Connection) DriverManager.getConnection(url,"pi","raspberry");
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
	public void sendLog()
	{
		try {
						
			stmt.executeUpdate("insert into test (name) values('" + networking.getTime() + "');");
			
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
			stmt.executeUpdate("update user set ip = '" + localIp + "' where userId = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	
	}
	public void removeIp(String id)
	{
		try {
			
			stmt.executeUpdate("update user set ip = 'NULL' where userId = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
		
	}
	
	//check user's id and password
	public boolean isJoinedUser(String id,String pwd){
		
		int result = 0;
		String query = "select count(*) from user where userId ='" + id + "' and userPwd = '" + pwd + "';";
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
			return true;
		
		// else false
		return false;
	}
	
}// class sqlConnect end;
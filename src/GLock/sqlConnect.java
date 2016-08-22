package GLock;

import java.net.InetAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

class sqlConnect {
	
	private static sqlConnect sc = new sqlConnect(); 
	SecurityBean sb = SecurityBean.getInstance();
	Connection con;
	Statement stmt;
	ResultSet rs;
	String url;
	String dbId = "pi";
	String dbPwd = "raspberry";
	
	String user_index;
	String id;
	String pwd;
	
	private sqlConnect() {
		// TODO Auto-generated constructor stub
		// a url which indicates server/dbname
		url = "jdbc:mysql://192.168.0.13:3306/kanglab_db?useSSL=false";
		con = null;
		stmt = null;
		rs = null;
	}
	
	public static sqlConnect getInstance(){ 
		if (sc == null)
		{
			// initializing
			// jdbcDriverLoad is needed when we use this program in Linux
			sc = new sqlConnect();
			sc.jdbcDriverLoad();
		}
		return sc; 
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setPwd(String pwd){
		this.pwd = pwd;
	}
	
	public String getId(){ return id; }
	public String getPwd(){ return pwd; }

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
	
	
	public SecurityBean getSecureDate()
	{
		System.out.println(id + " id inust");

		String query = "select * from security_time where id ='" + id + "';";
		String[] split = null;
		boolean a=false;
		int divided_i = 0;
		try {

			rs = stmt.executeQuery(query);
			a = rs.next();
			System.out.println(a);
			if(a){

				// the variable i is starting from 3
				// because in my database, fields come index, id, mon_start, mon_end ...
				// and when I use jtbc, I have to start i from 1
				// I'll make Sunday 0, Monday 1 ...
				for(int i = 3; i < 16; i = i + 2) {
					
					// if i/2 == 7, it's sunday. but i wanna sunday be 0.
					// so ~
					divided_i = i/2 == 7 ? 0 : i/2;
					
					String startTime = rs.getString(i);
					String endTime = rs.getString(i+1);
					
					if(startTime.equals("0:0") && endTime.equals("0:0"))
					{
						sb.day.put(divided_i + "", false);
						System.out.println(divided_i + " : false");
					}
					else
					{
						sb.day.put(divided_i + "", true);
						System.out.println(divided_i + " : true");
					}
					
					split = startTime.split(":");
					// setsStartHour(value, array index)
					sb.setsStartHour(Integer.parseInt(split[0]), divided_i);
					sb.setsStartMin(Integer.parseInt(split[1]), divided_i);
					System.out.println(divided_i + " start Time : " + split[0] + " "+ split[1]);
					

					split = endTime.split(":");
					sb.setsEndHour(Integer.parseInt(split[0]), divided_i); 
					sb.setsEndMin(Integer.parseInt(split[1]), divided_i); 
					System.out.println(divided_i + " end Time : " + split[0] + " "+ split[1]);
					
				}
			}
			
			rs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb;
	}
	
	
	// send local date to db
	public void sendLog(boolean success)
	{
		
		try {
			
			if(success)
				stmt.executeUpdate("insert into logs (access_check, id) " 
						+ "values(1, '" + id + "');");
			
			else
				stmt.executeUpdate("insert into logs (access_check, id) " 
						+ "values(0, '" + id + "');");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// send local ip to db
	public void sendIp()
	{
		
		try {
			
			String localIp = InetAddress.getLocalHost().getHostAddress();
			stmt.executeUpdate("update users set ip_address = '" + localIp + "' where id = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void removeIp()
	{
		try {
			
			stmt.executeUpdate("update users set ip_address = 'NULL' where id = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}
	
	//check user's id and password
	public boolean isJoinedUser(){
		
		int result = 0;
		String query = "select count(*) from users where id ='" + id + "' and id_password = '" + pwd + "';";
		String idQuery = "select index_user from users where id = '" + id + "';";
		
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
	
	public void getUid()
	{
		String query = "select * from uid where id = '" + id + "';";
	
		try {
			
			rs = stmt.executeQuery(query);
			
			while(rs.next())
			{
				System.out.println(rs.getString(3));
				sb.setUid(rs.getString(3), true);
			}
			
		} catch (SQLException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	public void getDisposablePwd()
	{
		String query = "select * from temp_pw where id = '" + id + "';";
	
		try {
			
			rs = stmt.executeQuery(query);
			
			while(rs.next())
			{
				System.out.println(rs.getString(3));
				sb.tempPwd.add(rs.getString(3));
			}
			
		} catch (SQLException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	public void removeDisposablePwd(String pwd)
	{
		String query = "delete from temp_pw where temp_password = '" + pwd + "';";
	
		try {
			
			stmt.executeUpdate(query);
			
		} catch (SQLException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
}// class sqlConnect end;
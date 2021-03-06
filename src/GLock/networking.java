package GLock;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

public class networking {

	static String serverIP = "192.168.1.100"; // 127.0.0.1 & localhost
	static GLock glock = GLock.getInstance();
	static sqlConnect sc = sqlConnect.getInstance();
	static class TCPServer extends Thread {

		static String command;
		
		public void run(){
			
			sockServer();

		}
		
		public static void sockServer(){
		
		  ServerSocket serverSocket = null;
		  	  
		  Socket socket = null;
	      try {
	    	  
	          // make server socket and open socket at port 5000
	          serverSocket = new ServerSocket(8106);
	          System.out.println("[" + getTime() + "]" + " Service is ready.");
	         
	      } catch (IOException e) {
	          e.printStackTrace();
	      } // try - catch
	      
	      
	          try {
	        	  while(true){
		              // Server socket  waits for communication requests
		              // if client requests communication, server makes socket for communication
		              socket = serverSocket.accept();
		              System.out.println("[" + getTime() + "]" + socket.getInetAddress() + " Requested response");
		              
		              // Start thread for listen to client messages
		              Listener listener = new Listener(socket);
		              listener.start();
		              
	        	  }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } // try - catch
	          // close socket and stream
//              dis.close();
            try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
				
		static class Listener extends  Thread{
	        protected  Socket  socket;
	        String  command=null;        
	        SecurityBean sb = SecurityBean.getInstance();	
	        
	        public  Listener(Socket  socket)  
	        {
	                this.socket  =  socket;  
	        }

	        public  void  run()  {
	        	try  {
	        		//// receive socket's data
	        		//	InputStream in = socket.getInputStream();
	        		//	DataInputStream dis = new DataInputStream(in);  // sub stream
	        		//	              
	        		//  command = dis.readUTF();
	                		              
	        		// changed code from the upper to this
	        		// because the upper can't read data from Linux C
	        		BufferedReader  reader  =  new  BufferedReader(new  InputStreamReader(socket.getInputStream()));

	        		while ( (command = reader.readLine()) != null){
		          	              
		        			// print data from socket
		        		System.out.println("Messages from client : " + command);
		        		String[] splitter = new String[2];
		        		splitter = command.split("\\.", -1);
		        		
		        		if(command.contains("open"))
		        			glock.procDoor(true);
              
		          		 if(command.contains("uidFromRaspberry"))
		          		 {
		          			 if(sb.getUid(splitter[1]))
		          				glock.procDoor(true);
		          			 else 
		          				 glock.procDoor(false);
		          		 }
		          		 
		          		 if(command.contains("updateData"))
		          		 {
		          			 sc.connectToMysql();
		          			 sc.getLockPwd();
		          			 sc.closeConnection();
		          		 }
		          		 
		          		 if(command.contains("updateSecuredate"))
		          		 {
		          			 sc.connectToMysql();
		          			 sc.getSecureDate();
		          			 sc.closeConnection();	          			 
		          		 }
		          		 
		          		 if(command.contains("temp_pw"))
		          		 {
		          			 sc.connectToMysql();
		          			 sc.getDisposablePwd();
		          			 sc.closeConnection();
		          		 }
		          		
		          		    System.out.println("Terminating communication.");
		          		    System.out.println("[" + getTime() + "]" + " Data Received");
		          		    
	          	        
	        		} // while   

	                }  catch(IOException  ignored)  {}  
	                finally  {
	                        try  {
	                                socket.close();
	                        }  catch(IOException  ignored)  {}
	                }
	        }

		}
		
	}//server
	
	static class HeartBeat extends Thread {

		public void run(){
			sockClient();
		}
		
		public void sockClient(){
			while(true)
			{
				try{
					String localIp = networking.getLocalIp();
			        System.out.println("Trying Communication. Server IP : " + serverIP);
			        // make socket and try communication
			        Socket socket = new Socket(serverIP, 8107);
			         
			        // receiving socket's inputstream
			        OutputStream out = socket.getOutputStream();
			        DataOutputStream dos = new DataOutputStream(out);  // sub stream
			        
			        // write local ip to server
			        dos.writeUTF("_"+localIp);
			        
			        System.out.println("HeartBeat successed, and terminating communication.");
			         
			        // close socket and stream
			        dos.close();
			        out.close();
			        socket.close();
			        
			        // code for wait 9 seconds
			        Thread.sleep(9000);
			        
			    } catch (ConnectException ce) {
			        ce.printStackTrace();
			    } catch (IOException ie) {
			        ie.printStackTrace();
			    } catch (Exception e) {
			        e.printStackTrace();
			    } // try - catch
				
			}
		}

	}//heartbeat

	public static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	      return f.format(new Date());
	} // getTime
	
	public static int getDay(){
		//get information about date
		Calendar oCalendar = Calendar.getInstance( );

//      // 1     2     3     4     5     6     7
//		final String[] week = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		
		// 0 = sun, because variable - 1
		return (oCalendar.get(Calendar.DAY_OF_WEEK) - 1);			
	}

	public static void runServer(){
		TCPServer server = new TCPServer();
		server.start();
	}
	
	public static void runHeartBeat(){
		HeartBeat heartbeat = new HeartBeat();
		heartbeat.start();
	}
	
	public static int uploadFile(String sourceFileUri, String id) {

	        String fileName = sourceFileUri;
	
	        HttpURLConnection conn = null;
	        DataOutputStream dos = null;
	        String lineEnd = "\r\n";
	        String twoHyphens = "--";
	        String boundary = "*****";
	        int bytesRead, bytesAvailable, bufferSize;
	        byte[] buffer;
	        int maxBufferSize = 1 * 1024 * 1024;
	        File sourceFile = new File(sourceFileUri);
	        String portNumber = ":80";
	
	        if (!sourceFile.isFile()) {
	        
		        System.out.println("Source File not exist ");
		        return 0;
		        
	        }
	        else
	        {
	        	
		        int serverResponseCode = 0;
				try {

		        // open a URL connection to the Servlet
		        FileInputStream fileInputStream = new FileInputStream(sourceFile);
		        URL url = new URL("http://" + serverIP + portNumber + "/glock/upload.php?id=" + id);
		        
		        
		        // Open a HTTP  connection to  the URL
		        conn = (HttpURLConnection) url.openConnection();
		        conn.setDoInput(true); // Allow Inputs
		        conn.setDoOutput(true); // Allow Outputs
		        conn.setUseCaches(false); // Don't use a Cached Copy
		        conn.setRequestMethod("POST");
		        conn.setRequestProperty("Connection", "Keep-Alive");
		        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
		        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		        conn.setRequestProperty("uploaded_file", fileName);
		
		        dos = new DataOutputStream(conn.getOutputStream());
		
		        dos.writeBytes(twoHyphens + boundary + lineEnd);
		        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename="+ fileName + "" + lineEnd);
		        dos.writeBytes(lineEnd);
		
		        // create a buffer of  maximum size
		        bytesAvailable = fileInputStream.available();
		
		        bufferSize = Math.min(bytesAvailable, maxBufferSize);
		        buffer = new byte[bufferSize];
		
		        // read file and write it into form...
		        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
	        while (bytesRead > 0) {
	
		        dos.write(buffer, 0, bufferSize);
		        bytesAvailable = fileInputStream.available();
		        bufferSize = Math.min(bytesAvailable, maxBufferSize);
		        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
	        }
	
	        // send multipart form data necesssary after file data...
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
	        // Responses from the server (code and message)
	        serverResponseCode = conn.getResponseCode();
	        String serverResponseMessage = conn.getResponseMessage();
	
	        System.out.println("HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);
	
	        if(serverResponseCode == 200){
	        	System.out.println("upload successed");
	        }
	        System.out.println("upload tried");
	        //close the streams //
	        fileInputStream.close();
	        dos.flush();
	        dos.close();
	
	        } catch (MalformedURLException ex) {
	        	
	        ex.printStackTrace();	
	        System.out.println("error: " + ex.getMessage());
	        
	        } catch (Exception e) {
	
	        	e.printStackTrace();
	        	System.out.println("Exception : "+ e.getMessage());

	        }
	        
				return serverResponseCode;
	
	        } // End else block 
	}  
	

	public static void callPush(String id) {


		
		BufferedReader br;
		String body = "id=" + id;
		URL url ;
		
		try {
		url = new URL( "http://" + serverIP +":80" + "/glock/push_notification.php" );
			
		HttpURLConnection  huc = (HttpURLConnection) url.openConnection();
		 
		// request on POST method
		huc.setRequestMethod("POST");
		 
		huc.setDoInput(true);
		huc.setDoOutput(true);
		huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		OutputStream os = huc.getOutputStream();
		 
		os.write( body.getBytes("utf-8") );
		 
		os.flush();
		os.close();

		br = new BufferedReader( new InputStreamReader( huc.getInputStream(), "UTF-8" ), huc.getContentLength() );
			
		String buf;
		
		//print message got on a browser
		while( ( buf = br.readLine() ) != null ) {
		System.out.println( buf );
		}
			 
		br.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	
   
	}  


	
	static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
	
	public static String getLocalIp()
	{
		try
		{
		    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
		    {
		        NetworkInterface intf = en.nextElement();
		        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
		        {
		            InetAddress inetAddress = enumIpAddr.nextElement();
		            if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress())
		            {
		            	return inetAddress.getHostAddress().toString();
		            }
		        }
		    }
		}
		catch (Exception ex) {}
		return null;
	}
	
}


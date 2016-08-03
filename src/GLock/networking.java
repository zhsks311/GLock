package GLock;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class networking {

	static String serverIP = "218.150.181.86"; // 127.0.0.1 & localhost
	
	static class TCPconnection extends Thread {

		static String command;
		
		public void run(){
			
			sockServer();

		}
		
		public static void sockServer(){
		
		  ServerSocket serverSocket = null;
	      try {
	    	  
	          // make server socket and open socket at port 5000
	          serverSocket = new ServerSocket(8106);
	          System.out.println("[" + getTime() + "]" + " Service is ready.");
	          
	      } catch (IOException e) {
	          e.printStackTrace();
	      } // try - catch
	      
	      while (true) {
	          try {
	        	  
	              System.out.println("[" + getTime() + "]" + " Waiting for responce.");
	              
	              // Server socket  waits for communication requests
	              // if client requests communication, server makes socket for communication
	              Socket socket = serverSocket.accept();
	              System.out.println("[" + getTime() + "]" + socket.getInetAddress() + " Requested response");
	              
	              // receive socket's data
	              InputStream in = socket.getInputStream();
	              DataInputStream dis = new DataInputStream(in);  // sub stream
	              
	              command = dis.readUTF();
	              
	              // print data from socket
	              System.out.println("Messages from client : " + command);
	              
	              /*
	               * For Linux
	              if(command.equals("open"))
	            	  GLock.openDoor();
	              */
	              
	              System.out.println("Terminating communication.");
	              
	              System.out.println("[" + getTime() + "]" + " Data Received");
	               
	              // close socket and stream
	              dis.close();
	              socket.close();
	          } catch (IOException e) {
	              e.printStackTrace();
	          } // try - catch
	          
	      } // while

		}
		
		
		public void sockClient(){
			try{
				
		        System.out.println("Trying Communication. Server IP : " + serverIP);
		         
		        // make socket and try communication
		        Socket socket = new Socket(serverIP, 5000);
		         
		        // receiving socket's inputstream
		        InputStream in = socket.getInputStream();
		        DataInputStream dis = new DataInputStream(in);  // sub stream
		         
		        // print data from socket
		        System.out.println("Messages from server : " + dis.readUTF());
		        System.out.println("Terminating communication.");
		         
		        // close socket and stream
		        dis.close();
		        socket.close();
		    } catch (ConnectException ce) {
		        ce.printStackTrace();
		    } catch (IOException ie) {
		        ie.printStackTrace();
		    } catch (Exception e) {
		        e.printStackTrace();
		    } // try - catch
		}
	}

	public static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      return f.format(new Date());
	} // getTime
	
	public static int getDay(){
		//get information about date
		Calendar oCalendar = Calendar.getInstance( );

//      // 1     2     3     4     5     6     7
//		final String[] week = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		
		return (oCalendar.get(Calendar.DAY_OF_WEEK) - 1);			
	}

	public static void runServer(){
		TCPconnection server = new TCPconnection();
		server.start();
		
	}
	
	public static int uploadFile(String sourceFileUri) {

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
	        String portNumber = ":8080";
	
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
		        URL url = new URL(serverIP + portNumber + "/upload.php");
		        
		
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
	
	
	
}


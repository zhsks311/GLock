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
import java.util.Date;

public class networking {

	static class TCPconnection extends Thread {

		static String command;
		
		public void run(){
			
			sockServer();

		}
		
		public static void sockServer(){
		
		  ServerSocket serverSocket = null;
	      try {
	    	  
	          // 서버소켓을 생성하고 5000번 포트와 결합(bind) 시킨다.
	          serverSocket = new ServerSocket(5000);
	          System.out.println(getTime() + " 서버가 준비되었습니다.");
	          
	      } catch (IOException e) {
	          e.printStackTrace();
	      } // try - catch
	      
	      while (true) {
	          try {
	        	  
	              System.out.println(getTime() + " 연결요청을 기다립니다.");
	              // 서버소켓은 클라이언트의 연결요청이 올 때까지 실행을 멈추고 계속 기다린다.
	              // 클라이언트의 연결요청이 오면 클라이언트 소켓과 통신할 새로운 소켓을 생성한다.
	              Socket socket = serverSocket.accept();
	              System.out.println(getTime() + socket.getInetAddress() + " 로부터 연결요청이 들어왔습니다.");
	              
	              // 소켓의 입력스트림을 얻는다.
	              InputStream in = socket.getInputStream();
	              DataInputStream dis = new DataInputStream(in);  // 기본형 단위로 처리하는 보조스트림
	              
	              command = dis.readUTF();
	              
	              // 소켓으로 부터 받은 데이터를 출력한다.
	              System.out.println("클라이언트로부터 받은 메세지 : " + command);
	              
	              /*
	               * For Linux
	              if(command.equals("open"))
	            	  GLock.openDoor();
	              */
	              
	              System.out.println("연결을 종료합니다.");
	              
	              System.out.println(getTime() + " 데이터를 수신했습니다.");
	               
	              // 스트림과 소켓을 달아준다.
	              dis.close();
	              socket.close();
	          } catch (IOException e) {
	              e.printStackTrace();
	          } // try - catch
	          
	      } // while

		}
		
		static String getTime() {
		      SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		      return f.format(new Date());
		} // getTime

		public void sockClient(){
			try{
				String serverIP = "127.0.0.1"; // 127.0.0.1 & localhost 본인
		        System.out.println("서버에 연결중입니다. 서버 IP : " + serverIP);
		         
		        // 소켓을 생성하여 연결을 요청한다.
		        Socket socket = new Socket(serverIP, 5000);
		         
		        // 소켓의 입력스트림을 얻는다.
		        InputStream in = socket.getInputStream();
		        DataInputStream dis = new DataInputStream(in);  // 기본형 단위로 처리하는 보조스트림
		         
		        // 소켓으로 부터 받은 데이터를 출력한다.
		        System.out.println("서버로부터 받은 메세지 : " + dis.readUTF());
		        System.out.println("연결을 종료합니다.");
		         
		        // 스트림과 소켓을 닫는다.
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
	        String server = "http://218.150.181.86:3000";
	
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
		        URL url = new URL(server + "/upload.php");
		
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


import java.net.*;
import java.io.*;
import java.util.Date;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Scanner;

class Main
{
	public static void main(String[] args) throws Exception
	{
		ArrayList<String> arrlist = new ArrayList<String>();		
		int users = 0;
		char id1 = '1';
		char id2 = '2';
		Scanner scanner = new Scanner( new File("load.html") );
		String text = scanner.useDelimiter("\\A").next();
		scanner.close(); // Put this call in a finally block
		String finalResponse = new String();
		int contentLength = 0;
		// Listen for a connection from a client
		ServerSocket serverSocket = new ServerSocket(1234);
		//opens the default web browser on the desktop
		if(Desktop.isDesktopSupported())
		{
			Desktop.getDesktop().browse(new URI("http://localhost:1234"));
			Desktop.getDesktop().browse(new URI("http://localhost:1234"));

		}
		else
			System.out.println("Please direct your browser to http://localhost:1234.");
		while(true)
		{
			Socket clientSocket = serverSocket.accept();
			System.out.println("Got a connection!");
			
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// Receive the request from the client
			String inputLine;
			boolean post = false;
			
			while ((inputLine = in.readLine()) != null)
			{
				if(inputLine.length() >= 4 && inputLine.substring(0, 4).equals("POST"))
				{
					post = true;
					//System.out.println("uwu");
				}
				if(inputLine.length() >= 14 && inputLine.substring(0, 14).equals("Content-Length"))
					contentLength = Integer.parseInt(inputLine.substring(16));
				System.out.println("The client said: " + inputLine);
				if(inputLine.length() < 2)
					break;
			}
			System.out.println("contentLength: " +contentLength);
			if(post && contentLength > 0)
			{
				int client;
				int ping;
				String response = new String();
				int convertedSize;


				// Read the POST content
				char[] postedContent = new char[contentLength];
				in.read(postedContent, 0, contentLength);
				if(postedContent[0] ==  '?')
				{
					if(users == 0)
					{
						System.out.println("here");
						postedContent[0] = id1;
						++users;
					}
					else if(users == 1)
					{
						postedContent[0] = id2;
					}
				}
				client = Character.getNumericValue(postedContent[0]);
				//System.out.println("client# from array: " +postedContent[0]);
				System.out.println("client# : " +client);

				ping = Character.getNumericValue(postedContent[1]);
				if(ping == 0)
				{
					String temp = new String(postedContent);
					int arrSize = arrlist.size();
					int clientMessages = convertToInteger(postedContent, temp.indexOf('~'));
					if(clientMessages < arrSize)
					{
						String mostRecentString = arrlist.get(arrSize - 1);
						response += Integer.toString(client);
						response += "1";
						response += mostRecentString.substring(0,1);
						response += mostRecentString.substring((mostRecentString.indexOf('~') + 1));
					}
					if(clientMessages == arrSize)
					{
						response += Integer.toString(client);
						response += "0";
						response += "null";
					}
				}
				if(ping == 1)
				{
					String temp = new String(postedContent);
					arrlist.add(temp);
					int tilde = temp.indexOf('~');
					//System.out.println("Index of \'~\': " +tilde);
					//System.out.println("temp before substring: " +temp);
					temp = temp.substring(tilde + 1);
					//System.out.println("temp after substring: " +temp);
					int lengthOfMessage = temp.length();
					response += Integer.toString(client);
					response += "1";
					response += Integer.toString(client);
					//System.out.println("Response before temp: " +response);
					response += temp;
					System.out.println("Response after temp: " +response);
				}

				finalResponse = response;
				//System.out.println("Response: " +response);
			}

			String dateString = (new Date()).toGMTString();
			String payload = dateString + text;

			// Send HTTP headers
			System.out.println("Sending a response...");
			out.print("HTTP/1.1 200 OK\r\n");
			out.print("Content-Type: text/html\r\n");
			if(!post)
				out.print("Content-Length: " + Integer.toString(payload.length()) + "\r\n");
			else
				out.print("Content-Length: " + Integer.toString(finalResponse.length()) + "\r\n");
			out.print("Date: " + dateString + "\r\n");
			out.print("Last-Modified: " + dateString + "\r\n");
			out.print("Connection: close\r\n");
			out.print("\r\n");

			// Send the payload
			if(!post)
				 out.println(payload);
			else
				out.println(finalResponse);
			clientSocket.close();
		}
	}

	public static int convertToInteger(char[] ch, int tilde)
	{
		int size;
		String temp = new String(ch);
		temp = temp.substring(2, tilde);
		System.out.println("in integer, temp:" +temp);
		size = Integer.parseInt(temp);
		System.out.println("size: " +size);
		return size;
	}
}
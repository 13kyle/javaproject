// client connects to server and can upload or download a file to/from the server
// 
// to use in terminal:
//
// java Server port
//
// for default port 9999:
//
// java Server

import java.net.*;
import java.io.*;

public class Server {
	// sockets
	private static ServerSocket servSocket = null;
	private static Socket cSocket = null;

	// i/o streams
	private static DataOutputStream dos = null;
	private static DataInputStream dis = null;

	// path where file will download on server if client chooses to
	// upload a file
	private static String filePath = "/root/ServerDownloads/";
		
	// this method checks that a file exists
	public static boolean checkFile(File file) {
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		else {
			return false;
		}
	}

	// upload file to client (client chose to download from server)
	public static void uploadFile(String fileToUpload) {
		try {
			// file that will be uploaded to server
			File file = new File(fileToUpload);
			// InputStream will read the bytes from the file
			InputStream is = new FileInputStream(file);

			// holds bytes from the file to send to server
			byte[] buffer = new byte[4096];
			
		 	// send size of file to client
			long fileSize = file.length();
			dos.writeLong(fileSize);

			int count;
			// send the bytes to the server
			while ((count = is.read(buffer)) > 0) {
				dos.write(buffer, 0, count);
				dos.flush();
			}

			// close InputStream
			is.close();

			// wait for confirmation or failure message from server
			System.out.println(dis.readUTF());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// download file from client (client chose to upload to server)
	public static void download() {
		try {
			// begin by reading file name from client to edit file path
			String fName = dis.readUTF();
			// create temp filepath
			String tempFilePath = filePath + fName;

			// create file object for path that the file
			// will download to
			File file = new File(tempFilePath);
			// this stream is used to write the bytes to the file
			OutputStream os = new FileOutputStream(file);
		
			// buffer holds bytes received from client to write to file
			byte[] buffer = new byte[4096];
				
			// read in bytes and then write them to file
			int count;
			long fileSize = dis.readLong();
			long max = fileSize;
			// read in bytes from dis and write them
			// to the file with os
			while (max > 0) {
				count = dis.read(buffer);	
				os.write(buffer, 0, count);
				max -= count;
				os.flush();
			}
			
			// verify successful upload of file
			if (checkFile(file) == true) {
				System.out.println("Successful download to server.");
				dos.writeUTF("[*] From Server: Upload successful.");
				dos.flush();
			}
			else {
				System.out.println("Failed download to server.");
				dos.writeUTF("[*] From Server: Upload failed.");
				dos.flush();
			}

			// close OutputStream
			os.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String args[]) {
		// default port
		int port = 9999;

		// check for port argument
		if (args.length > 0) {
			port = Integer.valueOf(args[0]);
		}
			
		try {
			// create ServerSocket and begin listening for requests
			servSocket = new ServerSocket(port);
		} catch (IOException ioe) {
			System.out.println("Error creating server. Check port number.");
			System.exit(0);
		}

		// used for input from client
		String clientInput;
		// used for parsing the user input
		String[] parsedInput;

		while (true) {
			// second try-catch place in while so if client closes connection
			// server won't stop running
			try {
				System.out.println("************************************");
				System.out.println("Now listening on port " + port + "\n");
				cSocket = servSocket.accept();

				// create the DataOutputStream/DataInputStream
				// and chain them to the BufferedOutputStream/
				// BufferedInputStream and connect them to the
				// connected socket
				dos = new DataOutputStream(new BufferedOutputStream(cSocket.getOutputStream()));
				dis = new DataInputStream(new BufferedInputStream(cSocket.getInputStream()));

				// confirmation for server
				System.out.println("Successful connection from client.");
				// notify client about successful connection
				dos.writeUTF("[*] From Server: Connection successful.\n");
				dos.flush();

				// receive client input for upload or download options
				// if user gave incorrect input or invalid file path
				// then client will send "exit" and server will break
				// and close the streams and sockets
				
				clientInput = dis.readUTF();
				// the users input will be parsed into two arguments
				parsedInput = clientInput.split("\\s+");
				
				// if client chose to upload to server
				if (parsedInput[0].equals("u")) {
					System.out.println("Client wants to upload.");
					// call method to begin downloading file from client
					download();
				// if client chose to download from server
				} else if (parsedInput[0].equals("d")) {
					System.out.println("Client wants to download.");

					// create file object to check that it exists before downloading
					File fileTemp = new File(parsedInput[1]);
					System.out.println("\nChecking file exists...");	
					// check that file exists on server
					if (checkFile(fileTemp) == true) {
						System.out.println("File exists on server.\n");
						dos.writeUTF("[*] From server: File exists on server");
						uploadFile(parsedInput[1]);
					}
					// if file does not exist
					else {
						System.out.println("File does not exist on server.\n");
						dos.writeUTF("[*] From server: Error: File does not exist on server.");
					}
				}
				// incorrect input from client or incorrect file path
				else {
					System.out.println("Error from client.");
				}

				// close streams and client socket
				// proceed to listen for new client request
				dos.close();
				dis.close();
				cSocket.close();
				System.out.println("Client finished." + "\n");
			} catch (IOException ioe) {
				System.out.println("Client closed connection.");
			}
		}
	}
}

				















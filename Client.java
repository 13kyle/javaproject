// client connects to server and can upload or download a file to/from the server
//	
// to use in terminal:
//
// java Client host port
//
// for default localhost and port 9999:
//
// java Client
//
//
// client will be prompted with ">> " after connection to write a command
// 
// command: "u filePath" will upload filePath to Server 
// the client will then be prompted to enter the file name to be
// used on the server. the directory for the download location
// is predetermined in the server program. client just chooses the file name
//
// *** Example
// >> u /root/file.txt
// Enter file name: uploadedFile.txt
//
//
// command: "d filePath" will download filePath to client
// the client will then be prompted to enter the file name to be used
// on the client. the directory for the download location is predetermined
// in the client program. client just chooses the file name
//
// *** Example
// >> d /root/file.txt
// Enter file name: downloadedFile.txt

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
	// socket
	private static Socket cSocket = null;

	// i/o streams
	private static DataOutputStream dos = null;
	private static DataInputStream dis = null;

	// path where file will upload to server if client chooses to 
	// upload a file
	private static String filePath = "/root/ClientDownloads/";

	// used for user input
	private static Scanner sc = new Scanner(System.in);

	// help user if invalid command is written after ">> " is displayed
	// on the screen to prompt the user for a command
	public static void help() {
		System.out.println("\n***Help***");
		System.out.print("Upload file to server or download file ");
		System.out.println("from server.");
		System.out.println("\nProper usage:");
		System.out.println("java Client host port");
		System.out.println("Or for default localhost and port 9999");
		System.out.println("java Client\n");
		System.out.println("filePath -> file to upload/download");
		System.out.println("To upload: >> u filePath");
		System.out.println("To download: >> d filePath");
	}

	// this method checks that a file exists
	public static boolean checkFile(File file) {
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// this method checks that input is correct when user is prompted to 
	// enter a command after ">>" and checks that upload file exists
	public static boolean checkInput(String[] input) {
		// check for 2 strings and that the first string is
		// "u" for upload or "d" for download
		if (input.length != 2 || !(input[0].equals("u") || input[0].equals("d"))) {
			return false;
		}
		// if command is to upload file, check file exists on client side
		else if (input[0].equals("u")) {
			System.out.println("\nChecking if file exists...");
			File file = new File(input[1]);
			if (checkFile(file) == true) {
				System.out.println("File exists.");
			}
			else {
				System.out.println("File does not exist");
				return false;
			}
		}
		// if input is correct
		return true;
	}
	
	// upload file to server
	public static void uploadFile(String fileToUpload) {
		try {
			// file that will be uploaded to server
			File file = new File(fileToUpload);
			// InputStream will read the bytes from the file
			InputStream is = new FileInputStream(file);

			// holds bytes from the file to send to server
			byte[] buffer = new byte[4096];
		
			// prompt user for filename of uploaded file to use on server side
			System.out.print("Enter file name: ");
			String fName = sc.nextLine();
			dos.writeUTF(fName);
			dos.flush();
			
			// send size of file to server
			long size = file.length();
			dos.writeLong(size);

			int count;
			// send the bytes to the server	
			while ((count = is.read(buffer)) > 0) {
				dos.write(buffer, 0, count);
				dos.flush();
			}
			
			// close InputStream
			is.close();

			// wait for confirmation or failure message from server
			System.out.println("\n" + dis.readUTF());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// download file from server
	public static void download() {
		try {
			// first prompt user for file name to add to file path
			System.out.print("Enter file name: ");
			String fName = sc.nextLine();
			// edit file path for download location
			filePath = filePath + fName;

			// create file object for path that the file
			// will download to
			File file = new File(filePath);
			// this stream is used to write the bytes to the file
			OutputStream os = new FileOutputStream(file);
		
			// buffer holds bytes received from server to write to file
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
			
			System.out.println("\nChecking for successful download...");

			// verify successful upload of file
			if (checkFile(file) == true) {
				System.out.println("Successful download from server.");
				dos.writeUTF("[*] From Client: Download successful.");
				dos.flush();
			}
			else {
				System.out.println("Failed download from server.");
				dos.writeUTF("[*] From client: Download failed.");
				dos.flush();
			}
			// close OutputStream
			os.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


	public static void main(String args[]) {
		// default host and port
		String host = "localhost";
		int port = 9999;		
		
		// check for host/port arguments
		if (args.length > 1 ) {
			host = args[0];
			port = Integer.valueOf(args[1]);
		}
		
		try {
			// connect to server
			cSocket = new Socket(host, port);

			// create the DataOutputStream/DataInputStream
			// and chain them to the BufferedOutputStream/
			// BufferedInputStream and connect them to the
			// connected socket
			dos = new DataOutputStream(new BufferedOutputStream(cSocket.getOutputStream()));
			dis = new DataInputStream(new BufferedInputStream(cSocket.getInputStream()));

			// receive successful connection confirmation message
			// from server
			System.out.println(dis.readUTF());

			// this variable is used for user input after being prompted
			// for a command
			String userInput;
			// this variable is used to parse the users command
			String[] parsedInput;

			// prompt user for input
			System.out.print(">> ");
			userInput = sc.nextLine();
			// parse user input
			parsedInput = userInput.split("\\s+");

			// if the input is accurate and file exists (file
			// is only checked to exist for uploading to server)
			if (checkInput(parsedInput) == true) {
				// send userInput to server
				dos.writeUTF(userInput);
				dos.flush();

				// if user wants to upload to server
				if (parsedInput[0].equals("u")) {
					uploadFile(parsedInput[1]);
				}
				// if user wants to download from server
				// user must know and enter the file path
				// of the file on the server they want to
				// download
				else if (parsedInput[0].equals("d")) {
					// receive message from server saying
					// the file exists or doesn't exist
					// If it doesn't exist then notify user and
					// call help() and break
					String message = dis.readUTF();
					System.out.println(message);
					// if file path exists on server
					if (!(message.contains("Error"))) {
						download();
					}
				}
			}
			// if incorrect input or file path
			else {
				userInput = "exit";
				dos.writeUTF(userInput);
				dos.flush();
				System.out.println("\nError: Invalid command or invalid file path entered.");
				help();
			}
	
			// close streams and sockets
			dos.close();
			dis.close();
			cSocket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}





























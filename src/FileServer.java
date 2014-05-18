import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

	private ServerSocket serverSocket;
	private File rootDirectory;

	private FileServer(File root, int port) throws IOException {
		rootDirectory = root;
		serverSocket = new ServerSocket(port);
	}

	public void run() {
		try {
			Socket socket;

			while (true) {
				socket = serverSocket.accept();
				RequestHandler handler = new RequestHandler(socket);
				new Thread(handler).start();
			}

		} catch (IOException ex) {
			System.err.println("IOException occured.  Closing connection.");
		}
	}

	private class RequestHandler implements Runnable {

		private Socket socket;
		private BufferedReader socketIn;
		private DataOutputStream socketOut;

		public RequestHandler(Socket socket) {
			this.socket = socket;
			System.out.println("Created new thread for connection.");
		}

		@Override
		public void run() {
			try {
				socketIn = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				socketOut = new DataOutputStream(socket.getOutputStream());
				
				boolean output = true;
				while (output) {
					output = readRequest();
				}
				
			} catch (IOException e) {
				System.err
						.println("Error occured in RequestHandler run function");
			} finally {
				if (socket != null) {
					try {
						socket.close();
						System.out.println("Connection closed.");
					} catch (IOException ex) {
						System.err
								.println("IOException occured while closing connection.");
					}
				}
			}
		}

		private boolean readRequest() throws IOException {
			try {
				String name = socketIn.readLine();
				System.out.println("Received request for: " + name);

				if (name.equals("*")) {
					printAllFiles();
				} else {
					downloadFile(name);
				}
				return true;
			} catch (NullPointerException exception) {
				return false;
			}
		}

		private void downloadFile(String name) throws IOException {
			File file = new File(rootDirectory, name);
			byte[] buffer = new byte[4096];
			int read = 0, total = 0;

			// Write the length of the file (or -1 if the file does not exist)
			if (file.exists()) {
				DataInputStream fileIn = new DataInputStream(
						new FileInputStream(file));

				socketOut.writeLong(file.length());

				System.out.println("Sending " + name);
				while (total < file.length()) {
					read = fileIn.read(buffer);
					socketOut.write(buffer, 0, read);
					socketOut.flush();
					total += read;
				}
				fileIn.close();
				System.out.println("Sent " + total + " bytes.");
			} else {
				System.out.println(name + " not found.");
				socketOut.writeLong(-1);
			}
		}

		private void printAllFiles() throws IOException {
			for (File file : rootDirectory.listFiles()) {
				socketOut.writeInt(file.getName().length());
				socketOut.flush();
				socketOut.write(file.getName().getBytes());
				socketOut.flush();
			}
			socketOut.writeInt(0);
			socketOut.flush();
		}

	}

	private void close() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException ex) {
				System.err
						.println("IOException occured while closing server socket.");
			}
		}
	}

	public static void main(String[] args) {
		System.setProperty("line.separator", "\r\n");
		if (args.length != 2) {
			System.err.println("Usage: java FileServer <root> <port>");
			System.exit(1);
		}
		FileServer server = null;
		try {
			File root = new File(args[0]);
			if (!root.exists()) {
				System.err.println("Root directory does not exist: " + args[0]);
				System.exit(1);
			}
			if (!root.isDirectory()) {
				System.err.println(args[0] + " is not a directory!");
				System.exit(1);
			}
			if (!root.canRead()) {
				System.err
						.println("Root directory is not readable: " + args[0]);
				System.exit(1);
			}
			int port = Integer.parseInt(args[1]);
			server = new FileServer(root, port);
			server.run();
		} catch (NumberFormatException ex) {
			System.err.println("Port agurment is not a number: " + args[1]);
		} catch (IOException ex) {
			System.err.println("IOException occured.");
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}
}

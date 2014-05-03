import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class DNSClient {
	
	enum DNSStatus {
		SUCCESSFUL,
		TIMEOUT,
		EXPIRED_REQUEST
	}

    private static String serverName;
    private static int serverPort;
    
    private static InetAddress dnsServer;
    
    private static int currentID = 0x00;
    
    private static DNSStatus sendRequestForWebsite(String website) throws Exception{
    	currentID++; 	// Generate a new id for each request

    	byte[] websiteBytes = getBytesForDomainName(website);
    	    	
    	byte[] bytes = new byte[2 + mBytesHeader.length + websiteBytes.length + mBytesFooter.length];
    	
    	System.arraycopy(new byte[]{ (byte)(currentID >> 8), (byte)currentID }, 0, bytes, 0, 2);
    	System.arraycopy(mBytesHeader, 0, bytes, 2, mBytesHeader.length);
    	System.arraycopy(websiteBytes, 0, bytes, 2 + mBytesHeader.length, websiteBytes.length);
    	System.arraycopy(mBytesFooter, 0, bytes, 2 + mBytesHeader.length + websiteBytes.length, mBytesFooter.length);
    	
    	DatagramSocket socket = new DatagramSocket();

    	DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, dnsServer, serverPort);
    	DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
    	
    	socket.send(sendPacket);  
    	
    	socket.setSoTimeout(2000);
    	try {
    		socket.receive(receivePacket);
    	} catch(SocketTimeoutException e){
    		System.out.println("Timed out. Trying again.");
    		return DNSStatus.TIMEOUT;
    	}
    	
    	byte[] data = receivePacket.getData();
    	int id = data[0] << 8 | data[1];
    	
    	// Checks if it is a response from an old request, if so, ignore it
//    	if(id != currentID){
//    		return DNSStatus.EXPIRED_REQUEST;
//    	}
    	
    	int numAnswers = data[6] + data[7];
    	
    	// Skip over the name section of data by finding the end of the
    	// name then skipping over the next 4 bytes
    	int i = 12;
    	while(data[i] != 0){
    		i++;
    	}
    	i += 5;
    	
    	// Print each IP Address
    	for(int answers=0; answers < numAnswers; answers++){
        	System.out.printf("%d.%d.%d.%d\n", 
			bToI(data[i+12]), 
			bToI(data[i+13]), 
			bToI(data[i+14]), 
			bToI(data[i+15]));
        	i += 16;
    	}

    	socket.close();
    	return DNSStatus.SUCCESSFUL;
    }

    private static void run() throws Exception {
//    	serverName = "wmarrero2.cstcis.cti.depaul.edu";
//    	serverPort = 6054;
//    	serverName = "8.8.8.8";
//    	serverPort = 53;
    	
    	dnsServer = InetAddress.getByName(serverName);
    	
    	Scanner scanner = new Scanner(System.in);
    	scanner.useDelimiter("\\n");
    	
    	System.out.print("Enter domain name> ");
    	String website = scanner.nextLine();
    	
    	while(!website.equals("")){
    		DNSStatus completed = DNSStatus.TIMEOUT;
    		do {
    			completed = sendRequestForWebsite(website);
    		} while(completed == DNSStatus.TIMEOUT);
    		
    		System.out.print("Enter domain name> ");
    		website = scanner.nextLine();
    	}
    	
    }
    
    private static byte[] getBytesForDomainName(String domainName){
    	String[] sections = domainName.split("\\.");
    	byte[] bytes = new byte[domainName.length()+2];
    	int currIndex = 0;
    	
    	for(int section=0; section < sections.length; section++){
    		String sectionString = sections[section];
    		bytes[currIndex++] = (byte)sectionString.length();
    		
    		for(int c = 0; c < sectionString.length(); c++){
    			bytes[currIndex++] = (byte)sectionString.charAt(c);
    		}
    	}
    	return bytes;
    }
    
    private static int bToI(byte i){
    	int value = (int)i;
    	return value < 0 ? value += 256 : value;
    }

    static byte[] mBytesHeader = new byte[]{
    	(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, 
    	(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
    };
    
    static byte[] mBytesFooter = new byte[]{
		(byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01
    };

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            serverName = args[0];
            serverPort = Integer.parseInt(args[1]);
            run();
        } else {
            System.err.println("Usage: java DNSClient <hostname> <port>");
        }
    }
    
}
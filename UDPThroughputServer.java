import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPThroughputServer {
	// Class constants
	static final int PORT = 10720;								// Port number assigned in class
	private static final int ITERATIONS[] = {1024, 2048, 8192};	// Array containing number of iterations for calculations
	private static final int BYTESIZES[] = {1024, 512, 128};	// Array containing byte sizes of sent messages
    private static final int ACK_SIZE = 8;						// Size of acknowledgement in the reverse direction
	private static final long ACK = 98765432123456789L;			// 8-byte acknowledgement in the reverse direction

	// Initialize InetSocketAddresses, DatagramChannel, and ByteBuffers
	private static InetSocketAddress address =  null;
	private static InetSocketAddress clientAddress = null;
	private static DatagramChannel channel = null;
	private static ByteBuffer buffer = null;
	private static ByteBuffer ackBuffer = null;
	
 	//*********************************************************************************************
	//
	// Main
	//
	public static void main(String[] args) {
		try {
            // Open a datagramChannel and bind it to address
            channel = DatagramChannel.open();
            address = new InetSocketAddress(PORT);
            channel.bind(address);
			
			// Display on server
   			System.out.println("Server started on port #" + PORT);
   			System.out.println("Waiting for a client...");
   			
   			// Measure TCP throughput averages
			measureUDPThroughputAverage(BYTESIZES[0], ITERATIONS[0]);
			measureUDPThroughputAverage(BYTESIZES[1], ITERATIONS[1]);
			measureUDPThroughputAverage(BYTESIZES[2], ITERATIONS[2]);
			System.out.println("UDP throughput measured...");
			
			// Close the connection
   			channel.close(); 
   			System.out.println("Channel closed.");
			
		} catch(IOException i) {
	  		System.out.println(i);
		}
 	} 

	//*********************************************************************************************
	//
	// Measure UDP Throughput Average
	//
	private static void measureUDPThroughputAverage(int byteSize, int iterations) 
	{ 
		// Start server and wait for a connection 
		try {			
			// Allocate byte buffer for byteSize bytes
			buffer = ByteBuffer.allocate(byteSize);
			
			// Allocate byte buffer for ACK_SIZE bytes
			ackBuffer = ByteBuffer.allocate(ACK_SIZE);
			
			// Put ACK into ackBuffer
			ackBuffer.putLong(ACK);

			// Get average from ITERATIONS number of round trips
			for (int i = 0; i < iterations; i++) {
   				
				// Clear buffer of any previous data
				buffer.clear();
				
				// Receive data from the client
		        clientAddress = (InetSocketAddress) channel.receive(buffer);
				
	  			// Send ACK echo back to client
				ackBuffer.flip();
				channel.send(ackBuffer, clientAddress);
   			}
			
  		} catch(IOException i) { 
	  		System.out.println(i); 
  		}		
 	}
}

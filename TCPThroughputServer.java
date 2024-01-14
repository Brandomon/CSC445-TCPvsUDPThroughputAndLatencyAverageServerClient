import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPThroughputServer {
	// Class constants
	static final int PORT = 10720;								// Port number assigned in class
	private static final int ITERATIONS[] = {1024, 2048, 8192};	// Array containing number of iterations for calculations
	private static final int BYTESIZES[] = {1024, 512, 128};	// Array containing byte sizes of sent messages
    private static final int ACK_SIZE = 8;						// Size of acknowledgement in the reverse direction
	private static final long ACK = 98765432123456789L;			// 8-byte acknowledgement in the reverse direction

    // Initialize ServerSocketChannel, SocketChannel and ByteBuffers
 	private static ServerSocketChannel serverChannel = null;
 	private static SocketChannel channel = null;
 	private static ByteBuffer buffer = null;
 	private static ByteBuffer ackBuffer = null;
	
 	//*********************************************************************************************
	//
	// Main
	//
	public static void main(String[] args) {
		try {
			// Create a server channel, open, and bind to a port
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(PORT));
			
			// Display on server
   			System.out.println("Server started on port #" + PORT);
   			System.out.println("Waiting for a client...");
   
   			// Listen for a connection to be made and accept it
   			channel = serverChannel.accept();
   			System.out.println("Client accepted");
   			
   			// Measure TCP throughput averages
			measureTCPThroughputAverage(BYTESIZES[0], ITERATIONS[0]);
			measureTCPThroughputAverage(BYTESIZES[1], ITERATIONS[1]);
			measureTCPThroughputAverage(BYTESIZES[2], ITERATIONS[2]);
			
			// Close the connection
   			channel.close(); 
   			System.out.println("Connection successfully closed.");
			
		} catch(IOException i) {
	  		System.out.println(i);
		}
 	}
	
	//*********************************************************************************************
	//
	// Measure TCP Throughput Average
	//
	private static void measureTCPThroughputAverage(int byteSize, int iterations) 
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
				
				// Read from channel to buffer
				channel.read(buffer);
				
	  			// Send echo back to client
				ackBuffer.flip();
				channel.write(ackBuffer);
   			}
			
  		} catch(IOException i) { 
	  		System.out.println(i); 
  		}		
 	}
}

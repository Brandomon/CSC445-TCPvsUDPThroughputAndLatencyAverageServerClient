import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPLatencyServer {
	// Class Constants
	static final int PORT = 10720;								// Port number assigned in class
	static final int ITERATIONS = 100;							// Manually adjustable number of iterations for average
	private static final int BYTESIZES[] = {8, 32, 512, 1024};	// Array containing byte sizes of sent messages
	
	// Initialize socket channels and byte buffer
	private static ServerSocketChannel serverChannel = null;
	private static SocketChannel channel = null;
	private static ByteBuffer buffer = null;
	
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
   			
   			// Measure TCP latency averages
   			MeasureTCPLatencyAverage(BYTESIZES[0]);
   			MeasureTCPLatencyAverage(BYTESIZES[1]);
   			MeasureTCPLatencyAverage(BYTESIZES[2]);
			MeasureTCPLatencyAverage(BYTESIZES[3]);
   			
   			// Close the connection
   			channel.close(); 
   			System.out.println("Connection successfully closed.");
   			
	 	} catch(IOException i) {
	  		System.out.println(i);
		}
	}
	
	//*********************************************************************************************
	//
	// Measure TCP Latency Average
	//
	public static void MeasureTCPLatencyAverage(int byteSize) {
		try {
			// Allocate byte buffer for byteSize bytes
			buffer = ByteBuffer.allocate(byteSize);
			
			// Get average from ITERATIONS number of round trips
			for (int i = 0; i < ITERATIONS; i++) {
			
				// Clear buffer of any previous data
				buffer.clear();
				
				// Read from channel to buffer
				channel.read(buffer);   			
	   			
	   			/*
	   			// Test - Display received decrypted bytes 
	   			buffer.flip();
	   			while (buffer.hasRemaining()) {
	   				System.out.println(buffer.getLong() ^ KEY);
	   			}
	   			*/
				
	  			// Send echo back to client
	  			buffer.flip();
				channel.write(buffer);
			}
			
		} catch( Exception i) {
			System.out.println(i);
		}
	}
}

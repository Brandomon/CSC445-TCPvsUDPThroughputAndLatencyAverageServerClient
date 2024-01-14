import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPLatencyServer {
	// Class Constants
	private static final int PORT = 10720;						// Port number assigned in class
	private static final int ITERATIONS = 100;					// Number of iterations for average
	private static final int BYTESIZES[] = {8, 32, 512, 1024};	// Array containing byte sizes of sent messages
	
	// Initialize InetSocketAddresses, DatagramChannel, and ByteBuffer
	private static InetSocketAddress address =  null;
	private static InetSocketAddress clientAddress = null;
	private static DatagramChannel channel = null;
	private static ByteBuffer buffer = null;
	
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
            System.out.println("Server started on port #" + PORT);
            System.out.println("Waiting for a client...");
            
            MeasureUDPLatencyAverage(BYTESIZES[0]);
	        MeasureUDPLatencyAverage(BYTESIZES[1]);
	        MeasureUDPLatencyAverage(BYTESIZES[2]);
	        MeasureUDPLatencyAverage(BYTESIZES[3]);
	        System.out.println("UDP latency measured...");
            
            // Close the channel
            channel.close();
            System.out.println("Channel closed.");
            
        } catch (IOException i) {
            System.out.println(i);
        }
    }
    
	//*********************************************************************************************
	//
	// Measure UDP Latency Average
	//
    public static void MeasureUDPLatencyAverage(int byteSize) {
    	try {
    		System.out.println("Measuring UDP latency of " + byteSize + " bytes...");
    		
	        // Allocate byte buffer for byteSize bytes
	        buffer = ByteBuffer.allocate(byteSize);
	        
			// Get average from ITERATIONS number of round trips
			for (int i = 0; i < ITERATIONS; i++) {
				
				// Clear buffer of any previous data
				buffer.clear();
				
		        // Receive data from the client
		        clientAddress = (InetSocketAddress) channel.receive(buffer);
		        
		        // Send the data back to the client
		        buffer.flip();
		        channel.send(buffer, clientAddress);
			}
	        
    	} catch(Exception i) {
    		System.out.println(i);
    	}
    }
}
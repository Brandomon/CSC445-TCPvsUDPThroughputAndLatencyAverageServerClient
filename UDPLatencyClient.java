import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Random;

public class UDPLatencyClient {
	// Class constants
	//private static final String HOST_ID = "localHost";			// Local host id
	private static final String HOST_ID = "moxie.cs.oswego.edu";	// Moxie server host id
	private static final int PORT = 10720;							// Port number assigned in class
	private static final int ITERATIONS = 100;						// number of iterations for calculations of average
	private static final int BYTESIZES[] = {8, 32, 512, 1024};		// Array containing byte sizes of sent messages
	private static final long KEY = 12345678987654321L;				// Key for XOR encryption

	// Initialize InetSocketAddress, DatagramChannel, and ByteBuffer
	private static InetSocketAddress address = null;
	private static DatagramChannel channel = null;
	private static ByteBuffer buffer = null;

 	//*********************************************************************************************
	//
	// Main
	//
    public static void main(String[] args) {
    	try {    	
	    	// Initialize address
	    	address = new InetSocketAddress(HOST_ID, PORT);
	    	
	    	// Open datagramChannel
	        channel = DatagramChannel.open();
	        
	        // Measure UDP latency averages
	        MeasureUDPLatencyAverage(BYTESIZES[0]);
	        MeasureUDPLatencyAverage(BYTESIZES[1]);
	        MeasureUDPLatencyAverage(BYTESIZES[2]);
	        MeasureUDPLatencyAverage(BYTESIZES[3]);
	        
	        // Close channel
	        channel.close();
	        System.out.println("----------------------------------------------");
	        System.out.println("Channel closed...");
	        
    	} catch(IOException i)  {
    		System.out.println(i);
    	}
    }
    
	//*********************************************************************************************
	//
	// Measure UDP Latency Average
	//
    public static void MeasureUDPLatencyAverage(int byteSize) {
		// Local Variables
		long startTime = 0;				// Start time to measure TCP RTT
		long endTime = 0;				// End time to measure TCP RTT
		long roundTripTime = 0;			// Round trip time measurement
		long totalTime = 0;				// Total time measurement
		long averageTime = 0;			// Average time measurement
		int count = 0;					// Count for looping through iterations
		int successCount = 0;			// Count of successes to validate
		int failCount = 0;				// Count of failures to validate
		
		try {        
	        // Allocate byte buffer
	        buffer = ByteBuffer.allocate(byteSize);
	        
			// Get average from ITERATIONS number of round trips
			for (int i = 0; i < ITERATIONS; i++) {
				
				// Clear buffer of any previous data
				buffer.clear();
	        
		        // Create byte array of random bytes of size byteSize
				Random random = new Random();
				byte[] byteArray = new byte[byteSize / 8];
				random.nextBytes(byteArray);
				//System.out.println("Original Byte Array : " + Arrays.toString(byteArray));	// Test - Print original array of bytes
				
				// XOR byte array with key and store in byte buffer
				long[] longArray = new long[byteSize / 8];
				for(count = 0; count < byteSize / 8; count++) {
					longArray[count] = byteArray[count] ^ KEY;
					buffer.putLong(longArray[count]);
					//System.out.println("Index " + count + " of longArray : " + longArray[count]);
				}
		        
				// Get start time
				startTime = System.nanoTime();
				
		        // Send buffer over channel to address
				buffer.flip();
		        channel.send(buffer, address);
		        
		        // Clear buffer
		        buffer.clear();
		        
		        // Receive reply from server
		        channel.receive(buffer);
		        
		        // Flip buffer and display
		        buffer.flip();
		        int[] receivedData = new int[byteSize / 8];
				for(count = 0; count < byteSize / 8; count++) {
					long received = buffer.getLong();
					long decoded = received ^ KEY;
					receivedData[count] = (int)decoded;
					//System.out.println("Received : " + receivedData[count]);	// Test - Print received decoded data
				}
				
				// Validate and calculate times
				if (Arrays.toString(receivedData).equals(Arrays.toString(byteArray))) {
					
					// Get end time
					endTime = System.nanoTime();
					
					// Increment success count
					successCount++;
						
					// Calculate RTT
					roundTripTime = endTime - startTime;
					
					// Calculate totalTime
					totalTime += roundTripTime;
				}
				else {
					failCount++;
				}
			}	// End for loop
			
			// Calculate averageTime
			averageTime = totalTime / successCount;
			
			// Display results
			System.out.println("----------------------------------------------");
			System.out.println("*Average taken from " + (ITERATIONS - failCount) + " samples of " + byteSize + " Bytes*");
			System.out.println("Failcount : " + failCount);
			System.out.println("Total Time : " + totalTime + "ns");
			System.out.println("Average RTT Time: " + averageTime + " ns / " + String.format("%.6f", (double)averageTime / 1_000_000) + " ms / " + String.format("%.6f", (double)averageTime / 1_000_000_000) + " seconds");
			
		} catch(IOException i) {
			System.out.println(i);
		}
    }
}
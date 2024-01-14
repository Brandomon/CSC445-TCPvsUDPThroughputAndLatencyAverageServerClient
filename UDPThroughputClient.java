import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

public class UDPThroughputClient {
	// Class constants
	//private static final String HOST_ID = "localHost";			// Local host id
	private static final String HOST_ID = "moxie.cs.oswego.edu";	// Moxie server host id
	private static final int PORT = 10720;							// Port number assigned in class
	private static final int ITERATIONS[] = {1024, 2048, 8192};		// Array containing number of iterations for calculations
	private static final int BYTESIZES[] = {1024, 512, 128};		// Array containing byte sizes of sent messages
	private static final long KEY = 12345678987654321L;				// Key for XOR encryption
	private static final long ACK = 98765432123456789L;				// 8-byte acknowledgement in the reverse direction
	
	// Initialize InetSocketAddress, DatagramChannel, and ByteBuffer
	private static InetSocketAddress address = null;	
	private static DatagramChannel channel = null;
	private static ByteBuffer buffer = null;
	
	//*********************************************************************************************
	//
	// Main
	//
	public static void main(String args[]) {
		try {
	    	// Initialize address
	    	address = new InetSocketAddress(HOST_ID, PORT);
	    	
	    	// Open datagramChannel
	        channel = DatagramChannel.open();
			
			// Measure UDP Throughput for 1024 messages of 1024 bytes
			MeasureUDPThroughput(BYTESIZES[0], ITERATIONS[0]);
			
			// Measure UDP Throughput for 2048 messages of 512 bytes
			MeasureUDPThroughput(BYTESIZES[1], ITERATIONS[1]);
			
			// Measure UDP Throughput for 8192 messages of 128 bytes
			MeasureUDPThroughput(BYTESIZES[2], ITERATIONS[2]);
			
			// Close the connection
			channel.close();
			System.out.println("----------------------------------------------");
			System.out.println("Connection closed...");
			
		} catch (IOException i) {
			System.out.println(i);
		}
	}
	
	//*********************************************************************************************
	//
	// Measure UDP Throughput Average
	//
	public static void MeasureUDPThroughput(int byteSize, int iterations) {
		// Local Variables
	    int failCount = 0;							// Count of failures to get accurate average
		long startTime = 0;							// Start time to measure TCP RTT
		long endTime = 0;							// End time to measure TCP RTT
		long roundTripTime = 0;						// Round trip time measurement
		long totalTime = 0;							// Total time measurement
		long averageTime = 0;						// Average time measurement
		double thruBytes = 0;						// Throughput measured in bytes/second
		double thruMBytes = 0;						// Throughput measured in MB/second
		
		try {
			// Allocate byte buffer for byteSize bytes
			buffer = ByteBuffer.allocate(byteSize);

			// Get average from ITERATIONS number of round trips
			for (int i = 0; i < iterations; i++) {
				
				// Clear buffer of any previous data
				buffer.clear();
				
				// Create byte array of random bytes of size byteSize / byte size of long
				Random rand = new Random();
				byte[] byteArray = new byte[byteSize / 8];
				rand.nextBytes(byteArray);
				//System.out.println(Arrays.toString(byteArray));	// Test - Print original array of bytes
				
				// XOR byte array with key and store in byte buffer
				long[] longArray = new long[byteSize / 8];
				for(int count = 0; count < byteSize / 8; count++) {
					longArray[count] = byteArray[count] ^ KEY;
					buffer.putLong(longArray[count]);
				}
				
				// Get start time
				startTime = System.nanoTime();
				
				// Send data to server
				buffer.flip();
				channel.send(buffer, address);
				
				// Clear buffer
				buffer.clear();
	
				// Receive reply from server
				channel.receive(buffer);
				
				// Store reply as replyAck
				buffer.flip();
				long replyAck = buffer.getLong();
				//System.out.print(receivedData[count] + "  ");	// Test - Print received ACK	
				
				// Validate and calculate times
				if (replyAck == ACK) {
					// Get end time after receiving reply
					endTime = System.nanoTime();
					
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
			averageTime = totalTime / (iterations - failCount);
		
		// Calculate throughput in bytes/second and MB/second
		thruBytes = ((double)(byteSize * iterations)) / ((double)totalTime / 1_000_000_000);
		thruMBytes = thruBytes / 1_000_000;
		
		// Display results
		System.out.println("----------------------------------------------");
		System.out.println("*Throughput taken from " + (iterations - failCount) + " samples of " + byteSize + " Bytes*");
		System.out.println("Average RTT Time: " + averageTime + "ns / " + String.format("%.6f", (double)averageTime / 1_000_000_000) + " seconds");
		System.out.println("Throughput : " + String.format("%.3f", thruBytes) + " Bytes/sec / " + String.format("%.3f", thruMBytes) + " MB/sec");

		} catch (IOException i) {
			System.out.println(i);
		}
	}
}

package ss.udapi.sdk.examples;

import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;

public class StreamingProgram {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		GTPService theService = new GTPService();
		theService.start();
		Scanner theScanner = new Scanner(System.in);
		while(!theScanner.nextLine().equals(""));
	}

}

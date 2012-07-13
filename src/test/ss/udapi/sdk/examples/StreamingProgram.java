package ss.udapi.sdk.examples;

import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

public class StreamingProgram {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//BasicConfigurator.configure();
		PropertyConfigurator.configure(args[0]);
		
		GTPService theService = new GTPService(args[1]);
		theService.start();
		Scanner theScanner = new Scanner(System.in);
		while(!theScanner.nextLine().equals(""));
	}

}

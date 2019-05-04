package topK;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TopKAnalyzer {
	private static final String COMMA_DELIMITER = ",";

	public static String analyze(String filename) {
		
		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("logs.txt"), "UTF8"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(COMMA_DELIMITER);
		        System.out.println(values[2]);
		        records.add(Arrays.asList(values));
		    }
		} catch(Exception ex) {}
		
		return "done";
	}
}

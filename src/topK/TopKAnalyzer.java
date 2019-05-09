package topK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database;

public class TopKAnalyzer {
	private static final String INPUT_DATA_FILENAME = "ARInput.txt";
	private static final String OUTPUT_DATA_FILENAME = "AROutput.txt";
	
	private static TreeMap<String, TreeSet<Integer>> users = new TreeMap<String, TreeSet<Integer>>();
	private static ArrayList<String> ips = new ArrayList<String>();
	private static HashMap<String, Integer> ipToIndex = new HashMap<String, Integer>();

	public static String analyze(String filename) {
		extractRelevantData(filename);
		writeToAssociationRuleInputFile();
		
		getRules();
		return getResultsString();
	}
	
	private static void extractRelevantData(String filename) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
			br.readLine();
		    String line = null;
		    while ((line = br.readLine()) != null) {
		    	String[] fields = line.split(",");
		    	if(fields.length < 8) {
		    		continue;
		    	}

		    	Pattern pattern = Pattern.compile("user with id '(.*?)'");
		    	Matcher matcher = pattern.matcher(line);
		    	if (!matcher.find()) {
		    	    continue;
		    	}
		    	
		    	String id = matcher.group(1);
		    	String ipAddress = line.split(",")[7];
		    	
		    	if(!users.containsKey(id)) {
		    		users.put(id, new TreeSet<Integer>());
		    	}
		    	
		    	if(!ipToIndex.containsKey(ipAddress)) {
		    		ipToIndex.put(ipAddress, ips.size());
		    		ips.add(ipAddress);
		    	}
		    	
		    	users.get(id).add(ipToIndex.get(ipAddress));
		    }
		} catch(Exception ex) {
			System.out.println(ex);
		}
	}
	
	private static void writeToAssociationRuleInputFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(INPUT_DATA_FILENAME);
			for(Entry<String, TreeSet<Integer>> user : users.entrySet()) {
				for(int ipIndex : user.getValue()) {
					writer.print(ipIndex);
					writer.print(" ");
				}
				writer.print("\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	private static void getRules() {

		Database database = new Database(); 
		try {
			database.loadFile(INPUT_DATA_FILENAME);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		int k = 100;  
		double minConf = 0.8; //
		
		AlgoTopKRules algo = new AlgoTopKRules();
		
//		// This optional parameter allows to specify the maximum number of items in the 
//		// left side (antecedent) of rules found:
//		algo.setMaxAntecedentSize(2);  // optional
//
//		// This optional parameter allows to specify the maximum number of items in the 
//		// right side (consequent) of rules found:
//		algo.setMaxConsequentSize(1);  // optional
		
		algo.runAlgorithm(k, minConf, database);
		algo.printStats();
		try {
			algo.writeResultTofile(OUTPUT_DATA_FILENAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


    private static String getResultsString(){
    	String message = "Users using ip addresses 1 are likely to use ip addresses 2 as well\n";
    	message += "IPAddresses1, IPAddresses2, support, confidence\n";
    	
    	try (BufferedReader br = new BufferedReader(new FileReader(new File(OUTPUT_DATA_FILENAME)))) {
		    String line;
		    String[] currentSegments;
		    while ((line = br.readLine()) != null) {
		    	currentSegments = line.split("  ==> ");
		    	message += translateIPAddresses(currentSegments[0]) + ", ";
		    	line = currentSegments[1];

		    	currentSegments = line.split("  #SUP: ");
		    	message += translateIPAddresses(currentSegments[0]) + ", ";
		    	line = currentSegments[1];
		    	
		    	currentSegments = line.split(" #CONF: ");
		    	message += currentSegments[0] + ", ";
		    	message += currentSegments[1];
		    	
		    	message += "\n";
		    }
		} catch(Exception ex) {
			System.out.println(ex);
		}
    	
        return message;
    }
    
    private static String translateIPAddresses(String ipIndexesString) {
    	String ipAddresses = "";

    	String[] ipIndexes = ipIndexesString.split(" ");
    	for(String ipIndexString : ipIndexes) {
    		int ipIndex = Integer.parseInt(ipIndexString);
    		ipAddresses += ips.get(ipIndex) + " ";
    	}
    	
    	return ipAddresses;
    }
}
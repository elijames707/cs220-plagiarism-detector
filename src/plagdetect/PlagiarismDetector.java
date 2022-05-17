package plagdetect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class PlagiarismDetector implements IPlagiarismDetector {
	
	Map<String, Map<String, Set<String>>> commonNGrams = new HashMap<>();
	Map<String, Set<String>> filegrams = new HashMap<>();
	private int n;
	
	public PlagiarismDetector(int n) {
		this.n=n;
	}
	
	@Override
	public int getN() {
		return n;
	}

	@Override
	public Collection<String> getFilenames() {
		Set<String> names = new HashSet<>();
		for (String key : commonNGrams.keySet()) {
			names.add(key);
		}
		return names;
	}

	@Override
	public Collection<String> getNgramsInFile(String filename) {
		return commonNGrams.get(filename).get(filename);
	}

	@Override
	public int getNumNgramsInFile(String filename) {
		return commonNGrams.get(filename).get(filename).size();
	}

	@Override
	public Map<String, Map<String, Integer>> getResults() {
		Map<String, Map<String, Integer>> results = new HashMap<>();
		for (String key1 : commonNGrams.keySet()) {
			for (String key2 : commonNGrams.get(key1).keySet()) {
				Map<String, Integer> map = new HashMap<>();
				map.put(key2, getNumNGramsInCommon(key1, key2));
				results.put(key1, map);
			}
		}
		return results;
	}

	@Override
	public void readFile(File file) throws IOException {
		// most of your work can happen in this method
		Scanner scn = new Scanner(file);
		Set<String> ngrams = new HashSet<>();
		String f = file.getName();
		while (scn.hasNextLine()) {
			String line = scn.nextLine();
			String[] s = line.split(" ");
			if (s.length>=n) {
				for (int i=0; i<s.length-(n-1); i++) {
					String x = "";
					for (int j=0; j<n-1; j++) {
						x = x +" "+s[i+j];
					}
				ngrams.add(x);
				}
			}
		}
		filegrams.put(f, ngrams);
		//compare n-grams with other files (I met with a TA for this section but I'm pretty sure it still doesn't work)
		commonNGrams.put(f, filegrams);
		commonNGrams.get(f).remove(f);
		scn.close();
	}

	@Override
	public int getNumNGramsInCommon(String file1, String file2) {
		return commonNGrams.get(file1).get(file2).size();
	}

	@Override
	public Collection<String> getSuspiciousPairs(int minNgrams) {
		Set<String> pairs = new HashSet<>();
		for (String key1 : commonNGrams.keySet()) {
			for (String key2 : commonNGrams.get(key1).keySet()) {
				if (getNumNGramsInCommon(key1, key2) >= minNgrams) {
					if (key1.compareTo(key2) > 0) {
						String p = key2 + " " + key1 + " " + getNumNGramsInCommon(key1, key2);
						pairs.add(p);
					} else {
						String p = key1 + " " + key2 + " " + getNumNGramsInCommon(key1, key2);
						pairs.add(p);
					}
				}
			}
		}
		return pairs;
	}

	@Override
	public void readFilesInDirectory(File dir) throws IOException {
		// delegation!
		// just go through each file in the directory, and delegate
		// to the method for reading a file
		for (File f : dir.listFiles()) {
			readFile(f);
		}
	}
}

package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.NtkConnection.Side;

public class FileProcessor {
	
	private final NtkModel model;
	
	public FileProcessor (NtkModel model) {
		this.model = model;
	}
	
	public void processFile (File file) throws NtkException {
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				processLine(line);
			}
			
		} catch (IOException ex) {
			throw new NtkException("Error reading file");
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					throw new NtkException("Cannot close reader");
				}
		}
		
		this.model.unDamage();
	}
	
	public void processLine (String line) throws NtkException, NumberFormatException {
		final String[] parts = split(line);
		
		if (parts[0].equals("N")) {
			this.model.addNode(new NtkNode(parts[3], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), this.model));
		} else if (parts[0].equals("C")) {
			this.model.addConnection(new NtkConnection(parts[1], Side.get(parts[2]), parts[3], Side.get(parts[4]), this.model));
		} else {
			throw new NtkException(String.format("Invalid file format.\nLine: %s", line));
		}
	}
	
	private static String[] split(String line) {
		List<String> result = new ArrayList<String>();
		Matcher m = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(line);
		
		while (m.find()) {
			if (m.group(1) != null) {
				result.add(m.group(1));
			} else if (m.group(2) != null) {
				result.add(m.group(2));
			} else {
				result.add(m.group());
			}
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	public void save() throws NtkException {
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(this.model.getFile()));
			
			for (int i = 0; i < this.model.nNodes(); i++) {
				writer.write(this.model.getNode(i).toString());
				if (i != this.model.nNodes()) {
					writer.write("\n");
				}
			}
			for (int i = 0; i < this.model.nConnections(); i++) {
				writer.write(this.model.getConnection(i).toString());
				if (i != this.model.nConnections()) {
					writer.write("\n");
				}
			}
			
		} catch (IOException ex) {
			throw new NtkException("cannot close writer");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new NtkException("cannot close writer");
				}
			}
		}
	}

}
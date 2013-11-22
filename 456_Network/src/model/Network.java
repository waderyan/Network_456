package model;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

import view.NtkViewFrame;


public class Network implements NtkModelListener {

	private final Map<String, NtkModel> models;
	
	public Network () {
		this.models = new HashMap<String, NtkModel>();
	}
	
	public boolean canAddModel (File f) {
		return !this.models.containsKey(f.getAbsolutePath());
	}
	
	public void addModel (NtkModel model) {
		this.models.put(model.getFile().getAbsolutePath(), model);
	}
	
	public void removeModel (File f) {
		this.models.remove(f.getAbsolutePath());
	}
	
	public NtkModel getModel (File f) {
		return this.models.get(f.getAbsolutePath());
	}
	
	public void changeFile (NtkModel model, File oldF) {
		this.models.remove(oldF.getAbsolutePath());
		this.addModel(model);
	}
	
	public void update(NtkModelChange change) {
		switch (change.getType()) {
		case FileChanged:
			this.changeFile((NtkModel) change.getRef(), (File) change.getOldRef());
			break;
		default:
			break;
		}
	}
	
	public boolean validateArgs (String[] args) {
		return args.length == 1 && !args[0].trim().equals("");
	}
	
	private void run (String fileName) {
		NtkModel model = null;
		
		try {
			model = new NtkModel(new java.io.File(fileName));
			this.addModel(model);
			new NtkViewFrame(model, this);
		} catch (NtkException e) {
			System.out.println("Error occurred while creating model");
			e.printStackTrace();
			return;
		}
	}
	
	public static void main (String[] args) {
		Network ntk = new Network();
		if (!ntk.validateArgs(args)) {
			System.out.println("Invalid arguments. Usage: filename");
			return;
		}
		
		ntk.run(args[0]);
	}

	
}

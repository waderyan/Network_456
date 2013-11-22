package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;


import common.FileUtils;

import model.Network;
import model.NtkException;
import model.NtkModel;

public class NtkMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private NtkModel model;
	private final Network ntk;
	private final JFileChooser fc;
	private final String NTK = "ntk";
	private final NtkViewFrame parent;
	
	public NtkMenuBar (NtkViewFrame parent, Network network, NtkModel model) {
		super();
		this.ntk = network;
		this.setModel(model);
		this.parent = parent;
		
		this.fc = new JFileChooser();
		this.fc.setCurrentDirectory(this.model.getFile());
		this.fc.setAcceptAllFileFilterUsed(false);
		this.fc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return FileUtils.getExtension(f).equals(NTK);
			}
			
			public String getDescription() {
				return String.format(".%s", NTK);
			}
		});
		draw();
	}
	
	private void open(File f) {
		try {
			NtkModel model = null;
			if (ntk.getModel(f) == null) {
				model = new NtkModel(f);
				ntk.addModel(model);
			} else {
				model = ntk.getModel(f);
			}
			
			assert model != null : "model is null!";
			
			new NtkViewFrame(model, ntk);
		} catch (NtkException e) {
			System.out.println("Error creating model");
			e.printStackTrace();
		}
	}
	
	private void save() {
		this.model.save();
	}
	
	private void saveAs(File f) {
		// detach all listeners from model except the one that this menu bar is on
		try {
			NtkModel model = new NtkModel(this.model, f);
			this.parent.update(model);
			this.model = model;
		} catch (NtkException e) {
			System.out.println("error saving as");
			e.printStackTrace();
		}
	}
	
	private void draw () {
		this.removeAll();
		
		JMenu editMenu = new JMenu("Edit");
		
		JMenuItem undo = new JMenuItem("Undo");
		undo.setMnemonic(KeyEvent.VK_UNDO);
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("undo");
			}
		});
		
		JMenuItem redo = new JMenuItem("Redo");
		redo.setMnemonic(KeyEvent.VK_UNDO);
		redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("redo");
			}
		});
		
		editMenu.add(undo);
		editMenu.add(redo);
		
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem openItem = new JMenuItem("Open");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		            open(fc.getSelectedFile());
		        } 
			}
		});
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		JMenuItem saveasItem = new JMenuItem("Save As ...");
		saveasItem.setMnemonic(KeyEvent.VK_A);
		saveasItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			         saveAs(fc.getSelectedFile());
			     } 
			}
		});
		
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveasItem);
		
		this.add(fileMenu);
		this.add(editMenu);
	}

	public void setModel(NtkModel m) {
		this.model = m;
	}

}

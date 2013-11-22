package view;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import model.Network;
import model.NtkException;
import model.NtkModel;

public class NtkViewFrame {

	private final JFrame frame;
	private final int ST_WID = 900;
	private final int ST_HEI = 800;
	private final int ST_SIZE = 800;
	private final int ST_MULT = 25;
	private static final int MIN_FRAMES = 1;
	private NtkModel model;
	private final NtkMenuBar menubar;
	private NtkView view;
	private final Network ntk;
	private NtkToolbar toolbar;
	private boolean initState;
	
	private static int instanceCounter = 0;

	public NtkViewFrame (final NtkModel m, final Network ntk) {
		this.initState = true;
		instanceCounter++;
		this.model = m;
		this.ntk = ntk;
		
		this.menubar = new NtkMenuBar(this, this.ntk, this.model);
		try {
			this.view = new NtkView(this.ntk, this.model);
		} catch (NtkException e) {
			System.out.println("error-creating-view");
			e.printStackTrace();
		}
		
		this.toolbar = new NtkToolbar(this, this.view.getModel());
		this.frame = new JFrame();
		this.createFrame();
		this.initState = false;
	}
	
	private void createFrame() {	
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.frame.setBounds(instanceCounter * ST_MULT, instanceCounter * ST_MULT, ST_SIZE, ST_SIZE);
		this.frame.addWindowListener(ntkWinAdapter);
		
		JPanel panel = new JPanel();
		panel.add(this.toolbar);
		panel.add(this.view);
		
		this.frame.setSize(ST_WID, ST_HEI);
		this.frame.getContentPane().setLayout(new FlowLayout());
		this.frame.getContentPane().add(panel);
		
		this.frame.setJMenuBar(menubar);
		this.frame.setVisible(true);
		
		this.setModel(this.model);
	}
	
	private WindowAdapter ntkWinAdapter = new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
			if (model.getInstanceCnt() == NtkViewFrame.MIN_FRAMES) {
				if (!model.unsavedChanges()) {
					close();
					return;
				}
				Object[] options = {
					"Yes",
					"No",
					"Cancel"
				};
				int result = JOptionPane.showOptionDialog(
						frame, 
						"You have unsaved changes.\nWould you like to save?", 
						"Unsaved Changes", 
						JOptionPane.YES_NO_CANCEL_OPTION, 
						JOptionPane.WARNING_MESSAGE, 
						null, 
						options, 
						options[0]
				);
				switch (result) {
				case JOptionPane.YES_OPTION:
					model.save();
				case JOptionPane.NO_OPTION:
					close();
					break;
				default:
					return;
				}
			} else {
				close();
			}
		}
	};
	
	private void close () {
		this.frame.setVisible(false);
		this.model.decrementViewInstance();
		this.frame.dispose();
	}
	
	private void setModel(NtkModel m) {
		if (!this.initState) {
			this.model.decrementViewInstance();
		}
		
		this.model = m;
		this.model.incrementViewInstance();
		this.frame.setTitle(m.getFile().getName());	
	}
	
	public NtkView getView() {
		return this.view;
	}

	public void update(NtkModel m) {
		this.setModel(m);
		this.view.setModel(m);
		this.menubar.setModel(m);
		this.toolbar.setModel(m);
	}

}

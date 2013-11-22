package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.NtkModel;
import model.NtkModelChange;
import model.NtkModelListener;
import model.NtkModel.ActType;

public class NtkToolbar extends JPanel implements NtkModelListener {

	private static final long serialVersionUID = 1L;
	private NtkModel model;
	
	private JButton selectBtn;
	private JButton connBtn;
	private JButton nodeBtn;
	private JButton rotBtn;
	
	private static final String sIcon = "resources/select2.png";
	private static final String nIcon = "resources/node.png";
	private static final String cIcon = "resources/connection.png";
	private static final String rIcon = "resources/rotate.png";
	
	protected static final Color pressed = Color.yellow;
	
	public NtkToolbar (final NtkViewFrame parent, final NtkModel model) {
		this.setModel(model);
		
		createComps();
		layoutComps();
	}
	
	public void createComps () {
		selectBtn = new SelectBtn();
		selectBtn.setIcon(new ImageIcon(sIcon));
		selectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setActType(ActType.Select);
			}
		});
		nodeBtn = new NodeBtn();
		nodeBtn.setIcon(new ImageIcon(nIcon));
		nodeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setActType(ActType.AddNode);
			}
		});
		connBtn = new ConnectionBtn();
		connBtn.setIcon(new ImageIcon(cIcon));
		connBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setActType(ActType.AddConnection);
			}
		});
		rotBtn = new JButton();
		rotBtn.setIcon(new ImageIcon(rIcon));
		rotBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setActType(ActType.Rotate);
			}
		});
	}
	
	public void layoutComps () {
		this.setBorder(new EmptyBorder(0, 5, 0, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);
		
		this.add(selectBtn);
		this.add(nodeBtn);
		this.add(connBtn);
		this.add(rotBtn);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(100, 800);
	}

	protected ActType getActionType () {
		return this.model.getActType();
	}
	
	private class SelectBtn extends JButton {
		
		private static final long serialVersionUID = 1L;
		
		protected void paintComponent (Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
//			if (getActionType() == ActType.Select) {
//				g2.setColor(pressed);
//				g2.fillRect(0, 0, getWidth(), getHeight());
//			} else {
//				g2.clearRect(0, 0, getWidth(), getHeight());
//			}
			
			super.paintComponent(g2);
		}
	}
	
	private class NodeBtn extends JButton {
		
		private static final long serialVersionUID = 1L;
		
		protected void paintComponent (Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
//			if (getActionType() == ActType.AddNode) {
//				g2.setColor(pressed);
//				g2.fillRect(0, 0, getWidth(), getHeight());
//			} else {
//				g2.clearRect(0, 0, getWidth(), getHeight());
//			}
			
			super.paintComponent(g2);
		}
	}

	private class ConnectionBtn extends JButton {

		private static final long serialVersionUID = 1L;

		protected void paintComponent (Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
//			if (getActionType() == ActType.AddConnection) {
//				g2.setColor(pressed);
//				g2.fillRect(0, 0, getWidth(), getHeight());
//			} else {
//				g2.clearRect(0, 0, getWidth(), getHeight());
//			}
			
			super.paintComponent(g2);
		}
	}
	

	
	public void update(NtkModelChange change) {
		switch (change.getType()) {
			case ActTypeChange:	
			default:
				break;
		}
	}

	public void setModel(NtkModel m) {
		this.model = m;
	}
}

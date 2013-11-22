package view;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import javax.swing.*;

import model.*;
import model.NtkConnection.Side;
import model.NtkModel.ActType;

import static common.MathUtils.*;

public class NtkView extends JPanel implements NtkModelListener {

	private NtkModel model;
	private static final long serialVersionUID = 1L;
	private List<NtkEntityView> comps;
	private List<NtkNodeView> nodes;
	private Map<UUID, NtkNodeView> idToNode;
	private final UUID id;
	
	private DraggingConnection draggingConn;
	
	private NtkNodeView selectedNode;
	private NtkConnectionView selectedConn;
	private int insertionPt;
	
	private boolean initState;
	private boolean firstTime = true;
	
	private Point center;
	
	public class DraggingConnection {
		
		public Point2D.Double p1;
		public Point2D.Double p2;
		public NtkNodeView n;
		public Side s;
		
		public DraggingConnection (int x1, int y1, int x2, int y2) {
			this.p1 = new Point2D.Double(x1, y1);
			this.p2 = new Point2D.Double(x2, y2);
		}
		
		public DraggingConnection (int x1, int y1, int x2, int y2, NtkNodeView n) {
			this(x1, y1, x2, y2);
			this.n = n;
			this.s = n.getClosestSide(new Point(x2, y2));
		}
	}
	
	public NtkView (Network network, NtkModel model) throws NtkException {
		this.initState = true;
		this.comps = new ArrayList<NtkEntityView>();
		this.nodes = new ArrayList<NtkNodeView>();
		this.idToNode = new HashMap<UUID, NtkNodeView>();
		this.id = UUID.randomUUID();
		
		// Voodoo is important to get the events to work
		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		this.enableEvents(AWTEvent.KEY_EVENT_MASK);
		
		this.addMouseListener(this.selectMouseListener);
		this.addMouseListener(this.addNodeMouseListener);
		this.addMouseListener(this.addConnMouseListener);
		this.addMouseListener(this.rotateMouseListener);
		this.addMouseMotionListener(this.rotateMouseListener);
		this.addMouseMotionListener(this.addConnMouseListener);
		this.addMouseMotionListener(this.selectMouseListener);
		this.addKeyListener(this.txtKeyListener);
		this.setFocusable(true);
		
		this.setModel(model);
		this.setMouseState(ActType.Select);
		this.initState = false;
	}
	
	public void setModel(NtkModel model) {
		if (!this.initState) {
			this.model.removeListener(this);
		}
		this.model = model;
		this.model.addListener(this);
		this.reloadComps();
		this.repaint();
	}
	
	private KeyAdapter txtKeyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
			if (selectedNode != null) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE 
						|| e.getKeyCode() == KeyEvent.VK_DELETE) {
						getModel().backspace(selectedNode, --insertionPt);
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					leftKey();
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					rightKey();
				} else if (Character.isDigit(e.getKeyChar()) 
						|| Character.isAlphabetic(e.getKeyChar())) {
					getModel().insertText(selectedNode, insertionPt++, e.getKeyChar());
				}
			}
		}
	};
	
	public void leftKey() {
		if (this.insertionPt > 0) {
			this.insertionPt--;
		}
		this.repaint();
	}
	
	public void rightKey() {
		if (this.selectedNode == null) return;
		if (this.insertionPt < this.selectedNode.getName().length()) {
			this.insertionPt++;
		}
		this.repaint();
	}
	
	public boolean isTextHighlighted(NtkEntityView comp) {
		return isHighlighted(comp) && this.insertionPt != -1;
	}
	
	public boolean isHighlighted(NtkEntityView comp) {
		if (comp instanceof NtkNodeView && selectedNode != null) {
			return selectedNode.getModel().getId().equals(((NtkNodeView) comp).getModel().getId());
		} else if (comp instanceof NtkConnectionView && selectedConn != null){
			return selectedConn.getModel().getId().equals(((NtkConnectionView) comp).getModel().getId());
		} else {
			return false;
		}
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.clearRect(0, 0, 1000, 1000);
		
		AffineTransform at = g2.getTransform();
		g2.transform(model.getTransform());

		List<NtkEntityView> comps = new ArrayList<NtkEntityView>(this.comps);
		for (NtkEntityView comp : comps) {
			comp.draw(g2, isHighlighted(comp), isTextHighlighted(comp), insertionPt);
		}
		if (draggingConn != null) {
			NtkConnectionView.drawDraggingConnection(g2, draggingConn);
		}
		
		if (this.firstTime) {	
			this.center = new Point(this.getVisibleRect().width / 2, this.getVisibleRect().height / 2);
			this.firstTime = false;
		}
		
		g2.setTransform(at);
		if (this.rotateMouseListener.isActive) {
			Point p = wToD(center);
			g2.drawRect(p.x, p.y, 4, 4);
		}
		
	}
	
	public Dimension getPreferredSize(){
        return new Dimension(800, 800);
    }
	
	public NtkModel getModel() {
		return this.model;
	}
	
	public GeometryDescriptor pointGeometry(Point mouseLoc) {
		List<GeometryDescriptor> hits1 = new ArrayList<GeometryDescriptor>();
		List<GeometryDescriptor> hits2 = new ArrayList<GeometryDescriptor>();
		
		for (int i = 0; i < comps.size(); i++) {
			if (comps.get(i).hitTestBB(mouseLoc)) {
				GeometryDescriptor tmp = new GeometryDescriptor(i, comps.get(i));
				hits1.add(tmp);
			}
		}
		
		for (GeometryDescriptor hit : hits1) {
			if (hit.getDrawingComp().hitTest(mouseLoc)) {
				int textIndex = -1;
				if ((textIndex = hit.getDrawingComp().hitTestText(mouseLoc)) != -1) {
					hit.addTextIndex(textIndex);
				}
				hits2.add(hit);
			}
		}
		
		return hits2.size() > 0 ? hits2.get(hits2.size() - 1) : GeometryDescriptor.noResult();
	}
	
	public NtkNodeView getClosestNode(Point p) {
		NtkNodeView closest = null;
		double clDist = Double.POSITIVE_INFINITY;
		final int PIXEL_DIST = 15;
		
		for (NtkNodeView n : this.nodes) {
			Point2D.Double pt = n.getClosestSidePoint(p);
			double dist = common.MathUtils.euclDist(pt.x, pt.y, p.x, p.y);
			if (dist < clDist) {
				clDist = dist;
				closest = n;
			}
		}
		
		return clDist <= PIXEL_DIST ? closest : null;
	}
	
	private NtkNodeView getNodeById(UUID id) throws NtkException {
		if (!idToNode.containsKey(id)) {
			throw new NtkException ("node does not exist!");
		}
		return idToNode.get(id);
	}
	
	private void reloadComps () {
		try {
			this.nodes.clear();
			this.comps.clear();
			this.idToNode.clear();
			
			for (int i = 0; i < model.nNodes(); i++) {
				NtkNodeView newNode = new NtkNodeView(this.model.getNode(i));
				this.comps.add(newNode);
				this.nodes.add(newNode);
				idToNode.put(this.model.getNode(i).getId(), newNode);
			}
			for (int i = 0; i < model.nConnections(); i++) {
				comps.add(new NtkConnectionView(
						this.model.getConnection(i),
						getNodeById(this.model.getConnection(i).getNode1().getId()),
						getNodeById(this.model.getConnection(i).getNode2().getId())
				));
			}
		} catch (NtkException e) {
			System.err.println("ERROR RE-LOADING COMPS");
			e.printStackTrace();
		}
		
	}

	private void setMouseState (ActType type) {
		switch (type){ 
		case Select:
			this.selectMouseListener.isActive = true;
			this.addNodeMouseListener.isActive = this.addConnMouseListener.isActive = this.rotateMouseListener.isActive = false;
			break;
		case AddNode:
			this.selectedConn = null;
			this.selectedNode = null;
			this.addNodeMouseListener.isActive = true;
			this.selectMouseListener.isActive = this.addConnMouseListener.isActive = this.rotateMouseListener.isActive= false;
			break;
		case AddConnection:
			this.selectedConn = null;
			this.selectedNode = null;
			this.addConnMouseListener.isActive = true;
			this.addNodeMouseListener.isActive = this.selectMouseListener.isActive = this.rotateMouseListener.isActive= false;
			break;
		case Rotate:
			this.selectedConn = null;
			this.selectedNode = null;
		    this.rotateMouseListener.isActive = true;
			this.addNodeMouseListener.isActive = this.addConnMouseListener.isActive = this.selectMouseListener.isActive = false;
			break;
		}
		this.repaint();
	}
	
	private abstract class NtkMouseListener extends MouseAdapter {
		public boolean isActive = true;
	}
	
	private Point dToW(Point mousePoint) {
		Point2D p = null;
		try {
			p = model.getTransform().inverseTransform(mousePoint, null);
		} catch (NoninvertibleTransformException e1) {
			e1.printStackTrace();
		}
		return new Point((int) p.getX(), (int) p.getY());
	}
	
	private Point wToD(Point p) {
		Point2D p1 = model.getTransform().transform(p, null);
		return new Point((int) p1.getX(), (int) p1.getY());
	}
	
	private NtkMouseListener rotateMouseListener = new NtkMouseListener() {
		
		private Point md;
		private AffineTransform prevTransform = new AffineTransform();
		
		public void mouseClicked(MouseEvent e) {
			if (!isActive) return;
			if (e.getClickCount() == 2) {
				md = e.getPoint();
				AffineTransform t = model.getTransform();
				t.setToIdentity();
				prevTransform = new AffineTransform(t);
				center = new Point(getVisibleRect().width / 2, getVisibleRect().height / 2);
				model.setTransform(t);
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if (!isActive) return;
			md = e.getPoint();
		}
		
		public void mouseReleased(MouseEvent e) {
			if (!isActive) return;
			Point mu = e.getPoint();
			if (euclDist(md, mu) <= 3) {
				center = dToW(mu);
			} else {
				prevTransform = new AffineTransform(model.getTransform());
			}
			repaint();
		}
		
		public void mouseDragged(MouseEvent e) {
			if (!isActive) return;
			Point mu = e.getPoint();
			model.setTransform(rotAndScale(mu, new AffineTransform(prevTransform)));
		}
		
		private AffineTransform rotAndScale(Point mu, AffineTransform at) {
			Point c = center;
			double factor = euclDist(mu, c) / euclDist(md, c);
			
			double hd = euclDist(md, c);
			double hu = euclDist(mu, c);
			
			double sind = -1 * division(md.y - c.y, hd);
			double cosd = division(md.x - c.x, hd);
			double sinu = division(mu.y - c.y, hu);
			double cosu = division(mu.x - c.x, hu);
			
			at.translate(c.x, c.y);
			at.concatenate(new AffineTransform(cosu, sinu, -1 * sinu, cosu, 0, 0));
			at.scale(factor, factor);
			at.concatenate(new AffineTransform(cosd, sind, -1 * sind, cosd, 0, 0));
			at.translate(-1 * c.x, -1 * c.y);
			
			return at;
		}
	};
	
	
	private NtkMouseListener addNodeMouseListener = new NtkMouseListener() {
		
		public void mousePressed(MouseEvent e) {
			if (isActive) {
				requestFocus();
				getModel().addNode(new NtkNode("[node-name]", e.getX(), e.getY(), getModel()));
			}
		}
	};
	
	private NtkMouseListener addConnMouseListener = new NtkMouseListener() {

		private NtkNodeView firstNode;
		private NtkConnection.Side firstSide;
		private Point2D.Double p;
		
		public void mouseReleased(MouseEvent e) {
			if (!this.isActive || this.firstNode == null || this.firstSide == null) {
				return;
			}
			
			NtkNodeView secNode = getClosestNode(e.getPoint());
			// Assuming you can't connect to the same node
			if (secNode == null || firstNode.equals(secNode)) {
				draggingConn = null;
				repaint();
				return;
			}
			
			NtkConnection.Side secSide = secNode.getClosestSide(e.getPoint());
			draggingConn = null;
			getModel().addConnection(new NtkConnection(
					firstNode.getName(), 
					firstSide, 
					secNode.getName(), 
					secSide,
					getModel()
			));
			this.firstNode = null;
			this.firstSide = null;
			this.p = null;
		}
		
		public void mouseDragged(MouseEvent e) {
			if (!this.isActive || this.p == null) {
				return;
			}
			
			NtkNodeView n = getClosestNode(e.getPoint());
			// Snap to closest node
			draggingConn = (n == null) 
					? new DraggingConnection((int) p.x, (int) p.y, e.getPoint().x, e.getPoint().y)
					: new DraggingConnection((int) p.x, (int) p.y, e.getPoint().x, e.getPoint().y, n);
			
			repaint();
		}
		
		public void mousePressed(MouseEvent e) {
			if (!this.isActive) {
				return;
			}
			
			requestFocus();
			draggingConn = null;
			this.firstNode = getClosestNode(e.getPoint());
			if (this.firstNode == null) {
				return;
			}
			
			this.p = this.firstNode.getClosestSidePoint(e.getPoint());
			this.firstSide = this.firstNode.getClosestSide(e.getPoint());
		}
	};
	
	private NtkMouseListener selectMouseListener = new NtkMouseListener() {
		
		private Point p;
		private final int PIXEL_DIST = 3;
		private final int OFF_THE_PAGE = -100000;
		
		public void mouseReleased(MouseEvent e) {
			if (isActive) {
				moveNode(dToW(e.getPoint()));
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			if (!isActive || selectedNode == null) {
				return;
			}
			moveNode(dToW(e.getPoint()));	
		}

		public void mousePressed(MouseEvent e) {
			if (isActive) {
				requestFocus();
				GeometryDescriptor geom = pointGeometry(dToW(e.getPoint()));
				if (geom.getDrawingComp() != null) {
					selectNode(geom, dToW(e.getPoint()));
				} else {
					deselect();
					this.p = new Point(OFF_THE_PAGE, OFF_THE_PAGE);
				}
			}
		}
		
		public void selectNode(GeometryDescriptor geom, Point p) {
			getModel().select(geom.getDrawingComp().getModel(), geom.getTextIndex());
			this.p = p;
			if (geom.getDrawingComp() instanceof NtkNodeView) {
				selectedNode = (NtkNodeView) geom.getDrawingComp();
				selectedConn = null;
				insertionPt = geom.getTextIndex();
			} else {
				selectedNode = null;
				selectedConn = (NtkConnectionView) geom.getDrawingComp();
			}
		}
		
		public void moveNode(Point p) {
			if (euclDist(this.p.x, this.p.y, p.x, p.y) >= PIXEL_DIST && selectedNode != null) {
				double dx = p.x - this.p.x;
				double dy = p.y - this.p.y;
				this.p = p;
				getModel().move(selectedNode, selectedNode.getModel().getX() + dx, selectedNode.getModel().getY() + dy);
			}
		}
		
		public void deselect() {
			insertionPt = -1;
			selectedNode = null;
			selectedConn = null;
			repaint();
		}
	};
	
	public void update(NtkModelChange change) {
		switch (change.getType()) {
			case Highlight:
			case NodeNameInsertion:
			case NodeNameRemove:
			case NodeLocationChanged:
			case InsertionPointChange:
			case Deselect:
			case TransformChanged:
				this.repaint();
				break;
			case NodeAdded:
			case ConnectionAdded:
				this.reloadComps();
				this.repaint();
				break;
			case ActTypeChange:
				this.setMouseState(this.model.getActType());
				break;
			default:
				break;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NtkView other = (NtkView) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString() {
		return this.id.toString();
	}

}




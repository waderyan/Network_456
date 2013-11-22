package view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import model.NtkConnection.Side;
import model.NtkNode;
import common.MathUtils;

public class NtkNodeView extends NtkEntityView {
	
	private final NtkNode model;
	private static final Color COLOR = Color.CYAN;
	private static final Color H_COLOR = Color.YELLOW;
	private static final int offset = 30;
	
	private double x;
	private double y;
	private double w;
	private double h;
	
	private Point2D.Double D;
	private Point2D.Double U;
	private Point2D.Double C;
	
	private NodeText text;
	
	private class NodeText {
		
		private int basey;
		private int topy;
		private double startx;
		private double endx;
		private String[] _chars;
		private String text;
		private FontMetrics _fm;
		
		public NodeText (double startx, int width, int basey, int height, String name, FontMetrics fm) {
			this.basey = basey;
			this.topy = basey - height;
			this.startx = startx;
			this.endx = startx + width;
			this._chars = new String[name.length()];
			this.text = name;
			for (int i = 0; i < name.length(); i++) {
				_chars[i] = Character.toString(name.charAt(i));
			}
			this._fm = fm;
		}
		
		public void drawInsertionPoint(Graphics2D g2, int index) {
			final int yOffset = 1;
			g2.drawLine(
					(int) (this.startx + _fm.stringWidth(text.substring(0, index))), 
					basey + yOffset, 
					(int) (this.startx + _fm.stringWidth(text.substring(0, index))), 
					topy - yOffset
			);
		}
		
		public int hitTest (Point p) {
			int smallOffset = 2;
			
			// TODO: look into making this more liberal
			if (p.x < startx || p.x > (endx + smallOffset) || p.y < topy || p.y > basey) {
				return -1;
			}
			
			double totalWidth = startx;
			for (int i = 0; i < _chars.length; i++) {
				double width = _fm.stringWidth(_chars[i]);
				if (p.x > totalWidth && p.x <= (totalWidth + width)) {
					return i;
				}
				totalWidth += width;
			}
			
			if (p.x <= (endx + smallOffset)) {
				return _chars.length - 1;
			}
			
			System.out.println("TEXT ALGORITHM FAILURE!!!");
			return -10001;
		}

		public String toString () {
			return text;
		}
		
	}
	
	public String getName() {
		return text.toString();
	}
	
	public Point2D.Double getLeftSidePoint() {
		return new Point2D.Double( x,  y + (h / 2));
	}
	
	public Point2D.Double getRightSidePoint() {
		return new Point2D.Double( x + w,  y + (h / 2));
	}
	
	public Point2D.Double getTopSidePoint() {
		return new Point2D.Double( x + (w / 2),  y);
	}
	
	public Point2D.Double getBottomSidePoint() {
		return new Point2D.Double( x + (w / 2),  y + h);
	}
	
	private Point2D.Double[] getAllPoints() {
		final int MAX_PTS = 4;
		Point2D.Double[] result = new Point2D.Double[MAX_PTS];
		
		// Easily extendable if more points are defined
		result[0] = getLeftSidePoint();
		result[1] = getRightSidePoint();
		result[2] = getTopSidePoint();
		result[3] = getBottomSidePoint();
		
		return result;
	}
	
	public Point2D.Double getClosestSidePoint(Point p) {
		Point2D.Double[] points = getAllPoints();
		
		assert points.length > 0;
		Point2D.Double min = points[0];
		double minDist = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < points.length; i++) {
			double dist = common.MathUtils.euclDist((int) points[i].x, (int) points[i].y, p.x, p.y);
			if (dist < minDist) {
				minDist = dist;
				min = points[i];
			} 
		}
		return min;
	}
	
	public Side getClosestSide (Point p) {
		Point2D.Double closest = this.getClosestSidePoint(p);
		if (closest.equals(this.getRightSidePoint())) {
			return Side.Right;
		} else if (closest.equals(this.getTopSidePoint())) {
			return Side.Top;
		} else if (closest.equals(this.getBottomSidePoint())) {
			return Side.Bottom;
		} else  {
			return Side.Left;
		}
	}
	
	public NtkNode getModel () {
		return this.model;
	}
	
	public NtkNodeView (NtkNode node) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		model = node;
	}
	
	public void draw(Graphics2D g2, boolean isHighlighted, boolean isTextHighlighted, int insertionPt) {	
		g2.setFont(_font);
		FontMetrics FM = g2.getFontMetrics();
		int textWidth = FM.stringWidth(model.getName());
		int textHeight = FM.getHeight();
		
		h = textHeight + offset;
		w = textWidth + offset;
		x = model.getX() - (w / 2);
		y = model.getY() - (h / 2);
		
		D = new Point2D.Double(x, y);
		U = new Point2D.Double(x + w, y + h);
		C = new Point2D.Double((D.x + U.x) / 2, (D.y + U.y) / 2);
		
		double textBase = model.getY() + (textHeight / 2);
		double textLeft = model.getX() - (textWidth / 2);
		
		text = new NodeText((int) textLeft, textWidth, (int) textBase, textHeight, model.getName(), FM);
		
		Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, w, h);
		
		AffineTransform at = g2.getTransform();
		// g2.transform(bt);
		g2.setColor(!isHighlighted ? COLOR : H_COLOR);
		g2.fill(ellipse);
		g2.draw(ellipse);
		g2.setColor(_fontColor);
		
		g2.drawString(model.getName(), (int) textLeft, (int) textBase);
		
		if (isTextHighlighted) {
			text.drawInsertionPoint(g2, insertionPt);
		} 
		g2.setTransform(at);
	}

	public boolean hitTestBB(Point p) {
		if (p.x > U.x) {
			return false;
		} else if (p.x < D.x) {
			return false;
		} else if (p.y < D.y) {
			return false;
		} else if (p.y > U.y) {
			return false;
		}
		return true;
	}

	public boolean hitTest(Point p) {
		return (MathUtils.square((p.x - C.x) / 2) + MathUtils.square((p.y - C.y) / 2) - 1) >= 0;
	}

	public int hitTestText(Point p) {
		return text.hitTest(p);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((C == null) ? 0 : C.hashCode());
		result = prime * result + ((D == null) ? 0 : D.hashCode());
		result = prime * result + ((U == null) ? 0 : U.hashCode());
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
		NtkNodeView other = (NtkNodeView) obj;
		if (C == null) {
			if (other.C != null)
				return false;
		} else if (!C.equals(other.C))
			return false;
		if (D == null) {
			if (other.D != null)
				return false;
		} else if (!D.equals(other.D))
			return false;
		if (U == null) {
			if (other.U != null)
				return false;
		} else if (!U.equals(other.U))
			return false;
		return true;
	}

	
}

package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import model.AbstractNtkEntity;
import model.NtkConnection;
import model.NtkConnection.Side;

import static java.lang.Math.*;
import static common.MathUtils.*;

public class NtkConnectionView extends NtkEntityView {
	
	private final NtkConnection _model;
	private final NtkNodeView _node1;
	private final NtkNodeView _node2;
	private final static Color COLOR = Color.black;
	private final static Color H_COLOR = Color.YELLOW;
	
	private Point2D.Double _p1;
	private Point2D.Double _p2;
	private Point2D.Double _c1;
	private Point2D.Double _c2;
	
	public AbstractNtkEntity getModel() {
		return _model;
	}
	
	public NtkConnectionView (NtkConnection conn, NtkNodeView node1, NtkNodeView node2) {
		if (conn == null || node1 == null || node2 == null) {
			throw new IllegalArgumentException();
		}
		_model = conn;
		_node1 = node1;
		_node2 = node2;
	}
	
	private static Point2D.Double getSidePoint(Side side, NtkNodeView node)  {
		assert (side != null && node != null);
		
		switch (side) {
		case Left: 
			return node.getLeftSidePoint();
		case Right:
			return node.getRightSidePoint();
		case Top:
			return node.getTopSidePoint();
		case Bottom:
			return node.getBottomSidePoint();
		default:
			assert(false);
		}
		return null;
	}
	
	public void draw(Graphics2D g2, boolean isHighlighted, boolean isTextHighlighted, int insertionPt) {
		assert (g2 != null);
		
		g2.setColor(isHighlighted ? H_COLOR : COLOR);
		
		_p1 = getSidePoint(_model.getSide1(), _node1);
		_p2 = getSidePoint(_model.getSide2(), _node2);
		_c1 = getControlPoint(_model.getSide1(), _p1);
		_c2 = getControlPoint(_model.getSide2(), _p2);
		
		g2.draw(new CubicCurve2D.Double(_p1.x, _p1.y, _c1.x, _c1.y, _c2.x, _c2.y, _p2.x, _p2.y));
//		for (Point2D.Double p : points) {
//			g2.drawRect((int) p.x, (int) p.y, 5, 5);
//		}
	}
	
	public static void drawDraggingConnection(Graphics2D g2, NtkView.DraggingConnection conn) {
		Point2D.Double c1 = conn.n == null 
				? new Point2D.Double(conn.p1.x, conn.p1.y) 
				: getControlPoint(conn.s, conn.p1);
		Point2D.Double c2 = conn.n == null 
				? new Point2D.Double(conn.p2.x, conn.p2.y) 
				: getControlPoint(conn.s, conn.p2);

		g2.draw(new CubicCurve2D.Double(conn.p1.x, conn.p1.y, c1.x, c1.y, c2.x, c2.y, conn.p2.x, conn.p2.y));
	
	}
	
	private static Point2D.Double getControlPoint(Side side, Point2D.Double p) {
		final int OFFSET = 50;
		
		if (side == Side.Bottom) {
			return new Point2D.Double(p.x, p.y + OFFSET);
		} else if (side == Side.Top) {
			return new Point2D.Double(p.x, p.y - OFFSET);
		} else if (side == Side.Right) {
			return new Point2D.Double(p.x + OFFSET, p.y);
		} else {
			return new Point2D.Double(p.x - OFFSET, p.y);
		}
	}

	public boolean hitTestBB(Point p) {
		final int OFFSET = 20;
		
		double  minx = min(_p1.x, _p2.x),
				miny = min(_p1.y, _p2.y),
				maxx = max(_p1.x, _p2.x),
				maxy = max(_p1.y, _p2.y);
		
		if (p.x < (minx - OFFSET)) {
			return false;
		} else if (p.y < (miny - OFFSET)) {
			return false;
		} else if (p.x > (maxx + OFFSET)) {
			return false;
		} else if (p.y > (maxy + OFFSET)){
			return false;
		}
		return true;
	}

	public boolean hitTestDep(Point p) {
		final int HIT_BUFFER = 8;
		
		double a = _p2.y - _p1.y;
		double b = _p1.x - _p2.x;
		// Should be five pixels - not sure if this is entirely accurate but its close
		return abs(dist(p, a, b, -1 * (a * _p1.x + b * _p1.y))) < HIT_BUFFER;
	}
	
	private final double[][] BZ = {
			{-1, 3, -3, 1},
			{3, -6, 3, 0},
			{-3, 3, 0, 0},
			{1, 0, 0, 0}
	};
	
	public boolean hitTest(Point p) {
		Point2D.Double p2 = new Point2D.Double(p.x, p.y);
		final int OFFSET = 5;
		final double[][] G = {
				{_p1.x, _c1.x, _c2.x, _p2.x}	,
				{_p1.y, _c1.y, _c2.y, _p2.y}
		};
		return euclDist(nearestPoint(p2, 0, 1.0, matrixMultiply(G,BZ)), p2) <= OFFSET;
	}
	
	private List<Point2D.Double> points = new ArrayList<Point2D.Double>();
	
	private Point2D.Double nearestPoint (Point2D.Double p, double lowerT, double upperT, double[][] gbz) {
		int N = 10;
		double inc = (upperT - lowerT) / N;
		Point2D.Double lowP = computePoint(lowerT, gbz);
		Point2D.Double highP = computePoint(upperT, gbz);
		
		// base case
		// close enough for pixel resolution
		if (sqrDist(lowP, highP) < 1.0) {
			return lowP;
		}
		
		double nearT = lowerT;
		Point2D.Double nearP = lowP;
		double nearD = sqrDist (nearP, p);
		
		for (double t = lowerT + inc; t <= upperT; t += inc) {
			Point2D.Double tp = computePoint(t, gbz);
			points.add(tp);
			double d = sqrDist (tp, p);
			if (d < nearD) {
				nearD = d;
				nearT = t;
				nearP = tp;
			}
		}
		
		double newLow = nearT - inc;
		double newHigh = nearT + inc;
		if (newLow < lowerT) {
			newLow = lowerT;
		}
		if (newHigh > upperT) {
			newHigh = upperT;
		}
		return nearestPoint(p, newLow, newHigh, gbz);
	}
	
	private Point2D.Double computePoint (double t, double[][] gbz) {
		return new Point2D.Double(
				getBzCoord(gbz[0][0], gbz[0][1], gbz[0][2], gbz[0][3], t), 
				getBzCoord(gbz[1][0], gbz[1][1], gbz[1][2], gbz[1][3], t) - 1.5
		);
	}
	
	private double getBzCoord(double a, double b, double c, double d, double t) {
		return (a * pow(t, 3)) + (b * t * t) + (c * t) + d;
	}
	
	private double[][] matrixMultiply(double[][] a, double[][] b) {
		assert a[0].length == b.length;
		
		final int L = a.length;
		final int M = a[0].length;
		final int N = b[0].length;
		
		double[][] result = new double[L][N];
		
		for (int r = 0; r < L; r++) {
			for (int c = 0; c < N; c++) {
				result[r][c] = 0.0;
				for (int i = 0; i < M; i++) {
					result[r][c] += a[r][i] * b[i][c];
				}
			}
		}
		
		return result;
	}

	public int hitTestText(Point p) {
		return -1;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_p1 == null) ? 0 : _p1.hashCode());
		result = prime * result + ((_p2 == null) ? 0 : _p2.hashCode());
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
		NtkConnectionView other = (NtkConnectionView) obj;
		if (_p1 == null) {
			if (other._p1 != null)
				return false;
		} else if (!_p1.equals(other._p1))
			return false;
		if (_p2 == null) {
			if (other._p2 != null)
				return false;
		} else if (!_p2.equals(other._p2))
			return false;
		return true;
	}


}
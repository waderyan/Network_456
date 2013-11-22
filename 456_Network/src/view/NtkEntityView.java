package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;

import model.AbstractNtkEntity;

public abstract class NtkEntityView {
	
	protected static final Font _font = new Font("Helvetica", Font.BOLD, 15);
	protected static final Color _fontColor = Color.black;
	
	public abstract void draw(Graphics2D g2, boolean isHighlighted, boolean isTextHighlighted, int insertionPoint);
	
	public abstract AbstractNtkEntity getModel();
	
	public abstract boolean hitTestBB (Point p);
	
	public abstract boolean hitTest (Point p);
	
	public abstract int hitTestText (Point p);
	
	
}

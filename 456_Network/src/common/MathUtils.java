package common;

import static java.lang.Math.*;

import java.awt.Point;
import java.awt.geom.Point2D;

public class MathUtils {

	public static double square (double num) {
		return pow(num, 2);
	}
	
	public static double euclDist (int x1, int y1, int x2, int y2) {
		return sqrt(pow((x1 - x2), 2) + pow((y1 - y2), 2));
	}
	
	public static double euclDist (double x1, double y1, double x2, double y2) {
		return sqrt(pow((x1 - x2), 2) + pow((y1 - y2), 2));
	}
	
	public static double euclDist (Point2D.Double p1, Point2D.Double p2) {
		return sqrt(pow((p2.x - p1.x), 2) + pow((p2.y - p1.y), 2));
	}
	
	public static double euclDist (Point p1, Point p2) {
		return sqrt(pow((p2.x - p1.x), 2) + pow((p2.y - p1.y), 2));
	}
	
	public static double sqrDist (Point2D.Double p1, Point2D.Double p2) {
		return pow((p2.x - p1.x), 2) + pow((p2.y - p1.y), 2);
	}
	
	public static double dist(Point m, double a, double b, double c)  {
		double d = sqrt(a * a + b * b);
		return m.x * (a / d) + m.y * (b / d) + (c / d);
	}
	
	public static double division(int a, int b) {
		return (double) a / (double) b;
	}
	
	public static double division(int a, double b) {
		return (double) a / b;
	}
}

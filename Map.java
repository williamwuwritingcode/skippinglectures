import java.util.Hashtable;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.io.*;

public class Map implements Runnable{

	private Hashtable<Point2D.Double, Character> map;
	private Point2D.Double goldLocation;
	private LinkedList<Point2D.Double> dynamiteLocations;
	private LinkedList<Point2D.Double> axeLocations;
	private LinkedList<Point2D.Double> boatLocations;
	private Point2D.Double topLeft;
	private Point2D.Double bottomRight;


	public Map()
	{
		this.map = new Hashtable<Point2D.Double, Character>();
		this.topLeft = new Point2D.Double(0, 0);
		this.bottomRight = new Point2D.Double(0, 0);
		
		this.goldLocation = null;
		this.dynamiteLocations = new LinkedList<Point2D.Double>();
		this.axeLocations = new LinkedList<Point2D.Double>();
		this.boatLocations = new LinkedList<Point2D.Double>();
	}

	//get rid of run and main when we implement it
	public void run()
	{
		Point2D.Double p1 = new Point2D.Double(0, 0);
		updateMap(p1, 'a');
		Point2D.Double p2 = new Point2D.Double(1, 0);
		updateMap(p2, 'b');
		Point2D.Double p3 = new Point2D.Double(2, 0);
		updateMap(p3, 'd');
		Point2D.Double p4 = new Point2D.Double(3, 0);
		updateMap(p4, 'c');
		Point2D.Double p5= new Point2D.Double(3, 1);
		updateMap(p5, 'd');
		Point2D.Double p6 = new Point2D.Double(3, 2);
		updateMap(p6, 'g');
		Point2D.Double p7 = new Point2D.Double(3, 3);
		updateMap(p7, 'e');

		System.out.println("topLeft: " + topLeft.toString());
		System.out.println("bottomRight: " + bottomRight.toString());

		System.out.println("Printing map");
		printMap();
	}


	public Point2D.Double getGoldLocation()
	{
		return goldLocation;
	}

	public Hashtable<Point2D.Double, Character> getCurMap()
	{
		return map;
	}

	//Updates one point on the map at a time
	public void updateMap(Point2D.Double point, Character thing)
	{
		map.put(point, thing);

		//update gold location
		if(thing.equals("g"))
			this.goldLocation = point;
		else if (thing.equals("d"))
			this.dynamiteLocations.add(point);
		else if (thing.equals("B"))
			this.boatLocations.add(point);
		else if (thing.equals("a"))
			this.axeLocations.add(point);

		//update bottom right and top left if needed
		if (point.getX() > bottomRight.getX())
			bottomRight.setLocation(point.getX(), bottomRight.getY());
		else if (point.getX() < topLeft.getX())
			topLeft.setLocation(point.getX(), topLeft.getY());

		if (point.getY() > topLeft.getY())
			topLeft.setLocation(topLeft.getX(), point.getY());
		else if (point.getY() < bottomRight.getY())
			bottomRight.setLocation(bottomRight.getX(), point.getY());
	}

	public void printMap()
	{
		for(int y = (int)bottomRight.getY(); y <= topLeft.getY(); y++)
		{
			for(int x = (int)topLeft.getX(); x <= bottomRight.getX(); x++)
			{
				Point2D.Double currentPos = new Point2D.Double(x, y);
				Character thing = map.get(currentPos);
				if(thing == null)
					System.out.print("?");
				else 
					System.out.print(thing);
			}
			System.out.println("");
		}
	}

	/*public static void main(String[] args)
	{
		Map map = new Map();
		map.run();
	}
    */
}

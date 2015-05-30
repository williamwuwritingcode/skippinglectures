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

	//directions
	private static final int NORTH = 1;
	private static final int EAST = 2;
	private static final int SOUTH = 3;
	private static final int WEST = 4;

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
		char[][] view = {
			{'*', ' ', ' ', '~', '~'},
			{'~', '*', ' ', '*', '~'},
			{'B', '*', ' ', ' ', ' '},
			{'~', ' ', '*', ' ', '~'},
			{'~', '~', '~', ' ', '~'}	 
		};

		updateMap( view, 0, 0, WEST);

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

	//updates map given a view
	public void updateMap(char[][] view, int x, int y, int dir)
	{
		boolean coordsOpposite = false;
		int xdir = 1; int ydir = -1;

		switch (dir){
			case EAST:
				coordsOpposite = false;
				xdir = -1; ydir = -1;
			case SOUTH:
				coordsOpposite = false;
				xdir = -1; ydir = 1;
			case WEST:
				coordsOpposite = false;
				xdir = 1; ydir = 1;
		}

		for (int row = 0; row < 5; row++){
			for (int col = 0; col < 5; col++){
				int xGlobal = x + (col-2)*xdir;
				int yGlobal = y + (row-2)*ydir;
				Point2D.Double point = new Point2D.Double(xGlobal,yGlobal);

				char thing;
		 		if(!coordsOpposite){
		 			thing = view[col][row];
		 		} else
		 		{
		 			thing = view[row][col];
		 		}

		 		updateMapPoint(point, thing);
			}
		}
	}

	//Updates one point on the map at a time
	public void updateMapPoint(Point2D.Double point, Character thing)
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



    // Determines whether a space is empty
    public boolean isEmptySpace(Point2D point, boolean axe) {
		if (map.contains(point)) {
		    if (map.get(point).equals(' ')) {
		        return true;
		    } else if (map.get(point) == 'T' && axe) {
		    	return true;
		    }
		    return false;
    	}
    	return false;
    }

	public static void main(String[] args)
	{
		Map map = new Map();
		map.run();
	}
   
    //Removes a wall/tree using dynamite
    public void blow(Point2D point) {
        if (map.get(point) == 'T' || map.get(point) == '*') {
            map.put(point, ' ');
        } 
    }
    
    //Removes a wall using axe
    public void chop(Point2D point) {
        if (map.get(point) == 'T') {
            map.put(point, ' ');
        }
    }









}


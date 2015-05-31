import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class Map implements Runnable{

	private Hashtable<Point2D.Double, Character> map;
	private Search search;
	
	//save the location of useful things
	private Point2D.Double goldLocation;
	private LinkedList<Point2D.Double> dynamiteLocations;
	private LinkedList<Point2D.Double> axeLocations;
	private LinkedList<Point2D.Double> boatLocations; 

	//these save bounds of the map
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
		//this.search = new Search(map);
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

		System.out.println("MAP");
		updateMap( view, 0, 0, NORTH);
		printMap();

		isExplored(new Point2D.Double(0, 0));
	}


	public Point2D.Double getGoldLocation()
	{
		return goldLocation;
	}

	public Hashtable<Point2D.Double, Character> getCurMap()
	{
		return map;
	}
    

    public boolean acquireDynamite(Point2D.Double check) {
        char temp;
        temp = map.get(check);
        if (temp == 'd') {         
            return true;
        }
        return false;
    }

    public boolean acquireAxe(Point2D.Double check) {
        char temp;
        temp = map.get(check);
        if (temp == 'a') {         
            return true;
        }
        return false;
    }

    
	//updates map for the new view given by the agent. 
	public void updateMap(char[][] view, int x, int y, int dir)
	{
		/*boolean coordsOpposite = false;
		int xdir = 1; 
		int ydir = -1;

		switch (dir){
			case EAST:
				coordsOpposite = true;
				xdir = 1;
				ydir = 1;
				break;
			case SOUTH:
				coordsOpposite = false;
				xdir = -1; 
				ydir = 1;
				break;
			case WEST:
				coordsOpposite = true;
				xdir = -1; 
				ydir = -1;
				break;
		}

		for (int row = 0; row < 5; row++){
			for (int col = 0; col < 5; col++){
                if (row == 2 && col == 2) continue;
				int xGlobal = x + (col-2)*xdir;
				int yGlobal = y + (row-2)*ydir;
				Point2D.Double point = new Point2D.Double(xGlobal,yGlobal);
				char thing;
		 		if(!coordsOpposite){
		 			thing = view[row][col];
		 		} else
		 		{
		 			thing = view[col][row];
		 		}
                System.out.println("Updating " + point.getX() + " " + point.getY() + " with |" + thing + "|");
		 		updateMapPoint(point, thing);
			}
		}*/

        for (int row = -2; row < 3; row++){
		    for (int col = -2; col < 3; col++){
                if (row == 0 && col == 0) continue;
                
                Point2D.Double point = null;
                char thing = 't';

                switch (dir) {
                    case NORTH:
                        point = new Point2D.Double(x - row, y + col);
                        
                        break;

                    case EAST:
                        point = new Point2D.Double(y - row, x - col);
                        
                        break;

                    case SOUTH:
                        point = new Point2D.Double(x + row, y - col);
    
                        break;

                    case WEST:
                        point = new Point2D.Double(y - row, x - col);
                
                        break;        
                }
                
                thing = view[row+2][col+2];
                System.out.println("Updating " + point.getX() + " " + point.getY() + " with |" + thing + "|");
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

	//prints the current map
	public void printMap()
	{
		for(int y = (int)topLeft.getY(); y <= bottomRight.getY(); y--)
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

	// Determines if we can reach te gold.
    // We remove any restrictions on whether or not we use the dynamite to get to it as if we can get
    // to the gold it's game over. 
    // Updates the moves queue.
    public LinkedList<Character> isGoldReachable(Point2D.Double currLoc) {
    	// A* search through known parts of the map 

        return null;
    }
    
    // Determines whether or not we can get to some dynamite.
    // We place restrictions on using dynamite to get to it for now. This might be a mistake TODO
    // Updates the moves queue.
    public boolean isDynamiteReachable() { 
        return false;
    }

    // Determines whether or not the axe is reachable. 
    // We place on restrictions on using dynamite to get to it. This might be a mistake TODO
    // Updates the moves queue.
    public boolean isAxeReachable(){
        
        return false;
    } 

    public LinkedList<Move> isExplored(Point2D.Double currLoc){
    	

    	return null;
    }

    public boolean isExplored(){return false;};

    // Determines whether a space is empty
    public boolean isEmptySpace(Point2D.Double point, boolean axe) {
		if (map.containsKey(point)) {
            
            System.out.println("Contains " + point.getX() + " " + point.getY());
            char temp = map.get(point);
		    if (temp == ' ' || temp  == 'e'|| temp == 'a' || temp == 'd') { 
                System.out.println("Returning true " + temp + "yolo");
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
    public void blow(Point2D.Double point) {
        if (map.get(point) == 'T' || map.get(point) == '*') {
            map.put(point, ' ');
        } 
    }
    
    //Removes a wall using axe
    public void chop(Point2D.Double point) {
        if (map.get(point) == 'T') {
            map.put(point, ' ');
        }
    }

    //Takes in a linked list of points to visit and current orientation and returns the moves to get there.
    private LinkedList<Move> changePathToMoves(LinkedList<Point2D.Double> path, int orientation){
    	return null;
    }
}


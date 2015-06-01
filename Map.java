import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.lang.Math;
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

	//tools
	public static final int AXE = 0;
	public static final int DYNAMITE = 1;
	public static final int BOAT = 2;


	public Map()
	{
		this.map = new Hashtable<Point2D.Double, Character>();
		this.topLeft = new Point2D.Double(0, 0);
		this.bottomRight = new Point2D.Double(0, 0);

		Hashtable<Point2D.Double, Character> searchMap = (Hashtable<Point2D.Double, Character>)map.clone();
		Point2D.Double searchTl = (Point2D.Double) topLeft.clone();
		Point2D.Double searchBr = (Point2D.Double) bottomRight.clone();
		this.search = new Search(searchMap, searchTl, searchBr);
		
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
//		printMap();

		isExplored(new Point2D.Double(0, 0), NORTH);

		int items[] = {0, 0, 0, 0};
		LinkedList<Point2D.Double> temp = search.isPointReachable(new Point2D.Double(2, 0), new Point2D.Double(0, 0), NORTH, items);
        LinkedList<Move> temp2 = changePathToMoves(temp, NORTH);

        while (temp2.size() > 0) {
            Move temp3 = temp2.remove();
            System.out.print(temp3.getMove() + " ");
        
        }

        System.out.println("");



	}


	public Point2D.Double getGoldLocation()//possibly should be private
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
	public void updateMap(char[][] view, int inx, int iny, int dir)
	{
        
        Point2D.Double currPos = new Point2D.Double(inx, iny);

        // For each point received
        for(int x = 0; x < 5;x++){
        	double xGlobal = 0;
        	double yGlobal = 0;
             for(int y = 0; y < 5; y++)
        	{
                // A switch statment that maps relative view index to global index
        		switch (dir){
		        	case NORTH:
		        		xGlobal = currPos.getX()+(x - 2);
		        		yGlobal = currPos.getY()+(2 - y);
		        		break; 
		        	case SOUTH:
		        		xGlobal = currPos.getX()+(2 - x);
		        		yGlobal = currPos.getY()+(y - 2);
		        		break;
		        	case EAST:
		        		yGlobal = currPos.getY()+(2 - x);
		        		xGlobal = currPos.getX()+(2 - y);
		        		break;
		        	case WEST:
		        		yGlobal = currPos.getY()+(x - 2);
		        		xGlobal = currPos.getX()+(y - 2);
		        		break;
		        	default: 
		        		assert(false);
		        		break;
        		}

                // Add the element in at the right coordinate
	        	char thing = view[y][x];
	        	Point2D.Double point = new Point2D.Double(xGlobal, yGlobal);
	        	map.put(point ,thing);         

                // Update the top left and bottom right hand corners
	        	if(xGlobal < topLeft.getX())
	        		topLeft = new Point2D.Double(xGlobal, topLeft.getY());
	        	else if(xGlobal > bottomRight.getX())
	        		bottomRight = new Point2D.Double(xGlobal, bottomRight.getY());

	        	if(yGlobal > topLeft.getY())
	        		topLeft = new Point2D.Double(topLeft.getX(), yGlobal);
	        	else if(yGlobal < bottomRight.getY())
	        		bottomRight = new Point2D.Double(bottomRight.getX(), yGlobal);
        	}
        }
        
        printMap();
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
        System.out.print("+ ");
        for (int i = (int) topLeft.getX(); i <= bottomRight.getX(); i++) {
            System.out.print(Math.abs(i));
        }
        System.out.print(" +");
        System.out.println("");
		for(int y = (int)topLeft.getY(); y >= bottomRight.getY(); y--)
		{
		    System.out.print(Math.abs(y) + " ");

		    for(int x = (int)topLeft.getX(); x <= bottomRight.getX(); x++)
		    {
                Point2D.Double currentPos = new Point2D.Double(x, y);
				Character thing = map.get(currentPos);
   				if(thing == null)
   					System.out.print("?");
   				else 
   					System.out.print(thing);
   			}
		    System.out.print(" " + Math.abs(y));
   			System.out.println("");
   		}
   	    
        System.out.print("+ ");
        for (int i = (int) topLeft.getX(); i <= bottomRight.getX(); i++) {
            System.out.print(Math.abs(i));
        }
        System.out.print(" +");
        System.out.println("");

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
    public LinkedList<Move> isAxeReachable(Point2D.Double currLoc, int direction, int[] useableItems){

        return null;
    } 

    public boolean isAxeReachable()
    {
    	return false;
    }

    //Given the current location checks to see if the entirereachable area has been 
    //explored. The reachable area is defined as everywhere reachable from the current 
    // location without using any tools.
    //returns:  - null if the area has been explored
    //			- a linked list of moves to get to the closest unexplored area.
    public LinkedList<Move> isExplored(Point2D.Double currLoc, int orientation){
		search.updateMap(map, topLeft, bottomRight);
		LinkedList<Point2D.Double> pathToUnexplored = search.isExplored(currLoc);

		LinkedList<Move> movesToUnexplored = null;
		if(pathToUnexplored!= null)
			movesToUnexplored = changePathToMoves(pathToUnexplored, orientation);

    	return movesToUnexplored;
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
    
        // Keeps track of location
        Point2D.Double curLoc = path.remove();
        int curOrientation = orientation;

        // Create the return value    
        LinkedList<Move> retVal = new LinkedList<Move>();
   
        // While the queue is not empty
        while (path.size() > 0) {

            // Take off the first item off the queue
            Point2D.Double next = path.remove();

            // Determine which we way need to rotate (if at all) to get to it
            LinkedList<Move> temp = determineRotation(curLoc, next, curOrientation);
            while (temp.size() > 0) {
                Move curMove = temp.remove();
                switch (curMove.getMove()) {
                    case 'r':
                        curOrientation++;
                        if (curOrientation > 4) curOrientation = 1;
                        break;
                    case 'l':
                        curOrientation--;
                        if (curOrientation < 1) curOrientation = 4;
                        break;

                }     
                retVal.add(curMove);
            }
    
            // Move Forward
            Move forward = new Move('f');
            retVal.add(forward);

            // Repeat for next position
            curLoc = next;
        }
        
       return retVal; 
    }

    //Given two points and current orientation, returns the moves required to face "to"
    private LinkedList<Move> determineRotation (Point2D.Double cur, Point2D.Double to, int orientation){
       
        // Stores whether to is north, east, south or west of current
        int absoluteRelativePos = 0;
        if (to.getY() == (cur.getY()+ 1)) {
            absoluteRelativePos = NORTH;
        } else if (to.getX() == (cur.getX() + 1)) {
            absoluteRelativePos = EAST;
        } else if (to.getY() == (cur.getY() - 1)) {
            absoluteRelativePos = SOUTH;
        } else if (to.getX() == (cur.getX() - 1)) {
            absoluteRelativePos = WEST;
        } else {
            // Should not get to here
            assert(false);
        }

        LinkedList<Move> retVal = new LinkedList<Move>();
        Move temp = null;
        switch (orientation) {
        
            // If we are facing north (absolutely)
            case NORTH:
                switch (absoluteRelativePos) {
                    case NORTH:
                        // Just move forward
                        break;
                    case EAST:
                        // Rotate to the right
                        temp = new Move('r');
                        retVal.add(temp);
                        break; 
                    case SOUTH:
                        // Rotate twice
                        temp = new Move('r');
                        retVal.add(temp);
                        retVal.add(temp);
                        break;            
                    case WEST:
                        // Rotate to the left
                        temp = new Move('l');
                        retVal.add(temp);
                        break;
                }
                break;
            
            // If we are facing east (absolutely)
            case EAST:
                switch (absoluteRelativePos) {
                    case NORTH:
                        // Rotate to the left
                        temp = new Move('l');
                        retVal.add(temp);
                        break;
                    case EAST:
                        // Just move forward
                        break; 
                    case SOUTH:
                        // Rotate to the right
                        temp = new Move('r');
                        retVal.add(temp);
                        break;            
                    case WEST:
                        // Rotate 180
                        temp = new Move('l');
                        retVal.add(temp);
                        retVal.add(temp);
                        break;
                }
                break;

            // If we are facing south (absolutely)
            case SOUTH:
                switch (absoluteRelativePos) {
                    case NORTH:
                        // Rotate twice
                        temp = new Move('r');
                        retVal.add(temp);
                        retVal.add(temp); 
                        break;
                    case EAST:
                        // Rotate to the left
                        temp = new Move('l');
                        retVal.add(temp);
                        break; 
                    case SOUTH:
                        // Just move forward 
                        break;            
                    case WEST:
                        // Rotate to the right
                        temp = new Move('r');
                        retVal.add(temp);
                        break;
                }
                break;
            
            // If we are facing west (absolutely)
            case WEST:
                switch (absoluteRelativePos) {
                    case NORTH:
                        // Rotate to the right
                        temp = new Move('r');
                        retVal.add(temp);
                        break;
                    case EAST:
                        // Rotate 180
                        temp = new Move('r');
                        retVal.add(temp);
                        retVal.add(temp);
                        break; 
                    case SOUTH:
                        // Rotate Left
                        temp = new Move('l');
                        retVal.add(temp);
                        break;            
                    case WEST:
                        // Go Forward
                        break;
                }
                break;
            
        }
        return retVal;
    }
}


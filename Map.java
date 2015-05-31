import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class Map implements Runnable{

	private Hashtable<Point2D.Double, Character> map;
	
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

	//add this to search class
	private boolean adjExplored = false;

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

	//updates map for the new view given by the agent. 
	public void updateMap(char[][] view, int x, int y, int dir)
	{
		boolean coordsOpposite = false;
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

    // Determines whether or not the current "space" is explored.
    // In the case of a maze like arena, this returns true if each branch is explored without having
    // to cut anything down or blow anything up.
    // Updates the moves queue.
    public LinkedList<Move> isExplored(Point2D.Double currLoc) {
    	//bfs to find if any border points are free to move in w/o the
    	//use of axe, dynamite or boat
    	
    	//A queue that stores the paths to be searched
    	LinkedList<LinkedList<Point2D.Double>> nextPathQueue = new LinkedList<LinkedList<Point2D.Double>>();
    	LinkedList<Point2D.Double> pathToUnexplored = null; 

    	//start the first path in the queue with just currLoc
    	LinkedList<Point2D.Double> initPaths = new LinkedList<Point2D.Double>();
    	initPaths.add(currLoc); 
    	nextPathQueue.add(initPaths);

    	while(true){
    		//get the next path in the queue and location of the last point in the list
    		LinkedList<Point2D.Double> path = nextPathQueue.remove();
    		Point2D.Double point = path.getLast();

    		System.out.println("Exploring point: " + point);

    		//check if the adjacent points have been explored
    		adjExplored = false;
    		LinkedList<Point2D.Double> nextPoints = areAdjacentExplored(point);

	    	if(!adjExplored && (nextPoints != null)){
	    		//check areAdjecentExplored returned the expected
	    		if(nextPoints.size() == 1){ 
	    			path.add(nextPoints.getFirst());
	    			pathToUnexplored = path;
	    			System.out.println("\tAdjacent unexplored was found at point: " + nextPoints.getFirst());
	    		}else if (nextPoints.size() > 1){ 
	    			System.out.println("areAdjacentExplored returned more than one value for adjecentExplored = false");
	    		}else{
	    			System.out.println("areAdjacentExplored did not return a point for adjacentExplored = false");
	    		}

	    		//Break out of loop since an unexplored location has been found
	    		break; 
	    	}else{
	    		//add the points that can be visited next to the current path and add to the queue
	    		for(int i = 0; i < nextPoints.size(); i++)
	    		{
	    			LinkedList<Point2D.Double> newPath = (LinkedList)(path.clone());
	    			newPath.addLast(nextPoints.get(i));
	    			nextPathQueue.add(newPath);
	    			System.out.println("\tPoint added to next path: "+ nextPoints.get(i));
	    		}
	    	}
    	}	
    	
    	LinkedList<Move> movesToUnexplored = null;
    	if(pathToUnexplored!=null){
    		int orientation = NORTH; //FIX THIS
    		movesToUnexplored = changePathToMoves(pathToUnexplored, orientation);
    		System.out.println("The map was not explored!");
    	}else{
    		System.out.println("The map has been explored");
    	}

    	//return the linked list that this class passes back
        return movesToUnexplored;
    }

    public boolean isExplored(){
    	return false;
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

    //Takes in a current location and checks if all adjecent squares (north, south, east, west)
    // have been explored or not.
    // areThey - is a pointer to a boolean that will be changed to false if adjacent points have not
    // been explored and true of all have been explored
    // returns:  	- areThey == true returns a linked list of all the adjecent points
    // 					  that are moveable to
    //				- areThey == false returns a linked list of the first point found 
    // 					  point that is unexplored. 
    //NB: Currently prefers north, then east, south, west. 
    private LinkedList<Point2D.Double> areAdjacentExplored(Point2D.Double currLoc)
    {
    	System.out.println("areAdjacentExplored");
    	LinkedList<Point2D.Double> unexploredPoint = new LinkedList<Point2D.Double>();
    	LinkedList<Point2D.Double> nextPointsToVisit = new LinkedList<Point2D.Double>();

    	// Check if points around curr loc are moveable to, if so add to queue
    	Point2D.Double p[] = new Point2D.Double[4];
    	p[NORTH-1] = new Point2D.Double(currLoc.x, currLoc.y - 1); //north
    	p[SOUTH-1] = new Point2D.Double(currLoc.x, currLoc.y + 1); //south
    	p[EAST-1] = new Point2D.Double(currLoc.x + 1, currLoc.y); //east
    	p[WEST-1] = new Point2D.Double(currLoc.x - 1, currLoc.y); //west
    	
    	for (int i = 0; i < WEST; i++){
	    	//if any of the adjecent points equal null
	    	Character value = map.get(p[i]); 
	    	if(value == null){
	    		unexploredPoint.add(p[i]);
	    		adjExplored = false;
	    		System.out.println("\tUnexplored Point: " + p[i]);
	    		return unexploredPoint; 
	    	} else if ((value == ' ') || (value == 'a') || (value == 'd')){
	    		nextPointsToVisit.add(p[i]);
	    		System.out.println("\tPoints to visit next: " + p[i]);
	    	} else {
	    		System.out.println("\tCan't move to point: " + p[i]);
	    	}
	    }

	    //if program reaches here, all points must have been explored
	   	adjExplored = true; System.out.println("\tChanged areThey to true");
	   	return nextPointsToVisit;
    }

    //Takes in a linked list of points to visit and current orientation and returns the moves to get there.
    private LinkedList<Move> changePathToMoves(LinkedList<Point2D.Double> path, int orientation){
    	return null;
    }
}


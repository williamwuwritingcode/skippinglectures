
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class Search{
	private Hashtable<Point2D.Double, Character> map;
	private Point2D.Double topLeft;
	private Point2D.Double bottomRight;
	
	private boolean explored;
	private boolean adjExplored;
	private boolean adjSearched;

	//directions
	private static final int NORTH = 1;
	private static final int EAST = 2;
	private static final int SOUTH = 3;
	private static final int WEST = 4;

	//tools
	public static final int AXE = 0;
	public static final int DYNAMITE = 1;
	public static final int BOAT = 2;

	public static final int NOT_ALLOWED = -1;
	public static final int DONT_HAVE = 0;
	public static final int USEABLE = 1;

	//mini class that holds a path and an array of items that are 
	//available for use
	public class Route implements Cloneable
	{
		public LinkedList<Point2D.Double> path;
		public int[] useableItems; 

		public Route(LinkedList<Point2D.Double> path, int[] useableItems)
		{
			this.path = path;
			this.useableItems = useableItems;
		}

		public Route(Point2D.Double startPoint, int[] useableItems)
		{
			this.path = new LinkedList<Point2D.Double>();
			this.path.add(startPoint);

			this.useableItems = useableItems;
		}

		public Route clone()
		{
			return new Route((LinkedList<Point2D.Double>)this.path.clone(), this.useableItems.clone());
		}
	}

	public Search(Hashtable<Point2D.Double, Character> map, Point2D.Double topLeft, Point2D.Double bottomRight)
	{
		this.map = map;
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;

		this.explored = false;
		this.adjExplored = false;
	}

	//updates the search map with changes and new points
	public void updateMap(Hashtable<Point2D.Double, Character> newMap, Point2D.Double tl, 
		Point2D.Double br)
	{
		topLeft = tl;
		bottomRight = br;

		System.out.println("UpdateMap: tl =" + tl + " br =" +br);

		//should update map to hold values where question marks used to be, but 
		//not override areas we have explored
		for(double row = br.getY(); row <= tl.getY(); row++)
		{
			for(double col = tl.getX(); col <= br.getX(); col++)
			{
				Point2D.Double loc = new Point2D.Double(col, row);
				//the values at this point in both the maps
				Character new_thing = newMap.get(loc);
                Character old_thing = map.get(loc);
				
				//check if the point on the map is explored and new thing is not a boat.
				//In this case we don't want to update the point.
				boolean isExplored = false;
				
                if(old_thing != null){
					if((old_thing == 'e') && (new_thing != 'B')){
						isExplored = true;
                    }
				}

				if((new_thing != null)) {
					//check if old thing is not 'e'
					map.put(loc, new_thing);
                } else {
					System.out.println("Everything was null! (for " + loc + " )");
                }
			}
		}

		System.out.println("Updated Search Map");
		printMap();
        System.out.println("Returning from updateMap");
	}

	//Determines whether a point is reachable given a current location and an array of things it's 
	//allowed to use during the search.
	//returns: null - if the point is not reachable
	//         linkedList of Points for the fastest path to the point. 
	public LinkedList<Point2D.Double> isPointReachable(Point2D.Double destination, Point2D.Double currLoc, int direction, int[] useableItems)
	{	
		//NB:can optimise this by checking if the area around the point is marked 'e'
		System.out.println("Searching from:" + currLoc + " to " + destination);
		LinkedList<Route> nextRouteQueue = new LinkedList<Route>();
		LinkedList<Point2D.Double> pathToPoint = null;

    	//start the first path in the queue with just currLoc
    	Route initRoute = new Route(currLoc, useableItems.clone()); 
    	nextRouteQueue.add(initRoute);

    	while(nextRouteQueue.size() > 0){
    		//get the next path in the queue and location of the last point in the list
    		Route route = nextRouteQueue.remove();
    		Point2D.Double point = route.path.getLast();

    		System.out.println("Searching point: " + point);

    		//check if the adjacent points have been explored
    		adjSearched = false;
    		LinkedList<Route> nextRoutes = areAdjacentMoveable(route);

    		if(nextRoutes != null){
    			for(int i = 0; i < nextRoutes.size(); i++){
      				Route curRoute = nextRoutes.get(i);
      				//check if any of the routes we're adding go to the destination
    				if(curRoute.path.getLast() == destination){
    					pathToPoint = curRoute.path;
    					break;
    				}
					nextRouteQueue.add(curRoute);
    			}
    		}
    	}	
    	
    	//for testing
    	if(pathToPoint == null){
    		System.out.println("Point is not reachable!");
    	}else{
    		System.out.println("Point is reachable!");
    	}

    	return pathToPoint;
	}	


	// Determines whether or not the current "space" is explored.
    // In the case of a maze like arena, this returns true if each branch is explored without having
    // to cut anything down or blow anything up.
    // Updates the moves queue.
    public LinkedList<Point2D.Double> isExplored(Point2D.Double currLoc) {
    	
    	//check if the internal explored value is true, if so no need to search.
    	if (explored)
    		return null;
    	
    	//A queue that stores the paths to be searched
    	LinkedList<LinkedList<Point2D.Double>> nextPathQueue = new LinkedList<LinkedList<Point2D.Double>>();
    	LinkedList<Point2D.Double> pathToUnexplored = null; 

    	//start the first path in the queue with just currLoc
    	LinkedList<Point2D.Double> initPaths = new LinkedList<Point2D.Double>();
    	initPaths.add(currLoc); 
    	nextPathQueue.add(initPaths);

    	while(nextPathQueue.size() > 0){
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

	    	//mark that node explored so we don't visit it again (Don't do that if there is an axe, boats e.t.c)
    		if(map.get(point) == ' ') 
    			map.put(point, 'e');
    	}	
    	
    	if(pathToUnexplored!=null){
    		System.out.println("The map was not explored!");
    	}else{
    		System.out.println("The map has been explored");
    	}

    	//return the linked list that this class passes back
        return pathToUnexplored;
    }

    //returns an array that holds the map coordinates for all points adjacent to the current
    Point2D.Double[] getAdjacentPoints(Point2D.Double currLoc)
    {
    	Point2D.Double p[] = new Point2D.Double[4];
    	p[NORTH-1] = new Point2D.Double(currLoc.x, currLoc.y - 1); //north
    	p[SOUTH-1] = new Point2D.Double(currLoc.x, currLoc.y + 1); //south
    	p[EAST-1] = new Point2D.Double(currLoc.x + 1, currLoc.y); //east
    	p[WEST-1] = new Point2D.Double(currLoc.x - 1, currLoc.y); //west

    	return p;
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

    	Point2D.Double p[] = getAdjacentPoints(currLoc);
    	
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

    LinkedList<Route> areAdjacentMoveable(Route route){
    	Point2D.Double point = route.path.getLast();
    	LinkedList<Route> newRoutes = null;

    	Point2D.Double p[] = getAdjacentPoints(point);
    	for(int i = 0; i < WEST; i++)
    	{
    		Route newRoute = route.clone();
    		boolean moveSuccess = addPointToRoute(newRoute, p[i]);
    		
    		if(moveSuccess){
    			//if the new routes list hasn't been initialised, initialise it
    			if(newRoutes == null)
    				newRoutes = new LinkedList<Route>();

    			newRoutes.add(newRoute);
    		}
    	}
    	return newRoutes;
    }

    //Adds a point to a route. Will return true or false depending on if 
    //the route was updated
    boolean addPointToRoute(Route route, Point2D.Double point)
    {
    	boolean canMoveTo = false;
    	Character value = map.get(point);
    	if(value == null)
    		return false;


    	switch(value){
	    	//can always move to these points
			case ' ': case 'e': case 'B': case 'g':
				canMoveTo = true;
				break;

			//can move here if axe is useable 
			case 'T':
				if(route.useableItems[AXE] == USEABLE){
					canMoveTo = true;
				} 
				break;

			//can move here if dynamite is useable
			case '*':
				if(route.useableItems[DYNAMITE] >= USEABLE){
					route.useableItems[DYNAMITE] -= 1;
					canMoveTo = true;
				}
				break;

			//can move here if allowed to use the boat and are standing on a boat or
			// already on top of the water
			case '~':
				char curValue  = map.get(point);
				if((route.useableItems[BOAT] != NOT_ALLOWED) &&
					((curValue == 'B') || (curValue == '~'))){
						canMoveTo = true;
					}
				break;

			//can always move here. if axe is allowed ensure it is now set to useable
			case 'a':
				if(route.useableItems[AXE] != NOT_ALLOWED){
					route.useableItems[AXE] = USEABLE;
				}
				canMoveTo = true;
				break;

			//can always move here. If dynamite is allowed, ensure to update amount of available dynamite
			case 'd':
				if(route.useableItems[DYNAMITE] != NOT_ALLOWED){
					route.useableItems[DYNAMITE] += 1;
				}
				canMoveTo = true;
				break;
		}

		if(canMoveTo)
			route.path.addLast(point);

		return canMoveTo;
    }

    //prints the current map
	public void printMap()
	{
		for(int y = (int)topLeft.getY(); y >= bottomRight.getY(); y--)
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


}

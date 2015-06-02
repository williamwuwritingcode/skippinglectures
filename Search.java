
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class Search{
	private Hashtable<Point2D.Double, Character> map;
	private Hashtable<Point2D.Double, Character> toBeSearched;
    private Hashtable<Point2D.Double, Character> toBeMoved;
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
		this.toBeSearched = new Hashtable<Point2D.Double, Character>();
        this.toBeMoved = new Hashtable<Point2D.Double, Character>();
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
				explored = false;
				
                if(old_thing != null){
					if((old_thing == 'e') && (new_thing != 'B')){
						explored = true;
                    }
				}

				if(new_thing != null) {
					//check if old thing is not 'e'
					map.put(loc, new_thing);
                }
			}
		}

		printMap();
	}

	//Determines whether a point is reachable given a current location and an array of things it's 
	//allowed to use during the search.
	//returns: null - if the point is not reachable
	//         linkedList of Points for the fastest path to the point. 
	public LinkedList<Point2D.Double> isPointReachable(Point2D.Double destination, Point2D.Double currLoc, int direction, int[] useableItems)
	{	
		//NB:can optimise this by checking if the area around the point is marked 'e'
		System.out.println("Searching from "+ currLoc + " to "+destination);
        // Queue of routes. A route is a search state.
        LinkedList<Route> nextRouteQueue = new LinkedList<Route>();

        //if value is not a valid point, don't add it 
        toBeMoved = new Hashtable<Point2D.Double, Character>();

        // A path to the required point. Doesn't get updated if not reachable.
		LinkedList<Point2D.Double> pathToPoint = null;

        
        toBeMoved = new Hashtable<Point2D.Double, Character>();

    	//start the first path in the queue with just currLoc
    	Route initRoute = new Route(currLoc, useableItems.clone()); 
    	nextRouteQueue.add(initRoute);

        boolean temp = false;

        // While there are search states to be searched
    	while(nextRouteQueue.size() > 0){

    		//get the next path in the queue and location of the last point in the list
    		Route route = nextRouteQueue.remove();

    		// Check if the adjacent points are moveable 
    		adjSearched = false;
    		LinkedList<Route> nextRoutes = areAdjacentMoveable(route);

    		if(nextRoutes != null){
    			for(int i = 0; i < nextRoutes.size(); i++){
      				Route curRoute = nextRoutes.get(i);
      			    System.out.println("Last point added: "+ curRoute.path.getLast() + "destination: " + destination);	
                    //check if any of the routes we're adding go to the destination
    				if(curRoute.path.getLast().equals(destination)){
    					pathToPoint = curRoute.path;
    					temp = true;
                        break;
    				}
					nextRouteQueue.add(curRoute);
    			}
    		}
            if (temp) break;
    	}	
         	
        return pathToPoint;
	}	

    // Clears the Hashtable
    public void clear() {
        toBeSearched = new Hashtable<Point2D.Double, Character>();
        toBeMoved = new Hashtable<Point2D.Double, Character>();
    }

	// Determines whether or not the current "space" is explored.
    // In the case of a maze like arena, this returns true if each branch is explored without having
    // to cut anything down or blow anything up.
    // Updates the moves queue.
    public LinkedList<Point2D.Double> isExplored(Point2D.Double currLoc) {
    	
    	// Check if the internal explored value is true, if so no need to search.
    	if (explored)
    		return null;
        
    	
    	//A queue that stores the paths to be searched
        // A path is defined as a list of points
    	LinkedList<LinkedList<Point2D.Double>> nextPathQueue = new LinkedList<LinkedList<Point2D.Double>>();
    	
        // A path that leads to an unexplored point
        LinkedList<Point2D.Double> pathToUnexplored = null; 

    	// All paths start currLoc
    	LinkedList<Point2D.Double> initPaths = new LinkedList<Point2D.Double>();
    	initPaths.add(currLoc); 
    	nextPathQueue.add(initPaths);

        // While there are more paths to explore
    	while(nextPathQueue.size() > 0){

    		// Get the next path in the queue and location of the last point in the list
    		LinkedList<Point2D.Double> path = nextPathQueue.remove();
    		Point2D.Double point = path.getLast();

    		//check if the adjacent points have been explored
            //Put a hashtable or something here that tracks whether or not that point is already on 
            //the queue.
    		adjExplored = false;
    		LinkedList<Point2D.Double> nextPoints = areAdjacentExplored(point);

	        // If adjacent points not explored and can move into adjacent points
            if((!adjExplored) && (nextPoints != null)){

	    		//check areAdjecentExplored returned the expected
	    		if(nextPoints.size() == 1){ 
	    			path.add(nextPoints.getFirst());
	    			pathToUnexplored = path;
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
	    		}
	    	}

	    	//mark that node explored so we don't visit it again (Don't do that if there is an axe, boats e.t.c)
    	    if(!(map.get(point) == 'B')) {
    		    map.put(point, 'e');
            }               

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
    	
        // New Lists
        LinkedList<Point2D.Double> unexploredPoint = new LinkedList<Point2D.Double>();
    	LinkedList<Point2D.Double> nextPointsToVisit = new LinkedList<Point2D.Double>();

        //Get adjacent points
    	Point2D.Double p[] = getAdjacentPoints(currLoc);
    	
        // For each point surrounding the current point
    	for (int i = 0; i < WEST; i++){

            // If this point is in to be searched, then no point exploring it multiple times
            if (toBeSearched.containsKey(p[i])) continue;
	    	
            // If any of the adjecent points equal null. (a point = null means that we searched to
            // the end of the current map and don't know whats in the other space
	    	Character value = map.get(p[i]); 
	    	if(value == null){
   
                // Mark the point as unexplored
	    		unexploredPoint.add(p[i]);

                // Mark as at least one adjacent is not explored
	    		adjExplored = false;

                // Mark that this point will be explored so unnecessary to search in another path
                toBeSearched.put(p[i],'?');

	    		
                return unexploredPoint; 
	    	
            // We know what's in these coordinates, but not necessarily whats on the other side.
            } else if ((value == ' ') || (value == 'g') || (value == 'a') || (value == 'd') || (value == 'e')){
	    		
                // Mark the point as something we need to explore
                nextPointsToVisit.add(p[i]);
	    	
                // Mark that this point will be explored
                toBeSearched.put(p[i],map.get(p[i])); 

	    	
            // If not any of the options above, we can't move into these spots. 
	    
            }
        }
	    //if program reaches here, all points must have been explored
	   	adjExplored = true; 
	   	return nextPointsToVisit;
    }

    LinkedList<Route> areAdjacentMoveable(Route route){
    	Point2D.Double point = route.path.getLast();
    	LinkedList<Route> newRoutes = null;
       
        //Pretty sure the line below is a mistake
        //toBeMoved = new Hashtable<Point2D.Double, Character>();

    	Point2D.Double p[] = getAdjacentPoints(point);
    	//System.out.println("Checking points adjacent to: "+ point);
        
        // Put our current point on the list of checked 
        toBeMoved.put(point, 'x');
         
        for(int i = 0; i < WEST; i++)
    	{
            //System.out.println("before check toBeMoved");
            if (toBeMoved.containsKey(p[i])) { 
//              System.out.println("contains key");
                continue;
            } else {
                toBeMoved.put(p[i], '?');
            }
            
            //Might need to implement our own cloneable....TODO
            Route newRoute = route.clone();
    		boolean moveSuccess = addPointToRoute(newRoute, p[i]);
    		
    		if(moveSuccess){
                //System.out.println("\t point " + p[i] +  "added to route");
                
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
    	if(value == null) {
//    	    System.out.println("Shit");
            return false;
        }
        
//        System.out.println("Attempt adding: " + point +  "With value" + value  + "to route");
        Point2D.Double prev = route.path.getLast();

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
                    toBeSearched = new Hashtable<Point2D.Double, Character>();
					canMoveTo = true;
				}
				break;

			//can move here if allowed to use the boat and are standing on a boat or
			// already on top of the water
			case '~':
				char prevValue  = map.get(prev);
				if((route.useableItems[BOAT] != NOT_ALLOWED) &&
					((prevValue == 'B') || (prevValue == '~'))){
						canMoveTo = true;
					}
				break;

			//can always move here. if axe is allowed ensure it is now set to useable
			case 'a':
				if(route.useableItems[AXE] != NOT_ALLOWED){
					route.useableItems[AXE] = USEABLE;
                    toBeSearched = new Hashtable<Point2D.Double, Character>();
				}
				canMoveTo = true;
				break;

			//can always move here. If dynamite is allowed, ensure to update amount of available dynamite
			case 'd':
				if(route.useableItems[DYNAMITE] != NOT_ALLOWED){
					route.useableItems[DYNAMITE] += 1;
                    toBeSearched = new Hashtable<Point2D.Double, Character>();
				}
				canMoveTo = true;
				break;
		}

		if(canMoveTo){
//            System.out.println("Added!");
			route.path.addLast(point);
        }

		return canMoveTo;
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
}

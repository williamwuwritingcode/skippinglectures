import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class Search{
	private Hashtable<Point2D.Double, Character> map;
	
	private boolean explored;
	private boolean adjExplored;

	//directions
	private static final int NORTH = 1;
	private static final int EAST = 2;
	private static final int SOUTH = 3;
	private static final int WEST = 4;

	public void Search(Hashtable<Point2D.Double, Character> map)
	{
		this.map = map;

		this.explored = false;
		this.adjExplored = false;
	}

	public void updateMap(Hashtable<Point2D.Double, Character> newMap, Point2D.Double tl, 
		Point2D.Double br)
	{
		//should update map to hold values where question marks used to be, but 
		//not override areas we have explored
		for(double row = br.y; row < tl.y; row++)
		{
			for(double col = tl.x; col < br.x; col++)
			{
				Point2D.Double loc = new Point2D.Double(col, row);
				char new_thing = newMap.get(loc);
				char old_thing = map.get(loc);

				if(new_thing == 'B')
					map.put(loc, new_thing);
				else if(new_thing == ' ' && old_thing != 'e')
					map.put(loc, new_thing);
			}
		}

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
    	
    	if(pathToUnexplored!=null){
    		System.out.println("The map was not explored!");
    	}else{
    		System.out.println("The map has been explored");
    	}

    	//return the linked list that this class passes back
        return pathToUnexplored;
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

}
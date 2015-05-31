ublic class Search{
	private Hashtable<Point2D.Double, Character> map;

	public void Search(Hashtable<Point2D.Double, Character> map)
	{
		this.map = map;
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
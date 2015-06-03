import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.lang.Math;
public class Map {

	private Hashtable<Point2D.Double, Character> map;
	private Search search;
	
	//save the location of useful things
	private Point2D.Double goldLocation;
	private LinkedList<Point2D.Double> dynamiteLocations;
	private LinkedList<Point2D.Double> axeLocations;
	private LinkedList<Point2D.Double> boatLocations; 
    private LinkedList<Point2D.Double> treeLocations;
    

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

    //permissions to use tools
    public static final int NOT_ALLOWED = -1;
    public static final int DONT_HAVE = 0;
    public static final int USEABLE = 1;

	public Map()
	{
		this.map = new Hashtable<Point2D.Double, Character>();
		this.topLeft = new Point2D.Double(0, 0);
		this.bottomRight = new Point2D.Double(0, 0);

        //create a search object and give it a copy of the map
		Hashtable<Point2D.Double, Character> searchMap = (Hashtable<Point2D.Double, Character>)map.clone();
		Point2D.Double searchTl = (Point2D.Double) topLeft.clone();
		Point2D.Double searchBr = (Point2D.Double) bottomRight.clone();
		this.search = new Search(searchMap, searchTl, searchBr);
		
		this.goldLocation = null;
		this.dynamiteLocations = new LinkedList<Point2D.Double>();
		this.axeLocations = new LinkedList<Point2D.Double>();
		this.boatLocations = new LinkedList<Point2D.Double>();
        this.treeLocations = new LinkedList<Point2D.Double>();
    }

    
    // Clears the search hashtable to make sure that shit works.
    public void clearSearch() {
        search.clear();        
    }
    

    // Updates the map to reflect a dynamite has been picked up
    public boolean acquireDynamite(Point2D.Double check) {
        char temp;
        //check the current location has an axe
        temp = map.get(check);
        if (temp == 'd') {         
            return true;
        }
        return false;
    }

    // Updates the map to reflect a dynamite has been picked up
    public boolean acquireAxe(Point2D.Double check) {
        char temp;
        //check the current location has an axe
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
                //if it is the agents' current location, fill the map with a space
                if (x == 2 && y == 2) {
                    xGlobal = currPos.getX();
                    yGlobal = currPos.getY();
                    Point2D.Double point = new Point2D.Double(xGlobal, yGlobal);
                    map.put(point, ' ');
                    continue;
                }

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
	        	updateMapPoint(point,thing);         

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
	}

	//Updates one point on the map at a time
	public void updateMapPoint(Point2D.Double point, Character thing)
	{
        //add the character to the map
		map.put(point, thing);

		//Update the saved location of useful items
		if(thing.equals('g')) {
			this.goldLocation = point;
        } else if (thing.equals('d')) {
			this.dynamiteLocations.add(point);
        } else if (thing.equals('B')) {
			this.boatLocations.add(point);
        } else if (thing.equals('a')) {
			this.axeLocations.add(point);
        } else if (thing.equals('T')) {
            this.treeLocations.add(point);
        }

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

	// Finds a list of moves the agent should make to get to a tree and cut it down.
    // If there are no reachable trees null will be returned.
    public LinkedList<Move> cutDownTree(Point2D.Double currLoc, int direction){
  	
        LinkedList<Move> moves = null;
        
		search.updateMap(map, topLeft, bottomRight);
        int[] items = new int[3];
        items[AXE] = 1;
        items[DYNAMITE] = NOT_ALLOWED;
        items[BOAT] = 0; 
            
        // Make sure there are trees
        if (treeLocations.size() > 0) {
            for (int i = 0; i < treeLocations.size(); i++) {
                Point2D.Double tree = treeLocations.get(i);
                LinkedList<Point2D.Double> path = search.isPointReachable(tree, currLoc, direction, items);
                if (!(path == null)) {
                    treeLocations.remove(i);
                    moves = changePathToMoves(path, direction);
                } 
            } 
        }
        return moves;
    }
    
    //prints the current map
	public void printMap()
	{
        //put plusses in the corners
        System.out.print("+ ");

        //print grid locations at the top of the map
        for (int i = (int) topLeft.getX(); i <= bottomRight.getX(); i++) {
            System.out.print(Math.abs(i));
        }
        System.out.print(" +");
        System.out.println("");

        //print out the map line by line from top left to bottom right
		for(int y = (int)topLeft.getY(); y >= bottomRight.getY(); y--)
		{
            //Print y coordinate for each line
		    System.out.print(Math.abs(y) + " ");

		    for(int x = (int)topLeft.getX(); x <= bottomRight.getX(); x++)
		    {
                //print what is at each map location. If nogthing a question mark will be printed
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
   	    
        //plusses in the corner and grid locations along the bottom
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
    // Returns a list of move to take to reach the gold
    public LinkedList<Move> isGoldReachable(int orientation, Point2D.Double currLoc, int[] items) {   	
        LinkedList<Move> moves = null;
        
		search.updateMap(map, topLeft, bottomRight);
        
        // check if we know where the gold is
        if(goldLocation != null){
            //BFS to find the gold
            LinkedList<Point2D.Double> path = search.isPointReachable(goldLocation, currLoc, orientation, items);
            if(path != null){
                moves = changePathToMoves(path, orientation);
            } 
        }
        return moves;
    }
    
    // Determines whether or not we can get to some dynamite.
    // We place restrictions on using dynamite to get to it for now. This might be a mistake TODO
    // Returns a list of moves to take to reach the dynamite null if no dynamite is reachable
    public LinkedList<Move> isDynamiteReachable(int orientation, Point2D.Double currLoc, int[] items) { 
        LinkedList<Move> moves = null;
        LinkedList<Point2D.Double> path = null;
		search.updateMap(map, topLeft, bottomRight);

        //BFS for each dynamite location saved until dynamite is reached.
        for(int i = 0; i < dynamiteLocations.size(); i++){
            path = search.isPointReachable(dynamiteLocations.get(i), currLoc, orientation, items);
            if(path != null) break;
        }

        if(path != null){
            moves = changePathToMoves(path, orientation);
        }

        return moves;
    }

    // Determines whether or not the axe is reachable., given the premissions for tools in useableItems. 
    // Returns a list of moves to make to reach acquire the axe, null if there is no reachable axe.
    public LinkedList<Move> isAxeReachable(Point2D.Double currLoc, int direction, int[] useableItems){
        LinkedList<Move> moves = null;
        LinkedList<Point2D.Double> path = null;

		search.updateMap(map, topLeft, bottomRight);
        
        for(int i = 0; i < axeLocations.size(); i++){
            path = search.isPointReachable(axeLocations.get(i), currLoc, direction, useableItems);
            if(path != null) break;
        }

        if(path != null){
            moves = changePathToMoves(path, direction);
        }

        return moves;
    } 

    // Returns a list of moves to take the agent back to the start
    public LinkedList<Move> goHome(Stack<Move> movesTaken) {
        Move temp = null;
        Move newMove = null;
        LinkedList<Move> retVal = new LinkedList<Move>();
        temp = new Move('l');
        retVal.add(temp);
        retVal.add(temp);
        while (!movesTaken.empty()){
            temp = movesTaken.pop(); 
            switch (temp.getMove()) {
                case 'l':
                    newMove = new Move('r');
                    retVal.add(newMove);
                    break;
                case 'r':
                    newMove = new Move('l');
                    retVal.add(newMove);
                    break;
                case 'f':
                    newMove = new Move('f');
                    retVal.add(newMove);
                    break;
            }
        }
        return retVal;
    }

    // Return Gold Location
    public Point2D.Double getGoldLocation() {
        return goldLocation;
    }

    //Given the current location checks to see if the entire reachable area has been 
    //explored. The reachable area is defined as everywhere reachable from the current 
    // location without using any tools.
    //returns:  - null if the area has been explored
    //			- a linked list of moves to get to the closest unexplored area.
    public LinkedList<Move> isExplored(Point2D.Double currLoc, int orientation) {
		search.updateMap(map, topLeft, bottomRight);
		LinkedList<Point2D.Double> pathToUnexplored = search.isExplored(currLoc);

		LinkedList<Move> movesToUnexplored = null;
		if(pathToUnexplored!= null)
			movesToUnexplored = changePathToMoves(pathToUnexplored, orientation);

    	return movesToUnexplored;
    }


    // Determines whether a space is empty
    public boolean isEmptySpace(Point2D.Double point, boolean axe) {
		if (map.containsKey(point)) {

            char temp = map.get(point);
		    if (temp == 'g' || temp == ' ' || temp  == 'e'|| temp == 'a' || temp == 'd') { 
                return true;
		    } else if (map.get(point) == 'T' && axe) {
		        //the space is considered empty if there is a tree there and we have an axe. 
            	return true;
		    }
		    return false;
    	}
        //if the map  does not contain the key default to false
    	return false;
    }
    
    //Removes a wall/tree using dynamite
    public void blow(Point2D.Double point) {
        if (map.get(point) == 'T' || map.get(point) == '*') {
            map.put(point, ' ');
        } 
    }
    
    //Removes a tree using axe
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

            if (map.get(next) == null){
                break;
            }else if (map.get(next) == 'T') {
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
                retVal.add(new Move('c')); 
            } else if (map.get(next) == '*') {
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
                retVal.add(new Move('b'));
            }else{
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


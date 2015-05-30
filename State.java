import java.util.*;
import java.awt.geom.Point2D;
import java.io.*;

public class State {
    
    public Map originalMap;
    private ArrayList<Move> movesTaken[]; // Need this to get back to the start
    private Boolean axe; // Determines whether we have an axe or not
    private int dynamite; // Determines how many dynamites we have
    private Point2D curLocation; // Current Location
    private Stack unexplored; // ?
    private Queue movesToDo; // Current queue of moves to be executed. This is automic.   
    private int direction; //(used as an enum: 1 = up, 2 = right, 3 = down, 4 = left (clockwise from up position));
    private List removedStuff; // Contains the points of walls/trees which have been removed

    public State(){
    	originalMap = new Map();
        direction = 0;
        curLocation = new Point2D.Double(0,0);
        dynamite = 0;
        axe = false;
        removedStuff = new ArrayList<Point2D>();
        movesTaken = new ArrayList<Move>();
        unexplored = new Stack();
        movesToDo = new LinkedList<Move>();
    }

    public Double getCurX() {
        return curLocation.getX()
    }

    public Double getCurY() {
        return curLocation.getY();
    }
    
    public ArrayList<Move> makeMove() {
        Move temp = movesToDo.poll();
        if (temp != null) {
            return temp;
        }

        if (isGoldReachable()) {
             temp = movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (isAxeReachable()) {
             temp = movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (isDynamiteReachable()) {
             temp = movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (isExplored()) {
             temp = movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (canMoveForward()) {
             temp = movesToDo.poll();
             assert(temp != null);
             return temp;
        }


        rotateToAppropriateOrientation();
        temp = movesToDo.poll();
        assert(temp != null);
        return temp;

    }

    private boolean isGoldReachable() {

    }
    
    private boolean isDynamiteReachable() 

    }

    private boolean isAxeReachable(){
        if (axe){
            return false;
        }
    } 

    private boolean isExplored() {


    }

    private boolean canMoveForward() {
		return originalMap.removeStuff(removedStuff).isValidForward(forwardPoint, axe);
    } 

    private void rotateToAppropriateOrientation() {

    }

    //Takes in the move that's about to be performed and updates the currrent state appropriately
    private void adjustState(Move thing) {
        switch (thing.getMove) {
        
        //Case of turning right
        case 'r': case 'R':
            if (direction == 4) {   
               direction = 1;     
            } else {
               direction++;
            }
        	break;
        
        //Case of trying to turn left
        case 'l': case 'L':
            if (direction == 1) {   
               direction = 4;     
            } else {
               direction--;
            }
			break;
		
		//Case of trying to move forward	
        case 'f': case 'F':
           curLocation = getPointInFront();
        
        //Case of trying to chop down	
        case 'C': case 'c':
		    
            //Get location in front
            Point2D inFront = getPointInFront();

            //Chop down the point in front
			removedStuff.add(inFront);

            break;
		
		//Case of trying to blow stuff up
		case 'b': case 'B':
	        //Get location in front
            Point2D inFront = getPointInFront();

            //Blow it up
            removedStuff.add(inFront);
            dynamite--;    
			break;
		
		}	
        assert(direction > 0 && direction < 5); //Sanity Check
        
        //update movesTaken
        movesTaken.add(Move);

        return;
    }
}

public int getDirection() {
    return direction;
}

private Point2D getPointInFront() {

    Point2D forwardPoint = null;        
    switch (direction) {
    case 1:
        forwardPoint = new Point2D(curLocation.getX(), curLocation.getY() + 1);
        break;
    case 2:
        forwardPoint = new Point2D(curLocation.getX() + 1, curLocation.getY()); 
        break;
    case 3:
        forwardPoint = new Point2D(curLocation.getX(), curLocation.getY() - 1); 
        break;
    case 4:
        forwardPoint = new Point2D(curLocation.getX() - 1, curLocation.getY()); 
        break;
    default:
        assert(false); //Error if it gets to here - Sanity Check
        break;
   	} 
    return forwardPoint;
    
}


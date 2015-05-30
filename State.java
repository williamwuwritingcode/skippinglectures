import java.util.*;
import java.awt.geom.Point2D;
import java.io.*;

public class State {
    
    private Map originalMap; // The original map
    private ArrayList<Move> movesTaken[]; // Need this to get back to the start
    private Boolean axe; // Determines whether we have an axe or not
    private int dynamite; // Determines how many dynamites we have
    private Point2D curLocation; // Current Location
    private Stack unexplored; // ?
    private Queue movesToDo; // Current queue of moves to be executed. This is automic.   
    private int direction; //(used as an enum: 1 = up, 2 = right, 3 = down, 4 = left (clockwise from up position));
    
    public State(){
    	curMap = new Map();
        direction = 0;
        curLocation = new Point2D.Double(0,0);
        dynamite = 0;
        axe = false;
        movesTaken = new ArrayList<Move>();
        unexplored = new Stack();
        movesToDo = new LinkedList<Move>();
    }

    //Updates the map
    public void updateCurMap (char view[][]) {
        curMap.updateMap(view, curLocation.getX(), curLocation.getY(), direction);
    } 

    // Called by agent method to determine what move to make next.
    // Operates by checking to see if the move queue is empty and if so, determines what moves
    // should be made next. Other wise it pulls the next item off the queue.
    public ArrayList<Move> makeMove() {
        
        // Are there any other moves?
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

    // Determines if we can reach te gold.
    // We remove any restrictions on whether or not we use the dynamite to get to it as if we can get
    // to the gold it's game over. 
    // Updates the moves queue.
    private boolean isGoldReachable() {

    }
    
    // Determines whether or not we can get to some dynamite.
    // We place restrictions on using dynamite to get to it for now. This might be a mistake TODO
    // Updates the moves queue.
    private boolean isDynamiteReachable() 

    }

    // Determines whether or not the axe is reachable. 
    // We place on restrictions on using dynamite to get to it. This might be a mistake TODO
    // Updates the moves queue.
    private boolean isAxeReachable(){
        if (axe){
            return false;
        }
    } 

    // Determines whether or not the current "space" is explored.
    // In the case of a maze like arena, this returns true if each branch is explored without having
     // to cut anything down or blow anything up.
    // Updates the moves queue.
    private boolean isExplored() {


    }

    // Determines whether or not we can move forward.
    // Updates the moves queue.
    private boolean canMoveForward() {
        
        Point2D forwardPoint = getPointInFront();

        // Send in empty because we don't want to consider using an axe in this case
        if (curMap.isEmptySpace(forwardPoint, false)) { 
            return true;     
        }
        
        return false;
    } 

    // Determines which way we should rotate when we can't move forward
    // Updates the moves queue.
    private void rotateToAppropriateOrientation() {
        
        // go left then right -< arbitrarily assigned.
        Point2D left = getPointToLeft();
        Point2D right = getPointToRight(); 

        if (curMap.isEmptySpace(left, false)) {
            movesToDo.add(new Move('L'));
            return;
        } else if (curMap.isEmptySpace(right,false) {
            movesToDo.add(new Move('R'));
            return;
        }
        
        // go backwards if only option
        Move turnAround = new Move("L");
        movesToDo.add(turnAround);
        movesToDo.add(turnAround);
        return;   
    }

    //Takes in the move that's about to be performed and updates the currrent state appropriately
    //We don't do much error checking here as we assume the other methods do that for us
    private void adjustState(Move thing) {
        
        //Get location in front
        Point2D inFront = getPointInFront();

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
           curLocation = inFront;
            
           break; 
        
        //Case of trying to chop down	
        case 'C': case 'c':            
            curMap.chop(inFront);
            break;
		
		//Case of trying to blow stuff up
		case 'b': case 'B':
            
            //Blow it up
            curMap.blow(inFront);
            dynamite--;    
			break;
		
		}	
        assert(direction > 0 && direction < 5); //Sanity Check
        
        //Update movesTaken
        movesTaken.add(Move);
        
        //Print the map
        curMap.printMap();
        
        return;
    }
}

// Gets the current direction
public int getDirection() {
    return direction;
}

// Returns a poin2d representing the square directly in front
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

private Point2D getPointToLeft() {

    Point2D forwardPoint = null;        
    switch (direction) {
    case 1:
        forwardPoint = new Point2D(curLocation.getX() -  1, curLocation.getY());
        break;
    case 2:
        forwardPoint = new Point2D(curLocation.getX(), curLocation.getY() + 1); 
        break;
    case 3:
        forwardPoint = new Point2D(curLocation.getX() + 1, curLocation.getY()); 
        break;
    case 4:
        forwardPoint = new Point2D(curLocation.getX(), curLocation.getY() - 1); 
        break;
    default:
        assert(false); //Error if it gets to here - Sanity Check
        break;
   	} 
    return forwardPoint;
}

private Point2D getPointToRight() {

    Point2D forwardPoint = null;        
    switch (direction) {
    case 1:
        forwardPoint = new Point2D(curLocation.getX() + 1, curLocation.getY());
        break;
    case 2:
        forwardPoint = new Point2D(curLocation.getX(), curLocation.getY() + 1); 
        break;
    case 3:
        forwardPoint = new Point2D(curLocation.getX() - 1, curLocation.getY()); 
        break;
    case 4:
        forwardPoint = new Point2D(curLocation.getX(), curLocation.getY() + 1); 
        break;
    default:
        assert(false); //Error if it gets to here - Sanity Check
        break;
   	} 
    return forwardPoint;

}



























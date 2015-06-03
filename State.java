/**
* The program has three main classes. State, Map and Search. As the player explores the map, the state will be updated 
* with orientation and location information and the map will be updated with the current view. The Map holds a hashtable
* which uses points as keys, the  point of origin is the maps (0, 0). 
*
* When deciding on what move to make the State will ask the map if certain objects are reachable or the map has been 
* explored in a prioritised order. The map will call the search to 'search' for the object asked of, a breadth first search 
* is performed by the search object to determine which points on the map are reachable from a certain location. The search
* will be given an array of permissions which outlines which tools the agent is allowed to uise along its' journey to a given point. 
* the map object will convert the points in a path to the destination that the search has supplied into an array of moves. The moves
* will be stored by the state and popped off a queue each time bounty asks for a move. 
*
*/




import java.util.*;
import java.awt.geom.Point2D;
import java.io.*;

public class State {
    
    private Map curMap; // The original map
    private Stack<Move> movesTaken; // Need this to get back to the start
    private Boolean axe; // Determines whether we have an axe or not
    private int dynamite; // Determines how many dynamites we have
    private int boat;
    private Point2D.Double curLocation; // Current Location
    private Queue<Move> movesToDo; // Current queue of moves to be executed. This is automic.   
    private int direction; //(used as an enum: 1 = up, 2 = right, 3 = down, 4 = left (clockwise from up position));
    private boolean gold;

    //tools
    public static final int AXE = 0;
    public static final int DYNAMITE = 1;
    public static final int BOAT = 2;

    //permissions to use tools
    public static final int NOT_ALLOWED = -1;
    public static final int DONT_HAVE = 0;
    public static final int USEABLE = 1;
    
    public State(){
    	curMap = new Map();
        direction = 1;
        gold = false;
        curLocation = new Point2D.Double(0,0);
        dynamite = 0;
        boat = 0;
        axe = false;
        movesTaken = new Stack<Move>();
        movesToDo = new LinkedList<Move>();
    }
 
    //Takes in the move that's about to be performed and updates the currrent state appropriately
    //We don't do much error checking here as we assume the other methods do that for us
    public void adjustState(Move thing) {
        
        //Get location in front
        Point2D.Double inFront = getPointInFront();
        switch (thing.getMove()) {
        
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
           
           // See if forward move is valid. Not actually required when we run the AI since it checks.
           if (curMap.isEmptySpace(inFront, false)) {
                curLocation = inFront;
           }
            
           // See if we picked up an axe or dynamite
           if (curMap.acquireDynamite(inFront)) {
                dynamite++;                  
           } else if (curMap.acquireAxe(inFront)) {
                axe = true;
           }
           break; 
        
        //Case of trying to chop down	
        case 'C': case 'c':            
            curMap.chop(inFront);
            break;
		
		//Case of trying to blow stuff up
		case 'b': case 'B':
            curMap.blow(inFront);
            dynamite--;    
			break;
		}	
        assert(direction > 0 && direction < 5); //Sanity Check
        
        //Update movesTaken
        movesTaken.push(thing);

        return;
    }


    //Updates the map
    public void updateCurMap (char view[][]) {
        curMap.updateMap(view, (int)curLocation.getX(), (int)curLocation.getY(), direction);
    } 

    // Called by agent method to determine what move to make next.
    // Operates by checking to see if the move queue is empty and if so, determines what moves
    // should be made next. Other wise it pulls the next item off the queue.
    public Move makeMove() {
        
        // Are there any moves still in the move queue
        Move temp = movesToDo.poll();
        if (temp != null) {
            // If the move is to go forward and forward means going into water w/o boat,
            // cancel the move.
            if (!(!curMap.isEmptySpace(getPointInFront(), false) && temp.getMove() =='f')) {
                return temp;
            }
        }
        
        //If the agent has the gold, go back to the start position       
        if (gold) {
            goHome();
            temp = (Move) movesToDo.poll();
            assert(temp != null);
            return temp;
        }

        //If the agent can reach the gold, knowing whats on the current map, go there
        if (isGoldReachable()) {
             temp = (Move) movesToDo.poll();
             gold = true;
             assert(temp != null);
             return temp;
        }
        
        if (!axe) {
            //if the agent doen't have an axe, check if it can aquire one, if so, acquire it
            if (isAxeReachable()) {
                temp = (Move)movesToDo.poll();
                assert(temp != null);
                return temp;
            }
        }

        //check if the agent can reach some dynamite, if so acquire it.
        if (isDynamiteReachable()) {
             temp = (Move) movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        //check if the entire map has been explored without the use of any tools
        if (!isExplored()) {
             temp = (Move) movesToDo.poll();
             assert(temp != null);
             return temp;
        
        //Agent has little choices anymore.
        } else {
            // So see if the agent can reach an axe via the use of dynamite.
            //This will open more of the board up to be explored
            if (!axe) {
                if (isAxeReachableWithDynamite()) {
                    temp = (Move) movesToDo.poll();
                    assert(temp != null);
                    return temp;
                }

            //Agent will cut down a tree. This will open more of the board up for exploration
            } else {
                cutDownTree();
                temp = (Move) movesToDo.poll();
                assert(temp != null);
                return temp;
            }
        }
        assert(false); // should never get to here
        return temp;
    }


    // Determine if we can reach te gold.
    // We remove any restrictions on whether or not we use the dynamite to get to it as if we can get
    // to the gold it's game over. 
    // Updates the moves queue.
    private boolean isGoldReachable() { 
        // If we haven't found gold yet, no point searching for it.
        Point2D.Double goldLoc = curMap.getGoldLocation();
        if (goldLoc == null) {
            return false;
        }

        //set permissions for tool user during the breadth first search
        int[] items = new int[3];

        if(axe) {
            items[AXE] = USEABLE;
        } else {
            items[AXE] = DONT_HAVE;
        }

        items[DYNAMITE] = dynamite;
        items[BOAT] = boat;
        LinkedList<Move> moves = curMap.isGoldReachable(direction, curLocation, items);

        //see if there are any moves
        if (moves == null) {
            return false;
        } else {
            //add the moves returned to the moves queue
            while (moves.size() > 0) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }
    }
    
    // Determines whether or not we can get to some dynamite.
    // We place restrictions on using dynamite to get to it for now. 
    // Updates the moves queue.
    private boolean isDynamiteReachable() {
        //Set permissions for the use of tools
        int[] items = new int[3];

        if(axe) items[AXE] = USEABLE;
        else items[AXE] = DONT_HAVE;

        items[DYNAMITE] = NOT_ALLOWED;
        items[BOAT] = boat;

        //ask the map if dynamite is reachable using permissions above
        LinkedList<Move> moves = curMap.isDynamiteReachable(direction, curLocation, items);         

        //check if the map returned moves to reach the dynamite
        if (moves == null) {
            return false;
        }else{
            //add the moves to the moves queue
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }
    }

    // Determines whether or not the axe is reachable. 
    // We place on restrictions on using dynamite to get to it. 
    // Updates the moves queue.
    private boolean isAxeReachable(){
        //Set permissions for the use of tools.
        int[] items = new int[3];
        items[AXE] = DONT_HAVE;
        items[BOAT] = boat;
        items[DYNAMITE] = NOT_ALLOWED;
        
        //Ask the map if axe is reachable
        LinkedList<Move> moves = curMap.isAxeReachable(curLocation, direction, items );         

        //check if the map returned moves to mreach the axe
        if (moves == null) {
            return false;
        }else{
            //add the moves to the move queue
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }

    } 

    // Use dynamite to check to see if we can get axe.
    private boolean isAxeReachableWithDynamite() {
        //Sert permissions for the use of tools.
        int[] items = new int[3];
        items[AXE] = 0;
        items[BOAT] = boat;
        items[DYNAMITE] = dynamite;

        //Ask the map if axe is reachable
        LinkedList<Move> moves = curMap.isAxeReachable(curLocation, direction, items);         

        //check if the map returned moves to the axe
        if (moves == null) {
            return false;
        }else{
            // add the moves to the queue
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }        

    }

    //Determines if the entire reachable area has been explored. The reachable area is 
    // defined as everywhere reachable from the current location without using any tools.
    private boolean isExplored() {

        //ask the map if the area has been explored
        LinkedList<Move> moves = curMap.isExplored(curLocation, direction);

        //check if the map returned any moves
        if(moves != null){
            //add the moves to the queue
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return false;
        }
        return true;
    }

    // finds a list of moves the agent must take in order to cut down a tree. (No
    //specific tree is defined)
    private boolean cutDownTree() {
        //ask the map for move to cut down a tree
        LinkedList<Move> moves = curMap.cutDownTree(curLocation, direction);
        
        //see if the map returned any moves
        if(moves != null){
            //add the moves top the move queue
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return false;
        }
        return true;
    }
    
    // We found the gold, let's finish this
    private void goHome() {
        //Go Back in reverse order
        LinkedList<Move> moves = curMap.goHome(movesTaken);

        //check the map returned moves to home
        if(moves != null){
            //add the moves to the move queue
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }   
        } else {
            assert(false);
        } 
    }

    // Returns a poin2d representing the square directly in front
    private Point2D.Double getPointInFront() {

        Point2D.Double forwardPoint = null;        
        switch (direction) {
        case 1:
            forwardPoint = new Point2D.Double((int) curLocation.getX(),(int) (curLocation.getY() + 1));
            break;
        case 2:
            forwardPoint = new Point2D.Double((int)(curLocation.getX() + 1), (int) curLocation.getY()); 
            break;
        case 3:
            forwardPoint = new Point2D.Double((int) curLocation.getX(),(int) ( curLocation.getY() - 1)); 
            break;
        case 4:
            forwardPoint = new Point2D.Double((int)(curLocation.getX() - 1),(int) curLocation.getY()); 
            break;
        default:
            assert(false); //Error if it gets to here - Sanity Check
            break;
   	    }

        return forwardPoint;
    
    }

    // Returns a poin2d representing the square directly in front
    private Point2D.Double getPointToLeft() {

        Point2D.Double forwardPoint = null;        
        switch (direction) {
        case 1:
            forwardPoint = new Point2D.Double(curLocation.getX() -  1, curLocation.getY());
            break;
        case 2:
            forwardPoint = new Point2D.Double(curLocation.getX(), curLocation.getY() + 1); 
            break;
        case 3:
            forwardPoint = new Point2D.Double(curLocation.getX() + 1, curLocation.getY()); 
            break;
        case 4:
            forwardPoint = new Point2D.Double(curLocation.getX(), curLocation.getY() - 1); 
            break;
        default:
            assert(false); //Error if it gets to here - Sanity Check
            break;
   	    } 
        return forwardPoint;
    }

    // Returns a poin2d representing the square directly in front
    private Point2D.Double getPointToRight() {

        Point2D.Double forwardPoint = null;        
        switch (direction) {
        case 1:
            forwardPoint = new Point2D.Double(curLocation.getX() + 1, curLocation.getY());
            break;
        case 2:
            forwardPoint = new Point2D.Double(curLocation.getX(), curLocation.getY() + 1); 
            break;
        case 3:
            forwardPoint = new Point2D.Double(curLocation.getX() - 1, curLocation.getY()); 
            break;
        case 4:
            forwardPoint = new Point2D.Double(curLocation.getX(), curLocation.getY() + 1); 
            break;
        default:
            assert(false); //Error if it gets to here - Sanity Check
            break;
   	    } 
        return forwardPoint;

    }

    // Determines whether or not we can move forward.
    // Updates the moves queue.
    private boolean canMoveForward() {
        
        Point2D.Double forwardPoint = getPointInFront();

        // Send in empty because we don't want to consider using an axe in this case
        if (curMap.isEmptySpace(forwardPoint, false)) { 
            movesToDo.add(new Move('f'));
            return true;     
        }
        
        return false;
    } 

    // Determines which way we should rotate when we can't move forward
    // Updates the moves queue.
    private void rotateToAppropriateOrientation() {
        
        // go left then right -< arbitrarily assigned.
        Point2D.Double left = getPointToLeft();
        Point2D.Double right = getPointToRight(); 

        if (curMap.isEmptySpace(left, false)) {
            movesToDo.add(new Move('L'));
            return;
        } else if (curMap.isEmptySpace(right,false)) {
            movesToDo.add(new Move('R'));
            return;
        }
        
        // go backwards if only option
        Move turnAround = new Move('L');
        movesToDo.add(turnAround);
        movesToDo.add(turnAround);
        return;   
    }

}



























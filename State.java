import java.util.*;
import java.awt.geom.Point2D;
import java.io.*;

public class State {
    
    private Map curMap; // The original map
    private ArrayList<Move> movesTaken; // Need this to get back to the start
    private Boolean axe; // Determines whether we have an axe or not
    private int dynamite; // Determines how many dynamites we have
    private int boat;
    private Point2D.Double curLocation; // Current Location
    private Stack unexplored; // ?
    private Queue<Move> movesToDo; // Current queue of moves to be executed. This is automic.   
    private int direction; //(used as an enum: 1 = up, 2 = right, 3 = down, 4 = left (clockwise from up position));

    //tools
    public static final int AXE = 0;
    public static final int DYNAMITE = 1;
    public static final int BOAT = 2;

    public static final int NOT_ALLOWED = -1;
    
    public State(){
    	curMap = new Map();
        direction = 1;
        curLocation = new Point2D.Double(0,0);
        dynamite = 0;
        boat = 0;
        axe = false;
        movesTaken = new ArrayList<Move>();
        unexplored = new Stack();
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
           
           // See if forward move is valid. Not actually required when we run the AI since it does 
           // this check anyway but eh
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
            
            //Blow it up
            curMap.blow(inFront);
            dynamite--;    
			break;
		
		}	
        assert(direction > 0 && direction < 5); //Sanity Check
        
        //Update movesTaken
        movesTaken.add(thing);
        
        //Print the map
        curMap.printMap();
        
        //Print out the state information
        System.out.println("Direction is: " + direction);
        System.out.println(dynamite + " dynamite");
        System.out.println("curLocation is (" + curLocation.getX() + ", " + (int)curLocation.getY() + ")");

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
        
        // Are there any other moves?
        Move temp = movesToDo.poll();
        
        if (temp != null) {
            System.out.println("Temp was not null");
            return temp;
        }

        if (isGoldReachable()) {
             temp = (Move) movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (isAxeReachable()) {
             temp = (Move)movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (isDynamiteReachable()) {
             temp = (Move) movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (!isExplored()) {
            System.out.println("Is not explored");
             temp = (Move) movesToDo.poll();
             assert(temp != null);
             return temp;
        }

        if (canMoveForward()) {
             temp = (Move) movesToDo.poll();
             assert(temp != null);
             return temp;
        }


        rotateToAppropriateOrientation();
        temp = (Move) movesToDo.poll();
        assert(temp != null);
        return temp;

    }


    // Determines if we can reach te gold.
    // We remove any restrictions on whether or not we use the dynamite to get to it as if we can get
    // to the gold it's game over. 
    // Updates the moves queue.
    private boolean isGoldReachable() {
        
        // If we haven't found gold yet, no point searching for it.
        Point2D.Double gold = curMap.getGoldLocation();
        if (gold == null) return false;

        int[] items = new int[3];

        if(axe) items[AXE] = 1;
        else items[AXE] = 0;

        items[DYNAMITE] = dynamite;
        items[BOAT] = boat;
        LinkedList<Move> moves = curMap.isGoldReachable(direction, curLocation, items);
        if (moves == null) {
            return false;
        } else {
            while (moves.size() > 0) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }

    }
    
    // Determines whether or not we can get to some dynamite.
    // We place restrictions on using dynamite to get to it for now. This might be a mistake TODO
    // Updates the moves queue.
    private boolean isDynamiteReachable() {
        int[] items = new int[3];

        if(axe) items[AXE] = 1;
        else items[AXE] = 0;

        items[DYNAMITE] = NOT_ALLOWED;
        items[BOAT] = boat;
        //ask the map if dynamite is reachable
        LinkedList<Move> moves = curMap.isDynamiteReachable(direction, curLocation, items);         

        if (moves == null) {
            return false;
        }else{
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }
    }

    // Determines whether or not the axe is reachable. 
    // We place on restrictions on using dynamite to get to it. This might be a mistake TODO
    // Updates the moves queue.
    private boolean isAxeReachable(){
        if (axe){
            return false;
        }

        int[] items = new int[3];
        items[AXE] = 0;
        items[BOAT] = boat;
        items[DYNAMITE] = dynamite;
        LinkedList<Move> moves = curMap.isAxeReachable(curLocation, direction, items);         

        if (moves == null) {
            return false;
        }else{
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return true;
        }

    } 

    // Determines whether or not the current "space" is explored.
    // In the case of a maze like arena, this returns true if each branch is explored without having
     // to cut anything down or blow anything up.
    // Updates the moves queue.
    private boolean isExplored() {
        LinkedList<Move> moves = curMap.isExplored(curLocation, direction);

        if(moves != null){
            while (moves.size() > 0 ) {
                Move temp = moves.remove();
                movesToDo.add(temp);
            }
            return false;
        }
        return true;
    }


    // Gets the current direction
    public int getDirection() {
        return direction;
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



























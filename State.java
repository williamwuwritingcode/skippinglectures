import java.util.*;
import java.awt.geom.Point2D;
import java.io.*;

public class State {
    
    public Map originalMap;
    private ArrayList<Move> movesTaken[];
    private Boolean axe;
    private int dynamite;
    private Point2D curLocation;
    private Stack unexplored; 
    private Queue movesToDo;    
    private int direction; //(used as an enum: 1 = up, 2 = right, 3 = down, 4 = left (clockwise from up position));

    public State(){
    	originalMap = new Map();
        direction = 0;
        curLocation = new Point2D.Double(0,0);
        dynamite = 0;
        axe = false;
        movesTaken = new ArrayList<Point2D>();
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
		return curMap.isValidForward(forwardPoint, axe);
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
            Point2D forwardPoint = null;
            
            //Get the forward point
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
                Map curMap = originalMap.removeStuff(); 
               
                //Update the curLocation
                curLocation = forwardPoint;
          		break;
		
		//Case of trying to chop down	
        case 'C': case 'c':
            if (direction == 1) {   
               direction = 4;     
            } else {
               direction--;
            }
			
			break;
		
		//Case of trying to blow stuff up
		case 'b': case 'B':
		
			break;
		
		}	
        assert(direction > 0 && direction < 5); //Sanity Check
        return;
    }
}

public int getDirection() {
    return direction;
}




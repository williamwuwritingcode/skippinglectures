import java.util.*;
import java.awt.geom.Point2D;
import java.io.*;

public class State {
    
    private ArrayList<Move> movesTaken[];
    private Boolean axe;
    private int dynamite;
    private Point2D curLocation;
    private Stack unexplored; 
    private Queue movesToDo;    
    private int direction; //(used as an enum: 0 = up, 1 = down, 2 = left, 3 = right);

    public State(){
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


    }

    private void rotateToAppropriateOrientation() {

    }

    //Takes in the move that's about to be performed and updates the currrent state appropriately
    private void adjustState(Move thing) {
        if (thing.getMove == 'r' || thing.getMove() == 'r') {
            // rotate direction to the right
        }//too tired to continue too much but more or less continue
    }

}    

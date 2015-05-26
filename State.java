public class State {
    
    private ArrayList<Move> movesTaken[];
    private Boolean axe;
    private int dynamite;
    private Point2D curLocation;
    private Stack unexplored; 
    private Queue movesToDo;    

    public State(){
        
        curLocation = new Point2D.Double(0,0);
        dynamite = 0;
        axe = false;
        movesTaken = new ArrayList<Point2D>();
        unexplored = new Stack();
        movesToDo = new LinkedList<Move>();
    }

    public ArrayList<Move> makeMove() {
        Move temp = movesToDo.poll();
        if (movesToDo != null) {
            return movesToDo;
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

    private void rotateToAppropriateOrientation()

}    

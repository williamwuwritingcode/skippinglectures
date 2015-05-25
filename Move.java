public class State {

    private char move;

    public Move(char move) {
        // Check if move is valid first
        this.move = move;
    }
    
    public Move getMove() {
        return move;
    }
}

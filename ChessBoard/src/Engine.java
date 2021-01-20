import java.util.*;

public class Engine {

    private ChessBoard board;
    private long startTime;
    private long allocated;
    private int initialTime, increment;

    private static final int WHITE = ChessBoard.WHITE;
    private static final int BLACK = ChessBoard.BLACK;
    private static final int INFINITY=1000000, MATE = 300000;

    private static final String programName = "Put Your Name Here";

    public String getName() {
	return programName;
    }

    public void newGame(int time, int inc) {
	initialTime = time;
	increment = inc;
	board = new ChessBoard();
    }

    public void applyMove(Move m) {
	board.makeMove(m);
    }

    public ChessBoard getBoard() {
	return board;
    }

    public Move computeMove(int timeleft, int optime) {

	Move move;
	startTime = System.currentTimeMillis();
	allocated = allocateTime(timeleft, optime);
	System.out.println("ALLOCATED: " + allocated + "ms");
	return search();
    }

    private int allocateTime(int timeleft, int optime) {
	double t = increment + timeleft/30.0;
	if (t > timeleft) t = .9*timeleft;
	return (int) t;
    }

    private boolean timeup() {

	if ((System.currentTimeMillis()-startTime) > allocated) {
	    return true;
	}
	return false;
    }

    private Move search() {
	return randomMove();
    }

    private Move randomMove() {
	Random rand = new Random();
	List moveList = board.generateMoves();
	if (moveList.size() == 0) return null;

	Move [] moveArray = (Move[]) moveList.toArray(new Move[0]);
	Move [] captureArray = new Move[moveArray.length];
	int capCount = 0;
	for (int i=0; i<moveArray.length; i++) if (moveArray[i].capture) captureArray[capCount++] = moveArray[i];
	if (capCount > 0) return captureArray[rand.nextInt(capCount)];
	return moveArray[rand.nextInt(moveArray.length)];
    }

    private int alpha_beta(ChessBoard board, int depth, int alpha, int beta) {
	return 0;
    }
}

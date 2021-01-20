import java.util.*;

public class ChessBoard {
    public byte[][] board = new byte[8][8];
    public int previousDoublePush;
    public int turn;
    public boolean[] kingside = new boolean[2], queenside = new boolean[2];
    public boolean[] hascastled = new boolean[2];

    public static final int KNIGHT = 0;
    public static final int BISHOP = 1;
    public static final int ROOK   = 2;
    public static final int QUEEN  = 3;
    public static final int KING   = 4;
    public static final int PAWN   = 5;
    public static final int EMPTY  = 6;
    public static final int CBIT   = 8;

    public static final int BLACK  = 0;
    public static final int WHITE  = 1;

    public static int colorof(byte x) {
	return (x & CBIT)>>3;
    }

    public static int pieceof(byte x) {
	return x & (~CBIT);
    }
    
    public static boolean isempty(byte x) {
	return (x & (~CBIT)) == EMPTY;
    }

    public static byte makesquare(int p, int c) {
	return (byte) (p | (c<<3));
    }


    private static class Pair {
	int x, y;
	Pair(int xx, int yy) {
	    x = xx;
	    y = yy;
	}
    }

    private static class PieceDesc {
	boolean iterate;
	Pair[] delta;
	PieceDesc(boolean iter, int[] deltas) {
	    iterate = iter;
	    delta = new Pair[deltas.length/2];
	    for (int i=0; i<deltas.length; i+=2) {
		delta[i/2] = new Pair(deltas[i], deltas[i+1]);
	    }
	}
    }

    public class SquareDesc {
	public SquareDesc() { }
	public SquareDesc(int t, int c, int ix, int iy) { type=t; color=c; x=ix; y=iy; }

	public int type,color,x,y;
    }

    private static PieceDesc[] pdesc = new PieceDesc[5];
    static {
	pdesc[KNIGHT] = new PieceDesc(false,  new int[]
	    { 2, 1 , 1, 2 , 2, -1 , 1, -2 , -2, 1 , -1,2 , -2,-1 , -1,-2 });
	
	pdesc[BISHOP] = new PieceDesc(true, new int[]
	    { 1, 1 , 1,-1 , -1, -1 , -1,1 });
	
	pdesc[ROOK] = new PieceDesc(true, new int[]
	    { 1, 0 , 0, 1 , -1,0 , 0,-1 });
	
	pdesc[QUEEN] = new PieceDesc(true, new int[]
	    { 1, 0 , 0, 1 , -1,0 , 0,-1 , 1, 1 , 1,-1 , -1, -1 , -1,1 });
	
	pdesc[KING] = new PieceDesc(false, new int[]
	    { 1, 0 , 0, 1 , -1,0 , 0,-1 , 1, 1 , 1,-1 , -1, -1 , -1,1 });
    }

    private static String piecestr = "nbrqkp-XNBRQKPX";

    /** Prints an ascii picture of a chess board.  p=pawn, n=knight
        b=bishop, r=rook, q=queen, k=king. Lower case letters are the
        black pieces, upper case letters are the white. */

    public String toString() {
	String s = "";
	s += "   a b c d e f g h\n";
	s += "  +---------------+\n";
	for (int y=7; y>=0; y--) {
	    s += (y+1) + " |";
	    for (int x=0; x<8; x++) {
		s += piecestr.charAt(board[x][y]);
		if (x<7) s += " ";
	    }
	    s += "| " + (y+1);
	    s += "\n";
	}
	s += "  +---------------+\n";
	s += "   a b c d e f g h\n";
	return s;
    }

    ChessBoard() {
	turn = WHITE;
	for (int x=0; x<8; x++) {
	    for (int y=0; y<8; y++) {
		board[x][y] = EMPTY;
	    }
	}
	previousDoublePush = -1;
	for (int color = 0; color < 2; color++) {
	    kingside[color] = queenside[color] = true;
	    hascastled[color] = false;
	}
	board[0][0] = makesquare(ROOK, WHITE);
	board[1][0] = makesquare(KNIGHT, WHITE);
	board[2][0] = makesquare(BISHOP, WHITE);
	board[3][0] = makesquare(QUEEN, WHITE);
	board[4][0] = makesquare(KING, WHITE);
	board[5][0] = makesquare(BISHOP, WHITE);
	board[6][0] = makesquare(KNIGHT, WHITE);
	board[7][0] = makesquare(ROOK, WHITE);
	for (int x=0; x<8; x++) {
	    board[x][7] = makesquare(pieceof(board[x][0]), BLACK);
	    board[x][1] = makesquare(PAWN, WHITE);
	    board[x][6] = makesquare(PAWN, BLACK);
	}
    }

    ChessBoard(ChessBoard b) {
	for (int x=0; x<8; x++) {
	    for (int y=0; y<8; y++) {
		board[x][y] = b.board[x][y];
	    }
	}

	previousDoublePush = b.previousDoublePush;
	turn = b.turn;
	for (int i=0; i<2; i++) {
	    kingside[i] = b.kingside[i];
	    queenside[i] = b.queenside[i];
	    hascastled[i] = b.hascastled[i];
	}
    }

    private void makeCastlingMove(Move m) {
	board[m.destx][m.desty] = board[m.srcx][m.srcy];
	board[m.srcx][m.srcy] = EMPTY;
	if (m.destx == 6) {  // Kingside
	    board[5][m.desty] = board[7][m.srcy];
	    board[7][m.srcy] = EMPTY;  
	}

	if (m.destx == 2) {  // Queenside
	    board[3][m.desty] = board[0][m.srcy];
	    board[0][m.srcy] = EMPTY;  
	}
	kingside[turn] = queenside[turn] = false;
	hascastled[turn] = true;

	turn = 1-turn;
    }

    public SquareDesc[] getPieces(int pturn) {

	LinkedList pieces = new LinkedList();

	for (int x=0; x<8; x++) {
	    for (int y=0; y<8; y++) {
		if (isempty(board[x][y])) continue;
		if (colorof(board[x][y]) != pturn) continue;
		pieces.addFirst(new SquareDesc(pieceof(board[x][y]), colorof(board[x][y]),x,y));
	    }
	}

	return (SquareDesc[]) pieces.toArray(new SquareDesc[0]);
    }

    public void makeMove(Move m) {

	if ((pieceof(board[m.srcx][m.srcy]) == KING) && 
	    ((m.srcx-m.destx >= 2) || (m.srcx-m.destx <= -2))) {
	    makeCastlingMove(m);
	    return;
	}
	
	if ((pieceof(board[m.srcx][m.srcy]) == PAWN) && 
	    (isempty(board[m.destx][m.desty])) &&
	    (m.srcx != m.destx)) {
	    board[m.destx][m.srcy] = EMPTY;
	}

	if ((pieceof(board[m.srcx][m.srcy]) == PAWN) && 
	    (m.desty != m.srcy +1) && (m.desty != m.srcy -1)) {
	    previousDoublePush = m.srcx;
	} else {
	    previousDoublePush = -1;
	}

	if (pieceof(board[m.srcx][m.srcy]) == KING) {
	    kingside[turn] = queenside[turn] = false;
	}

	int castleRank = (turn == WHITE)? 0: 7;
	if ((m.destx == 0) && (m.desty == castleRank)) queenside[turn] = false;
	if ((m.destx == 7) && (m.desty == castleRank)) kingside[turn] = false;
    
	board[m.destx][m.desty] = board[m.srcx][m.srcy];
	board[m.srcx][m.srcy] = EMPTY;
    
	if ((pieceof(board[m.destx][m.desty]) == PAWN) && 
	    (m.desty == 7 || m.desty == 0)) {
	    /* promote to a queen */
	    board[m.destx][m.desty] = makesquare(QUEEN, turn);
	}
	turn = 1-turn;
    }

    public int getTurn() {
	return turn;
    }

    public SquareDesc getSquare(int x, int y) {
	if (isempty(board[x][y]))
	    return new SquareDesc(EMPTY, EMPTY, x, y);
	else
	    return new SquareDesc(pieceof(board[x][y]), colorof(board[x][y]), x , y);
    }

    private boolean pawnCheck(int x, int y) {
	int dy, dx;
	byte enemyking = makesquare(KING, turn);
	if (1-turn == WHITE) dy = 1; else dy = -1;
	if (y+dy >= 8 || y+dy < 0) return false;
	for (dx = -1; dx <= 1; dx += 2) {
	    if (x+dx < 0 || x+dx >= 8) continue;
	    if (board[x+dx][y+dy] == enemyking) return true;
	}
	return false;
    }

    private boolean pieceCheck(int x, int y) {
	int cx, cy;
	boolean stopnow;
	byte enemyking = makesquare(KING, turn);
	int piece = pieceof(board[x][y]);
	for (int dir = 0; dir < pdesc[piece].delta.length; dir++) {
	    cx = x;
	    cy = y;
	    stopnow = !pdesc[piece].iterate;
	    while (true) {
		cx += pdesc[piece].delta[dir].x;
		cy += pdesc[piece].delta[dir].y;
		if (cx < 0 || cy < 0 || cx >=8 || cy >= 8) break;
		if (!isempty(board[cx][cy])) {
		    if (colorof(board[cx][cy]) == 1-turn) break;
		    stopnow = true;
		}
		if (board[cx][cy] == enemyking) return true;
		if (stopnow) break;
	    }
	}
	return false;
    }

    public boolean inCheck() {
	for (int x=0; x<8; x++) {
	    for (int y=0; y<8; y++) {
		if (isempty(board[x][y])) continue;
		if (colorof(board[x][y]) != 1-turn) continue;
		if (pieceof(board[x][y]) == PAWN) {
		    if (pawnCheck(x, y)) return true;
		} else {
		    if (pieceCheck(x, y)) return true;
		}
	    }
	}
	return false;
    }

    private void generatePawnMoves(LinkedList moveList, int x, int y) {
	int dy, dx, ex, ey;
	ChessBoard newp;
	
	if (turn == WHITE) dy = 1; else dy = -1;
	if (y+dy >= 8 || y+dy < 0) return;
	/* push 1 */
	if (isempty(board[x][y+dy])) {
	    newp = new ChessBoard(this);
	    newp.board[x][y+dy] = newp.board[x][y];
	    newp.board[x][y] = EMPTY;
	    if (!newp.inCheck()) moveList.addFirst(new Move(x, y, x, y+dy, false));
	    /* push 2 */
	    if (((turn == WHITE && y == 1) || (turn == BLACK && y == 6)) && isempty(board[x][y+2*dy])) {
		newp.board[x][y+2*dy] = newp.board[x][y+dy];
		newp.board[x][y+dy] = EMPTY;	    
		if (!newp.inCheck()) moveList.addFirst(new Move(x, y, x, y+2*dy, false));
	    }
	}

	for (dx = -1; dx <= 1; dx += 2) {
	    if (x+dx < 0 || x+dx >= 8) continue;
	    if (!isempty(board[x+dx][y+dy])) {
		if (colorof(board[x+dx][y+dy]) == turn) continue;

		ex = x;
		ey = y;
	    } else {
		if (x+dx != previousDoublePush) continue;
		if (!((turn == WHITE && y == 4) || (turn == BLACK && y == 3))) continue;
		ex = x+dx;
		ey = y;
	    }
	    newp = new ChessBoard(this);
	    newp.board[x+dx][y+dy] = newp.board[x][y];
	    newp.board[x][y] = EMPTY;
	    newp.board[ex][ey] = EMPTY;
	    if (!newp.inCheck()) moveList.addFirst(new Move(x, y, x+dx, y+dy, true));
	}
    }

    private void generatePieceMoves(LinkedList moveList, int x, int y) {
	int cx, cy, dir;
	boolean stopnow, capture;
	ChessBoard newp;
	int piece = pieceof(board[x][y]);
	stopnow = !pdesc[piece].iterate;
	for (dir = 0; dir < pdesc[piece].delta.length; dir++) {
	    cx = x;
	    cy = y;
	    capture = false;
	    while (true) {
		cx += pdesc[piece].delta[dir].x;
		cy += pdesc[piece].delta[dir].y;
		/* (cx, cy) is the current position */
		if (cx < 0 || cy < 0 || cx >=8 || cy >= 8) break;
		if (!isempty(board[cx][cy])) {
		    if (colorof(board[cx][cy]) == turn) break;
		    /* hit an enemy piece */
		    capture = true;
		}

		newp = new ChessBoard(this);
		newp.board[cx][cy] = newp.board[x][y];
		newp.board[x][y] = EMPTY;
		if (!newp.inCheck()) moveList.addFirst(new Move(x, y, cx, cy, capture));
		if (stopnow || capture) break;
	    }
	}
    }

    private void generateQueenCastling(int castleRank, LinkedList moveList) {
	if (board[0][castleRank] != makesquare(ROOK, turn)) return;
	if (!isempty(board[3][castleRank])) return;
	if (!isempty(board[2][castleRank])) return;
	if (!isempty(board[1][castleRank])) return;

	ChessBoard newp = new ChessBoard(this);
	if (newp.inCheck()) return;
	newp.board[3][castleRank] = newp.board[4][castleRank];
	newp.board[4][castleRank] = EMPTY;
	if (newp.inCheck()) return;
	newp.board[2][castleRank] = newp.board[3][castleRank];
	newp.board[3][castleRank] = EMPTY;
	if (newp.inCheck()) return;
	moveList.addFirst(new Move(4, castleRank, 2, castleRank, false));
    }

    private void generateKingCastling(int castleRank, LinkedList moveList) {
	if (board[7][castleRank] != makesquare(ROOK, turn)) return;
	if (!isempty(board[5][castleRank])) return;
	if (!isempty(board[6][castleRank])) return;

	ChessBoard newp = new ChessBoard(this);
	if (newp.inCheck()) return;
	newp.board[5][castleRank] = newp.board[4][castleRank];
	newp.board[4][castleRank] = EMPTY;
	if (newp.inCheck()) return;
	newp.board[6][castleRank] = newp.board[5][castleRank];
	newp.board[5][castleRank] = EMPTY;
	if (newp.inCheck()) return;
	moveList.addFirst(new Move(4, castleRank, 6, castleRank, false));
    }

    private void generateCastlingMoves(LinkedList moveList) {
	int castleRank = (turn == WHITE)? 0: 7;
	if (board[4][castleRank] != makesquare(KING, turn)) return;
	if (kingside[turn]) generateKingCastling(castleRank, moveList);
	if (queenside[turn]) generateQueenCastling(castleRank, moveList);
    }

    public List generateMoves() {
	LinkedList moveList = new LinkedList();

	for (int x=0; x<8; x++) {
	    for (int y=0; y<8; y++) {
		if (isempty(board[x][y])) continue;
		if (colorof(board[x][y]) != turn) continue;
		if (pieceof(board[x][y]) == PAWN) {
		    generatePawnMoves(moveList, x, y);
		} else {
		    generatePieceMoves(moveList, x, y);
		}
	    }
	}
	generateCastlingMoves(moveList);
	return moveList;
    }
}

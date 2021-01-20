public class Evaluator {

    public static int eval(ChessBoard board) {
	return 0;
    }

    private static int[] pieceValue = new int[7];
    static {
	pieceValue[ChessBoard.PAWN]   = 100;
	pieceValue[ChessBoard.KNIGHT] = 300;
	pieceValue[ChessBoard.BISHOP] = 300;
	pieceValue[ChessBoard.ROOK]   = 500;
	pieceValue[ChessBoard.QUEEN]  = 900;
	pieceValue[ChessBoard.KING]   = 0;
	pieceValue[ChessBoard.EMPTY]  = 0;
    }
    
    private static int bishoppos[] = {-5,-5,-5,-5,-5,-5,-5,-5,
				      -5,10,5,8,8,5,10,-5,
				      -5,5,3,8,8,3,5,-5,
				      -5,3,10,3,3,10,3,-5,
				      -5,3,10,3,3,10,3,-5,
				      -5,5,3,8,8,3,5,-5,
				      -5,10,5,8,8,5,10,-5,
				      -5,-5,-5,-5,-5,-5,-5,-5};
    
    private static int knightpos[] = {-10,-5,-5,-5,-5,-5,-5,-10,
				      -8,0,0,3,3,0,0,-8,
				      -8,0,10,8,8,10,0,-8,
				      -8,0,8,10,10,8,0,-8,
				      -8,0,8,10,10,8,0,-8,
				      -8,0,10,8,8,10,0,-8,
				      -8,0,0,3,3,0,0,-8,
				      -10,-5,-5,-5,-5,-5,-5,-10};
    
    private static int pawnpos[] =   {0,0,0,0,0,0,0,0,
				      0,0,0,-5,-5,0,0,0,	
				      0,2,3,4,4,3,2,0,
				      0,4,6,10,10,6,4,0,
				      0,6,9,10,10,9,6,0,
				      4,8,12,16,16,12,8,4,
				      5,10,15,20,20,15,10,5,
				      0,0,0,0,0,0,0,0};
}

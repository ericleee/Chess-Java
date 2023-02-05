package Chess;

import java.util.ArrayList;
import java.util.HashMap;

public class Move {

    private int startRow;
    private int startCol;
    public int endRow;
    public int endCol;
    private String movedPiece;
    private String capturedPiece;
    public boolean pawnPromotion;
    public static boolean isEnPassantMove;
    public static castleRights currentCastlingRight = new castleRights(true, true, true, true);
    public static ArrayList<castleRights> castleRightsLog = new ArrayList<>();
    private int moveID;

    public Move(ArrayList<Integer> startSq, ArrayList<Integer> endSq){ // Move class constructor
        startRow = startSq.get(0); startCol = startSq.get(1); // define the row and column the piece is starting its move from
        endRow = endSq.get(0); endCol = endSq.get(1); // define the row and column the piece is ending its move at
        movedPiece = chessGame.board[startRow][startCol]; // get the piece stored at the starting square
        capturedPiece = chessGame.board[endRow][endCol]; // get the piece stored at the ending piece
        pawnPromotion = (movedPiece.equals("wP") && endRow == 0) || (movedPiece.equals("bP") && endRow == 7); // determine if move is a promotin
        isEnPassantMove = false; // determine if move is en passant or not
        if (isEnPassantMove){
            if (movedPiece.equals("bP")){capturedPiece = "wP";}
            else {capturedPiece = "bP";}
        }
        moveID = startRow * 1000 + startCol * 100 + endRow * 10 + endCol; // create a unique move id

        castleRightsLog.add(new castleRights(currentCastlingRight.wks, currentCastlingRight.wqs, currentCastlingRight.bks, currentCastlingRight.bqs)); // create new castling rights class and store it in the castle rights log ArrayList
    }

    public Move(ArrayList<Integer> startSq, ArrayList<Integer> endSq, boolean enPassant){ // create version of Move construtor with en passant parameter
        startRow = startSq.get(0); startCol = startSq.get(1); // rest of the code is the same as other constructor
        endRow = endSq.get(0); endCol = endSq.get(1);
        movedPiece = chessGame.board[startRow][startCol];
        capturedPiece = chessGame.board[endRow][endCol];
        pawnPromotion = (movedPiece.equals("wP") && endRow == 0) || (movedPiece.equals("bP") && endRow == 7);
        isEnPassantMove = enPassant; // except here where we are no longer hard coding the value but rather getting it from the parameters
        if (isEnPassantMove){
            if (movedPiece.equals("bP")){capturedPiece = "wP";}
            else {capturedPiece = "bP";}
        }
        moveID = startRow * 1000 + startCol * 100 + endRow * 10 + endCol;

        castleRightsLog.add(new castleRights(currentCastlingRight.wks, currentCastlingRight.wqs, currentCastlingRight.bks, currentCastlingRight.bqs));
    }

    public static void makeMove(Move move){
        chessGame.board[move.startRow][move.startCol] = "--"; // set the square the piece is leaving to blank
        chessGame.board[move.endRow][move.endCol] = move.movedPiece; // set the square the piece is going to as the piece
        chessGame.moveList.add(move); // add the move to the move log
        chessGame.whiteToMove = !chessGame.whiteToMove; // switch whose turn it is

        if (move.movedPiece.equals("wK")){ // if the piece moved is the white king
            chessGame.wKingLoc[0] = move.endRow; // update the white kings location
            chessGame.wKingLoc[1] = move.endCol;
        }

        else if (move.movedPiece.equals("bK")){ // if the piece moved is the black king
            chessGame.bKingLoc[0] = move.endRow; // update the black kings location
            chessGame.bKingLoc[1] = move.endCol;
        }

        if (move.pawnPromotion){ // if move is a pawn promotion
            chessGame.board[move.endRow][move.endCol] = move.movedPiece.charAt(0) + "Q"; // set the end square to be a queen instead of a pawn
        }

        if (move.isEnPassantMove){ // if move is an en passant
            chessGame.board[move.startRow][move.endCol] = "--"; // set the 'middle' square to be empty instead
        }

        if (move.movedPiece.charAt(1) == 'P' && Math.abs(move.startRow - move.endRow) == 2){ // if a pawn was moved 2 squares forward
            chessGame.enpassantPossible.clear(); // update the location of the possible en passant
            chessGame.enpassantPossible.add((move.startRow + move.endRow) / 2 );
            chessGame.enpassantPossible.add(move.startCol);
        }
        else {
            chessGame.enpassantPossible.clear();
        }

        updateCastleRights(move); // update the current log for castle rights
        castleRightsLog.add(new castleRights(currentCastlingRight.wks, currentCastlingRight.wqs, currentCastlingRight.bks, currentCastlingRight.bqs));
    }

    public static void undoMove(){
        if (chessGame.moveList.size() != 0){ // if moves exists to be undone
            Move move = chessGame.moveList.get(chessGame.moveList.size() - 1); // get the last move in the move log
            chessGame.moveList.remove(chessGame.moveList.size() - 1); // remove the last move in the move log
            chessGame.board[move.startRow][move.startCol] = move.movedPiece; // undo the last move
            chessGame.board[move.endRow][move.endCol] = move.capturedPiece;
            chessGame.whiteToMove = !chessGame.whiteToMove; // swtich whose turn it is

            if (move.movedPiece.equals("wK")){ // if piece moved is the white king
                chessGame.wKingLoc[0] = move.startRow; // update the white kings location
                chessGame.wKingLoc[1] = move.startCol;
            }

            else if (move.movedPiece.equals("bK")){ // if piece moved is the black king
                chessGame.bKingLoc[0] = move.startRow; // update the black kings location
                chessGame.bKingLoc[1] = move.startCol;
            }

            if (move.isEnPassantMove){ // if move is an en passant
                chessGame.board[move.endRow][move.endCol] = "--"; // set end square to be blank instead
                chessGame.board[move.startRow][move.endCol] = move.capturedPiece; // set the 'middle' square to be empty instead

                chessGame.enpassantPossible.clear(); // update the location of the possible en passant move
                chessGame.enpassantPossible.add(move.endRow);
                chessGame.enpassantPossible.add(move.endCol);
            }

            if (move.movedPiece.charAt((1)) == 'P' && Math.abs(move.startRow - move.endRow) == 2){ // if move is a pawn moving 2 squares forward
                chessGame.enpassantPossible.clear(); // update the location of the possible en passant move
            }
            castleRightsLog.remove(castleRightsLog.size() - 1); // remove last change to castle rights log
        }
    }

    public static void updateCastleRights(Move move){
        if (move.movedPiece.equals("wK")){ // piece moved is the white king
            currentCastlingRight.wks = false; // white cannot castle to any side
            currentCastlingRight.wqs = false;
        }
        else if (move.movedPiece.equals("bK")){ // piece moved is the black side
            currentCastlingRight.bks = false; // black cannot castle to any side
            currentCastlingRight.bqs = false;
        }
        else if (move.movedPiece.equals("wR")){ // piece moved is a white rook
            if (move.startRow == 7){ // if rook started from its back rank
                if (move.startCol == 0){ // if rook is queen side
                    currentCastlingRight.wqs = false; // white cannot castle to queen side
                }
                else if (move.startCol == 7){ // if rook is king side
                    currentCastlingRight.wks = false; // white cannot castle to king side
                }
            }
        }
        else if (move.movedPiece.equals("bR")){ // piece moved is a black rook
            if (move.startRow == 0){ // if rook started from its back rank
                if (move.startCol == 0){ // if rook is queen side
                    currentCastlingRight.bqs = false; // black cannot castle to queen side
                }
                else if (move.startCol == 7){ // if rook is king side
                    currentCastlingRight.bks = false; // black cannot castle to king side
                }
            }
        }
    }

    @Override
    public boolean equals(Object object){ // overide the equals method for Move class
        if (object instanceof Move){ // if the object being compared to a Move is also a move
            return ((Move) object).moveID == this.moveID; // return if the ids match or not
        }
        return false;
    }
}
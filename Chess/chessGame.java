package Chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class chessGame {

    public static String[][] board;
    public static boolean whiteToMove;
    public static ArrayList<Move> moveList = new ArrayList<>();
    public static ArrayList<Move> validMoves;
    public static ArrayList<ArrayList<Integer>> clicks;
    boolean moveMade;
    public static int[] wKingLoc = {7, 4};
    public static int[] bKingLoc = {0, 4};
    public static boolean checkmate = false;
    public static boolean stalemate = false;
    public static ArrayList<Integer> enpassantPossible = new ArrayList<>();
    public static int wins;

    public chessGame() { // chessGame constructor, run everytime a chessGame object is created
        board = new String[][]{
                {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR",},
                {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP",},
                {"--", "--", "--", "--", "--", "--", "--", "--",},
                {"--", "--", "--", "--", "--", "--", "--", "--",},
                {"--", "--", "--", "--", "--", "--", "--", "--",},
                {"--", "--", "--", "--", "--", "--", "--", "--",},
                {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP",},
                {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR",}
        }; // create the starting board

        play(); // call the play function to begin game
    }

    private void play() {
        ArrayList<Integer> selected_square = new ArrayList<>(); // create ArrayList to track the row and column of the currently selected square
        clicks = new ArrayList<>(); // create ArrayList
        whiteToMove = true; // create boolean variable to track whose turn it is to move
        validMoves = getValidMoves(); // get a list of all the legal moves the current side can make
        moveMade = false; // create boolean variable to prevent attempts to make illegal moves from counting as a turn

        GUI.frame.addMouseListener(new MouseAdapter() { // create a mouse listener to track mouse clicks
            public void mouseClicked(MouseEvent me) {

                if (me.getButton() == MouseEvent.BUTTON1) { // if left click is clicked
                    int col = Math.floorDiv(me.getX(), 96); // get the column of the square that was clicked on
                    int row = Math.floorDiv(me.getY(), 96); // get the row of the square that was clicked on

                    if ((!selected_square.isEmpty()) && selected_square.get(0) == row && selected_square.get(1) == col) { // if the same square is clicked twice
                        selected_square.clear(); // reset our vars to allow new squares to be clicked on
                        clicks.clear();
                    } else { // if the same square was not selected set selected square to be the square that was clicked on and append to our clicks ArrayList
                        selected_square.clear();
                        selected_square.add(row);
                        selected_square.add(col);

                        clicks.add(new ArrayList<>(selected_square));
                    }

                    if (clicks.size() == 2) { // if a starting square and ending square has been selected
                        Move move = new Move(clicks.get(0), clicks.get(1)); // create an instance of Move class

                        for (Move current_move : validMoves) { // for all the valid moves the player could make
                            if (move.equals(current_move)) { // if the move they attempted is in that list of valid moves
                                Move.makeMove(current_move); // make the move
                                moveMade = true; // set our moveMade variable to true

                                for (Component component : GUI.panel.getComponents()) { // remove all the piece images
                                    if (component instanceof JLabel) {
                                        GUI.panel.remove(component);
                                    }
                                }

                                GUI.drawPieces(); // redraw the pieces images, but will now do so with the updated postion

                                GUI.panel.revalidate(); // show these changes on the screen
                                GUI.panel.repaint();

                                selected_square.clear(); // reset the ArrayLists since a move was completed and it needs to be blank to make another
                                clicks.clear();
                            }
                        }
                        if (!moveMade) { // if the move was not made (illegal)
                            clicks.clear(); // rest the ArrayLists so the user can try again
                            selected_square.clear();
                        }
                    }

                    if (moveMade) { // if a move has been made
                        validMoves = getValidMoves(); // update the ArrayList of valid moves
                        moveMade = false; // rest the moveMade variable

                        if (checkmate || stalemate) { // check if game is over
                            try {
                                gameover(); // call the game over function to handle end of game behaviour
                            } catch (IOException e){e.printStackTrace();}
                        }

                    }
                }
            }
        });

        GUI.frame.addKeyListener(new KeyListener() { // add a key listener to allow for the program to register keys being typed
            @Override
            public void keyTyped(KeyEvent e) { // when a key is typed
                if (e.getKeyChar() == 'p' && (checkmate || stalemate)){ // if 'P' is pressed and the game is over
                    GUI.frame.dispose(); // delete the current game
                    new GUI(); // create another
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private ArrayList<Move> getValidMoves() {
        ArrayList<Integer> tempEnPassantPossible = enpassantPossible; // create a temp ArrayList so generating moves does not permanently affect the enpassentPossile ArrayList
        ArrayList<Move> moves = getPossibleMoves(); // create an ArrayList of all the physically possible moves

        for (int i = moves.size() - 1; i >= 0; i--) { // loop through the moves ArrayList
            Move.makeMove(moves.get(i)); // temporarily make the move
            whiteToMove = !whiteToMove; // temporarily switch turns
            if (inCheck()) {
                moves.remove(moves.get(i)); // if that move puts the king in check that piece is 'pinned' and the move is removed from the list of valid mvoes
            }
            whiteToMove = !whiteToMove; // switch turns back
            Move.undoMove(); // undo the move
        }

        if (moves.size() == 0) { // if the moves ArrayList is empty
            if (inCheck()) { // if the current color is in check
                checkmate = true; // then they are in checkmate as
            } else {
                stalemate = true; // if they are not in checkmate they must be in stalemate
            }
        } else {
            checkmate = false;
            stalemate = false;
        }

        enpassantPossible = tempEnPassantPossible;
        return moves;
    }

    private boolean inCheck() {
        if (whiteToMove) { // if it is whites turn to move
            return underAttack(wKingLoc[0], wKingLoc[1]); // check if the white king is under attack (or 'in check')
        } else { // if it is not whites turn it must be blacks
            return underAttack(bKingLoc[0], bKingLoc[1]); // check if the black king is under attack
        }
    }

    private boolean underAttack(int r, int c) {
        whiteToMove = !whiteToMove;
        ArrayList<Move> oppMoves = getPossibleMoves(); // generate a list of all the opponents moves
        whiteToMove = !whiteToMove;

        for (int i = 0; i < oppMoves.size(); i++) { // loop through the list of opponent moves
            if ((oppMoves.get(i).endRow == r) && (oppMoves.get(i).endCol == c)) { // if one of those moves 'ends' on the same square as the piece we are checking then that piece is under attack
                return true;
            }
        }
        return false;
    }

    private ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        char turn, piece;

        for (int r = 0; r < board.length; r++) { // for every row on the board
            for (int c = 0; c < board[r].length; c++) { // for every column on the board
                turn = board[r][c].charAt(0);
                if ((turn == 'w' && whiteToMove) || (turn == 'b' && whiteToMove == false)) { // if the piece on that square has its turn to move
                    piece = board[r][c].charAt(1);

                    switch (piece) { // call the function that corresponds to the piece on that square
                        case 'P':
                            getPawnMoves(r, c, moves);
                            break;
                        case 'N':
                            getKnightMoves(r, c, moves);
                            break;
                        case 'B':
                            getBishopMoves(r, c, moves);
                            break;
                        case 'R':
                            getRookMoves(r, c, moves);
                            break;
                        case 'Q':
                            getQueenMoves(r, c, moves);
                            break;
                        case 'K':
                            getKingMoves(r, c, moves);
                            break;
                    }
                }
            }
        }

        return moves;
    }

    private void getPawnMoves(int r, int c, ArrayList<Move> moves) {
        ArrayList<Integer> tempStart;
        ArrayList<Integer> tempEnd;

        if (whiteToMove) {
            if (board[r - 1][c].equals("--")) { // if the square in front of the pawn is empty
                tempStart = new ArrayList<>();
                tempStart.add(r);
                tempStart.add(c);

                tempEnd = new ArrayList<>();
                tempEnd.add(r - 1);
                tempEnd.add(c);

                moves.add(new Move(tempStart, tempEnd)); // then it can move to that square so add the move to the list of valid moves

                if (r == 6 && board[r - 2][c].equals("--")) { // if the pawn has not yet moved AND the square 2 squares ahead is empty
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r - 2);
                    tempEnd.add(c);

                    moves.add(new Move(tempStart, tempEnd)); // then it can move to that square so add that move to the list of valid moves
                }
            }
            if (c - 1 >= 0) { // if the pawn is not on the left most column
                if (board[r - 1][c - 1].charAt(0) == 'b') { // if the piece there is an enemy (cannot move if the square is empty or has an ally)
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r - 1);
                    tempEnd.add(c - 1);

                    moves.add(new Move(tempStart, tempEnd)); // add that move to the ArrayList
                } else if (enpassantPossible.size() == 2 && r - 1 == enpassantPossible.get(0) && c - 1 == enpassantPossible.get(1)) { // if an en passant move is possible
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r - 1);
                    tempEnd.add(c - 1);

                    moves.add(new Move(tempStart, tempEnd, true)); // add that move to the ArrayList
                }
            }
            if (c + 1 <= 7) { // if the pawn is not on the right most column
                if (board[r - 1][c + 1].charAt(0) == 'b') { // if the piece there is an enemy (cannot move if the square is empty or has an ally)
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r - 1);
                    tempEnd.add(c + 1);

                    moves.add(new Move(tempStart, tempEnd)); // add that move to the list
                } else if (enpassantPossible.size() == 2 && r - 1 == enpassantPossible.get(0) && c + 1 == enpassantPossible.get(1)) { // if an en passant move is possible
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r - 1);
                    tempEnd.add(c + 1);

                    moves.add(new Move(tempStart, tempEnd, true)); // add that move to the list
                }
            }
        } else { // if it is not whites turn it must be blacks; following code works the exact same as white but in the opposite direction
            if (board[r + 1][c].equals("--")) {
                tempStart = new ArrayList<>();
                tempStart.add(r);
                tempStart.add(c);

                tempEnd = new ArrayList<>();
                tempEnd.add(r + 1);
                tempEnd.add(c);

                moves.add(new Move(tempStart, tempEnd));

                if (r == 1 && board[r + 2][c].equals("--")) {
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r + 2);
                    tempEnd.add(c);

                    moves.add(new Move(tempStart, tempEnd));
                }
            }
            if (c - 1 >= 0) {
                if (board[r + 1][c - 1].charAt(0) == 'b') {
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r + 1);
                    tempEnd.add(c - 1);

                    moves.add(new Move(tempStart, tempEnd));
                } else if (enpassantPossible.size() == 2 && r + 1 == enpassantPossible.get(0) && c - 1 == enpassantPossible.get(1)) {
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r + 1);
                    tempEnd.add(c - 1);

                    moves.add(new Move(tempStart, tempEnd, true));
                }
            }
            if (c + 1 <= 7) {
                if (board[r + 1][c + 1].charAt(0) == 'b') {
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r + 1);
                    tempEnd.add(c + 1);

                    moves.add(new Move(tempStart, tempEnd));
                } else if (enpassantPossible.size() == 2 && r + 1 == enpassantPossible.get(0) && c + 1 == enpassantPossible.get(1)) {
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(r + 1);
                    tempEnd.add(c + 1);

                    moves.add(new Move(tempStart, tempEnd, true));
                }
            }
        }
    }

    private void getRookMoves(int r, int c, ArrayList<Move> moves) {
        ArrayList<Integer> tempStart;
        ArrayList<Integer> tempEnd;
        int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        char enemy;
        int endRow, endCol;
        String endPiece;

        if (whiteToMove) { // determine which color the piece can capture
            enemy = 'b';
        } else {
            enemy = 'w';
        }

        for (int[] d : directions) { // for every possible direction a rook could move
            for (int i = 1; i < 8; i++) {
                endRow = r + d[0] * i; // find the end row that is currently being checked
                endCol = c + d[1] * i; // find the end column that is currently being checked

                if ((endRow >= 0 && endRow < 8) && (endCol >= 0 && endCol < 8)) { // if the move is not off the board
                    endPiece = board[endRow][endCol];
                    if (endPiece.equals("--")) { // if the end square is empty
                        tempStart = new ArrayList<>();
                        tempStart.add(r);
                        tempStart.add(c);

                        tempEnd = new ArrayList<>();
                        tempEnd.add(endRow);
                        tempEnd.add(endCol);

                        moves.add(new Move(tempStart, tempEnd)); // add the move to the Arraylist
                    } else if (endPiece.charAt(0) == enemy) { // if the end square has an enemy
                        tempStart = new ArrayList<>();
                        tempStart.add(r);
                        tempStart.add(c);

                        tempEnd = new ArrayList<>();
                        tempEnd.add(endRow);
                        tempEnd.add(endCol);

                        moves.add(new Move(tempStart, tempEnd)); // add that move the list

                        break; // stop looping, once you run into an enemy piece you cannot move the rook past it
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void getBishopMoves(int r, int c, ArrayList<Move> moves) {
        ArrayList<Integer> tempStart;
        ArrayList<Integer> tempEnd;
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        char enemy;
        int endRow, endCol;
        String endPiece;

        if (whiteToMove) { // determine which color the piece can capture
            enemy = 'b';
        } else {
            enemy = 'w';
        }

        for (int[] d : directions) { // for every possible direction a bishop could move in
            for (int i = 1; i < 8; i++) {
                endRow = r + d[0] * i; // find the end row that is currently being checked
                endCol = c + d[1] * i; // find the end column that is currently being checked

                if ((endRow >= 0 && endRow < 8) && (endCol >= 0 && endCol < 8)) { // if the move is not off the board
                    endPiece = board[endRow][endCol];

                    if (endPiece.equals("--")) { // if the end square is empty
                        tempStart = new ArrayList<>();
                        tempStart.add(r);
                        tempStart.add(c);

                        tempEnd = new ArrayList<>();
                        tempEnd.add(endRow);
                        tempEnd.add(endCol);

                        moves.add(new Move(tempStart, tempEnd)); // add that move to the ArrayList
                    } else if (endPiece.charAt(0) == enemy) { // if the end square has an enemy
                        tempStart = new ArrayList<>();
                        tempStart.add(r);
                        tempStart.add(c);

                        tempEnd = new ArrayList<>();
                        tempEnd.add(endRow);
                        tempEnd.add(endCol);

                        moves.add(new Move(tempStart, tempEnd)); // add that move to the ArrayList

                        break; // stop looping, once you run into an enemy the piece can not move past it
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void getKnightMoves(int r, int c, ArrayList<Move> moves) {
        ArrayList<Integer> tempStart;
        ArrayList<Integer> tempEnd;
        int[][] directions = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        char ally;
        int endRow, endCol;
        String endPiece;

        if (whiteToMove) { // determine which color can NOT be captured
            ally = 'w';
        } else {
            ally = 'b';
        }

        for (int d[] : directions) { // for everyone possible direction a knight can move
            endRow = r + d[0]; // determine the end row currently being checked
            endCol = c + d[1]; // determine the end column currently being checked

            if ((endRow >= 0 && endRow < 8) && (endCol >= 0 && endCol < 8)) { // if the move is not off the board
                endPiece = board[endRow][endCol];

                if (endPiece.charAt(0) != ally) { // if the end square is either empty or an ally
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(endRow);
                    tempEnd.add(endCol);

                    moves.add(new Move(tempStart, tempEnd)); // add that move to the ArrayList
                }
            }
        }
    }

    private void getQueenMoves(int r, int c, ArrayList<Move> moves) { // queen moves like a bishop and rook combined, so just call both functions to get queen moves
        getRookMoves(r, c, moves);
        getBishopMoves(r, c, moves);
    }

    private void getKingMoves(int r, int c, ArrayList<Move> moves) {
        ArrayList<Integer> tempStart;
        ArrayList<Integer> tempEnd;
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        char ally;
        int endRow, endCol;
        String endPiece;

        if (whiteToMove) { // determine which color can NOT be captured
            ally = 'w';
        } else {
            ally = 'b';
        }

        for (int i = 0; i < 8; i++) { // for every possible move the king can make
            endRow = r + directions[i][0]; // determine the end row currently being checked
            endCol = c + directions[i][1]; // determine the end column currently being checked

            if ((endRow >= 0 && endRow < 8) && (endCol >= 0 && endCol < 8)) { // if the move is not off the board
                endPiece = board[endRow][endCol];

                if (endPiece.charAt(0) != ally) { // if the end piece is empty or an enemy
                    tempStart = new ArrayList<>();
                    tempStart.add(r);
                    tempStart.add(c);

                    tempEnd = new ArrayList<>();
                    tempEnd.add(endRow);
                    tempEnd.add(endCol);

                    moves.add(new Move(tempStart, tempEnd)); // add that move to the ArrayList
                }
            }
        }
    }

    public void gameover() throws IOException{
        if (checkmate) { // the game has ended in checkmate

            if (!whiteToMove) { // white won
                Scanner scanner = new Scanner(new File("src/Chess/whiteWins.txt")); // create a new scanner object that scans the file storing white's win total
                wins = scanner.nextInt() + 1; // get the number stored in the file and add one to it
                FileWriter writer = new FileWriter("src/Chess/whiteWins.txt", false); // create a filewriter object that writes to the file storing white's win total
                PrintWriter pw = new PrintWriter(writer); // create a new printwriter object that prints to the white wins file
                pw.print(wins); // update the amount of white wins
                pw.close(); // close the print writer

            } else if (whiteToMove){ // black won, below does the same as above but for the black wins file
                Scanner scanner = new Scanner(new File("src/Chess/blackWins.txt"));
                wins = scanner.nextInt() + 1;
                FileWriter writer = new FileWriter("src/Chess/blackWins.txt", false);
                PrintWriter pw = new PrintWriter(writer);
                pw.print(wins);
                pw.close();
            }

        GUI.gameoverMessage(); // display the game over message
        }
    }
}
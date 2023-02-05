package Chess;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class GUI {

    public static JFrame frame;
    public static JPanel panel;
    private static String[] pieces;
    private static JLabel pieceLabel;
    private static HashMap<String, ImageIcon> images;
    public static int screenSize = 768;
    public static int squareSize = screenSize / 8;

    public GUI() { // create GUI constructor, is run everytime a GUI object is created
        frame = new JFrame(); // create a new JFrame object
        frame.setUndecorated(true); // remove the header and outline of the window
        frame.setSize(screenSize, screenSize); // set the size of the window
        frame.setLocationRelativeTo(null); // allow the use of x and y cords to place objects onto the window
        frame.setResizable(false); // dont allow the user to resize the window
        frame.setVisible(true); // set the frame to be visible

        panel = new chessPanel(); // create a new chessPanel object
        panel.setLayout(null); // set layout to Null
        panel.setBackground(Color.DARK_GRAY); // set background color to dark gray
        frame.add(panel, BorderLayout.CENTER); // add panel to the center of the frame
        frame.pack(); // make sure the frame is the right size for the panel

        new chessGame(); // create a new chessGame object, this is what starts the game

        pieces = new String[]{"wR", "wN", "wB", "wK", "wQ", "wP", "bR", "bN", "bB", "bK", "bQ", "bP"}; // create an Array of the types of pieces
        images = new HashMap(); // create an empty hashmap where the images will be stored

        for (int i = 0; i < pieces.length; i++) { // for every type of piece
            images.put(pieces[i], new ImageIcon("src/images/" + pieces[i] + ".png")); // create a  new ImageIcon object for the current piece and add it to the hashmap
        }

        drawPieces(); // draw the pieces onto the board
    }

    public static void drawPieces() {
        for (int r = 0; r < 8; r++){ // for every row on the board
            for (int c = 0; c < 8; c++){ // for every column on the board
                String piece = chessGame.board[r][c];

                if (!piece.equals("--")) { // if the square needs a piece drawn onto it
                    Image image = images.get(piece).getImage(); // create a new Image object of the current piece
                    pieceLabel = new JLabel(new ImageIcon(image.getScaledInstance(squareSize, squareSize,  java.awt.Image.SCALE_SMOOTH))); // create a new JLabel and set its icon to the piece image
                    pieceLabel.setBounds(c * squareSize, r * squareSize, squareSize, squareSize); // set the location and size of the current image
                    panel.add(pieceLabel); // add the image to the board
                }
                else {
                    panel.add(new JLabel("")); // add an empty JLabel to the board
                }
            }
        }
    }

    public static void gameoverMessage(){
        String message = "";
        if (chessGame.checkmate){ // if game ended in checkmate
            if (!chessGame.whiteToMove){ // if it is blacks turn (white delivered winning move)
                message = "White Wins! White has won " + chessGame.wins + " time(s)"; // display whites win message
            }
            else if (chessGame.whiteToMove){ // if it is whites turn (black delivered the winning move)
                message = "Black Wins! Black has won " + chessGame.wins + " time(s)"; // display blacks win message
            }
        }
        else if (chessGame.stalemate){ // if game ended in stalemate
            message = "Stalemate!\n That's a tie!"; // display stalemate message
        }

        JLabel endLabel = new JLabel(message); // create new JLabel object
        endLabel.setForeground(Color.RED); // set the font color to red
        endLabel.setFont(new Font("Verdana", Font.BOLD, 24)); // set the font and size
        panel.add(endLabel); // add the label to the panel
        endLabel.setBounds(125, 320, endLabel.getPreferredSize().width, endLabel.getPreferredSize().height); // set the labels size and location
        panel.setComponentZOrder(endLabel, 0); // set the label to be rendered lasst

        JLabel playAgainLabel = new JLabel("Press 'P' to play again"); // same as above but for the playAgainLabel variable
        playAgainLabel.setForeground(Color.RED);
        playAgainLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        panel.add(playAgainLabel);
        playAgainLabel.setBounds(240, 400, playAgainLabel.getPreferredSize().width, playAgainLabel.getPreferredSize().height);
        panel.setComponentZOrder(playAgainLabel, 1);
    }
}
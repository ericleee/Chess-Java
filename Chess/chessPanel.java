package Chess;

import javax.swing.*;
import java.awt.*;

public class chessPanel extends JPanel { // custom panel that draws the board, subclasses JPanel
    chessPanel(){ // chessPanel constructor, run everytime a chessPanel object is created
        super.setPreferredSize(new Dimension(GUI.screenSize, GUI.screenSize)); // set the preferred height and width of the window
    }
    public void paintComponent(Graphics g) { // override the default paintComponent function
        super.paintComponent(g); // call JPanel's paintComponent function

        g.setColor(Color.WHITE); // set color to white
        for (int row = 0; row < 8; row++) {
            for (int col = row % 2; col < 8; col += 2){
                g.fillRect(row * GUI.squareSize, col * GUI.squareSize, GUI.squareSize, GUI.squareSize); // draw an 8 x 8 board by drawing every other square in whtie
            }
        }
    }
}
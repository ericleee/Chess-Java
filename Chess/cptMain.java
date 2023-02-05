package Chess;

import javax.swing.SwingUtilities;

public class cptMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            } // create a new GUI object
        });
    }
}
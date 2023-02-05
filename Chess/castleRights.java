package Chess;

public class castleRights { // create a simple class that stores which of the 4 possible castling directions are still valid
    static public boolean wks; // white king side
    static public boolean wqs; // white queen side
    static public boolean bks; // black king side
    static public boolean bqs; // black queen side
    public castleRights(boolean wksInput, boolean wqsInput, boolean bksInput, boolean bqsInput){ // castleRights constructor, is run everytime a castleRights object is created
        wks = wksInput;
        wqs = wqsInput;
        bks = bksInput;
        bqs = bqsInput;
    }
}
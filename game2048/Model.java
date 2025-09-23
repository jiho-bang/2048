package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board _board;
    /** Current score. */
    private int _score;
    /** Maximum score so far.  Updated when game ends. */
    private int _maxScore;
    /** True iff game is ended. */
    private boolean _gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        _board = new Board(size);
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        _score = score;
        _maxScore = maxScore;
        _gameOver = gameOver;
        _board = new Board(rawValues, score);
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return _board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return _board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (_gameOver) {
            _maxScore = Math.max(_score, _maxScore);
        }
        return _gameOver;
    }

    /** Return the current score. */
    public int score() {
        return _score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return _maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        _score = 0;
        _gameOver = false;
        _board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        _board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Fill in this function.
        _board.setViewingPerspective(side);

        for (int c = 0; c < _board.size(); c++) {
            int emptySpaces = 0;
            for (int r = _board.size() - 1; r >= 0; r--) {
                if (_board.tile(c, r) == null) {
                    emptySpaces += 1;
                    continue;
                }
                Tile curr = _board.tile(c, r);
                if (_board.tile(c, r + emptySpaces) == null) {
                    _board.move(c, r + emptySpaces, curr);
                    changed = true;
                }
            }
        }

        for (int c = 0; c < _board.size(); c++) {
            for (int r = _board.size() - 1; r > 0; r--) {
                if (_board.tile(c, r) != null && _board.tile(c, r - 1) != null && _board.tile(c, r).value() == _board.tile(c, r - 1).value()) {
                    Tile mergeTile = _board.tile(c, r - 1);
                    _board.move(c, r, mergeTile);
                    changed = true;
                    _score += mergeTile.value() * 2;
                }
            }
        }

        for (int c = 0; c < _board.size(); c++) {
            int emptySpaces = 0;
            for (int r = _board.size() - 1; r >= 0; r--) {
                if (_board.tile(c, r) == null) {
                    emptySpaces += 1;
                    continue;
                }
                Tile curr = _board.tile(c, r);
                if (_board.tile(c, r + emptySpaces) == null) {
                    _board.move(c, r + emptySpaces, curr);
                    changed = true;
                }
            }
        }
        _board.setViewingPerspective(Side.NORTH);
        
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        _gameOver = checkGameOver(_board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     */
    public static boolean emptySpaceExists(Board b) {
        int dim = b.size();
        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                if (b.tile(c, r) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        int dim = b.size();
        for (int c = 0; c < dim; c++) {
            for (int r = 0; r < dim; r++) {
                if (b.tile(c, r) != null && b.tile(c, r).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        if (emptySpaceExists(b)) {
            return true;
        }

        int dim = b.size();
        for (int c = 0; c < dim; c++) {
            for (int r = 0; r < dim; r++) {
                Tile curr = b.tile(c, r);
                int cRight = c + 1;
                int rUp = r + 1;
                int cLeft = c - 1;
                int rDown = r - 1;
                if (cRight > 0 && cRight < 4 && rUp > 0 && rUp < 4) {
                    Tile right = b.tile(cRight, r);
                    Tile up = b.tile(c, rUp);
                    if (right != null && right.value() == curr.value()) {
                        return true;
                    }
                    if (up != null && up.value() == curr.value()) {
                        return true;
                    }
                }
                if (cLeft > 0 && cLeft < 4 && rDown > 0 && rDown < 4) {
                    Tile left = b.tile(cLeft, r);
                    Tile down = b.tile(c, rDown);
                    if (left != null && left.value() == curr.value()) {
                        return true;
                    }
                    if (down != null && down.value() == curr.value()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Returns the model as a string, used for debugging. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    /** Returns whether two models are equal. */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    /** Returns hash code of Model’s string. */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

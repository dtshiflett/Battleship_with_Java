package battleship;

import java.util.Arrays;

/**
 * Represents a single coordinate on the game board.
 *
 * A coordinate stores its zero-based horizontal and vertical indexes, its alphabetic
 * horizontal label, and the visible state associated with the board position.
 * Coordinates may be created from a textual board position or from numeric indexes.
 */
class Coordinate {
    protected char alphaX;
    protected int xIndex;
    protected int yIndex;
    protected GameBoard.PositionState state = GameBoard.PositionState.FOG;

    /**
     * Creates a coordinate from a board-position string and assigns the specified
     * position state.
     *
     * The coordinate string is interpreted as an alphabetic horizontal axis label
     * followed by a one-based vertical index. The horizontal label is converted to
     * uppercase, translated to its zero-based board index, and validated with the
     * vertical index against the board bounds.
     *
     * @param coordinate the board-position string to parse
     * @param state the position state to associate with this coordinate
     * @throws NumberFormatException if the numeric portion of the coordinate cannot be parsed
     * @throws IndexOutOfBoundsException if the parsed coordinate is outside the board bounds
     */
    public Coordinate(String coordinate, GameBoard.PositionState state) {
        this.state = state;
        coordinate = coordinate.toUpperCase();
        alphaX = coordinate.charAt(0);
        yIndex = Integer.parseInt(coordinate.substring(1)) - 1;
        xIndex = Arrays.binarySearch(GameBoard.alphaAxis, alphaX);
        // check that the parsed coordinate is within the board bounds
        validateBounds(xIndex, yIndex);
    }

    /**
     * Creates a coordinate from zero-based board indexes and assigns the specified
     * position state.
     *
     * The horizontal index is used to determine the alphabetic board label. Both
     * indexes are validated against the board bounds before the coordinate is
     * initialized.
     *
     * @param x the zero-based horizontal board index
     * @param y the zero-based vertical board index
     * @param state the position state to associate with this coordinate
     * @throws IndexOutOfBoundsException if either index is outside the board bounds
     */
    public Coordinate(int x, int y, GameBoard.PositionState state) {
        validateBounds(x, y);
        this.state = state;
        xIndex = x;
        yIndex = y;
        alphaX = GameBoard.alphaAxis[x];
    }

    /**
     * Validates that the specified zero-based board indexes are within the current
     * game board bounds.
     *
     * @param x the zero-based horizontal board index to validate
     * @param y the zero-based vertical board index to validate
     * @throws Main.InvalidCoordinatesException if either index is outside the board bounds
     */
    private static void validateBounds(int x, int y) {
        if (x < 0 || x >= GameBoard.boardSize || y < 0 || y >= GameBoard.boardSize) {
            throw new Main.InvalidCoordinatesException("Error! Coordinate is out of bounds");
        }
    }

    public boolean isSameCell(Coordinate other) {
        return xIndex == other.xIndex && yIndex == other.yIndex;
    }

    /**
     * Calculates the inclusive distance between two coordinates when they are aligned
     * on the same horizontal or vertical axis.
     *
     * @param a the first coordinate
     * @param b the second coordinate
     * @return the inclusive distance between the coordinates if they share the same
     *         x-index or y-index; -1 if they are not aligned
     */
    public static int calculateDistance(Coordinate a, Coordinate b) {
        if (a.xIndex == b.xIndex) {
            return Math.abs(a.yIndex - b.yIndex) + 1;
        }
        if (a.yIndex == b.yIndex) {
            return Math.abs(a.xIndex - b.xIndex) + 1;
        }
        // coordinates are not on the same line (vertical or horizontal)
        return -1;
    }

    @Override
    public String toString() {
        return "" + alphaX + (yIndex + 1);
    }
}

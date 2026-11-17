package battleship;

import java.util.Arrays;
import battleship.Main.InvalidCoordinatesException;


/**
 * Represents a Battleship game board for one player.
 *
 * The board tracks the state of each coordinate, the ships placed on the board,
 * which ships have been sunk, and whether the player has won. It supports ship
 * placement validation, cell state updates, shot resolution, win-condition checks,
 * and console rendering with optional fog that hides unhit ships.
 *
 * Coordinates are stored as zero-based row and column indexes. Ship placement
 * marks occupied cells as clear ship positions, successful strikes mark ship
 * cells as hits, and unsuccessful strikes mark fog cells as misses.
 */
class GameBoard {
    protected static final int boardSize = 10;
    protected PositionState[][] board = new PositionState[boardSize][boardSize];
    static char[] alphaAxis = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
    Ship[] ships = new Ship[Ship.ShipType.values().length];
    boolean[] sunkShips = new boolean[Ship.ShipType.values().length];
    String playerName = "";


    enum PositionState {
        FOG('~'), SHIP_CLEAR('O'), SHIP_HIT('X'), MISS('M');

        final char marker;

        PositionState(char marker) {
            this.marker = marker;
        }
    }

    /**
     * Constructs a new game board and initializes every position to the fog state.
     */
    public GameBoard() {
        for (PositionState[] row : board) {
            Arrays.fill(row, PositionState.FOG);
        }
    }

    public void setCellState(int x, int y, PositionState state) {
        board[x][y] = state;
    }

    public PositionState getCellState(int x, int y) {
        return board[x][y];
    }

    /**
     * Adds the specified ship to the game board and marks each of its occupied
     * coordinates as a clear ship position.
     *
     * @param ship the ship to place on the game board
     * @throws InvalidCoordinatesException if any coordinate occupied by the ship is
     *         already occupied by another ship
     */
    public void addShipToGameBoard(Ship ship) throws InvalidCoordinatesException {
        ships[ship.type.ordinal()] = ship;
        for (Coordinate position : ship.getShipCoordinates()) {
            // since this is the start of the game, all positions should be fog if not occupied by a ship
            if (getCellState(position.xIndex, position.yIndex) != PositionState.FOG) {
                throw new InvalidCoordinatesException("Error! Space(s) already occupied by another ship");
            }
            setCellState(position.xIndex, position.yIndex, PositionState.SHIP_CLEAR);
        }
    }

    /**
     * Determines whether the game is over by checking if all ships on the board
     * have been sunk.
     *
     * @return true if every ship has been sunk; false otherwise
     */
    public boolean isGameOver() {
        for (boolean sunk : sunkShips) {
            if (!sunk) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether any cell directly adjacent to the specified ship is already occupied.
     * Adjacent cells are the horizontal and vertical neighbors of each coordinate occupied
     * by the ship. If an occupied neighboring cell is found, the ship is considered too
     * close to another ship.
     *
     * @param ship the ship whose neighboring cells are checked
     * @throws InvalidCoordinatesException if a ship is located in any adjacent cell (vertical or horizontal)
     */
    public void checkForAdjacentShips(Ship ship) {
        for (Coordinate position : ship.getShipCoordinates()) {
            checkNeighbor(position.xIndex - 1, position.yIndex);
            checkNeighbor(position.xIndex + 1, position.yIndex);
            checkNeighbor(position.xIndex, position.yIndex - 1);
            checkNeighbor(position.xIndex, position.yIndex + 1);
        }
    }

    /**
     * Checks whether the specified board coordinate is within bounds and occupied by
     * a non-fog position. If the coordinate is valid and not fog, it overlaps another ship.
     *
     * @param x the row index of the neighboring cell to check
     * @param y the column index of the neighboring cell to check
     * @throws InvalidCoordinatesException if the specified in-bounds coordinate contains a non-fog position
     */
    private void checkNeighbor(int x, int y) throws InvalidCoordinatesException {
        if (x >= 0 && x < boardSize && y >= 0 && y < boardSize
                && board[x][y] != PositionState.FOG) {
            throw new InvalidCoordinatesException("Error! Ships can't overlap.");
        }
    }

    /**
     * Prints the current game board to standard output, including row and column labels.
     * When fog is enabled, clear ship positions are displayed as fog while hits, misses,
     * and other visible states are printed using their associated markers.
     *
     * @param fogOn true to hide clear ship positions as fog; false to display all cell states
     */
    public void printGameBoard(boolean fogOn) {
        // @TODO not dynamic with boardSize
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        for (int x = 0; x < boardSize; x++) {
            System.out.print(alphaAxis[x] + " ");
            for (int y = 0; y < boardSize; y++) {
                PositionState state = getCellState(x, y);
                if (fogOn && state == PositionState.SHIP_CLEAR) {
                    state = PositionState.FOG;
                }
                System.out.print(state.marker + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

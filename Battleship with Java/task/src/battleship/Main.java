/**
 * Provides classes and utilities for implementing the Battleship game.
 *
 * The package is intended to contain the core game model, rules, board
 * management, ship placement, attack handling, and related gameplay logic.
 */
package battleship;

import org.w3c.dom.ranges.RangeException;

import java.util.Arrays;
import java.util.Scanner;


/**
 * Provides the entry point and top-level control flow for a console-based,
 * two-player Battleship game.
 *
 * Handles game setup, player ship placement, turn progression, shot input,
 * shot resolution, hit and sink reporting, victory detection, and turn
 * transitions between players. All behavior is implemented through static
 * helper routines that coordinate interactions between game boards, ships,
 * coordinates, and console input.
 */
public class Main {
    public static void main(String[] args) {
        GameBoard player1 = new GameBoard();
        player1.playerName = "Player 1";
        GameBoard player2 = new GameBoard();
        player2.playerName = "Player 2";
        Scanner scanner = new Scanner(System.in);

        try {
            // let each player specify their ship locations
            inputPlayerShips(player1, scanner);
            clearScreen(scanner);
            inputPlayerShips(player2, scanner);
            clearScreen(scanner);
            playGame(player1, player2, scanner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the main turn-based game loop until one player wins or a shot resolution ends the game.
     * Displays the opponent's board with ships hidden, displays the current player's board,
     * prompts the active player for a shot, resolves the shot against the opponent, clears the
     * screen, and then swaps the active player and opponent for the next turn.
     *
     * @param currentPlayer the game board for the player whose turn is currently active
     * @param opponent the game board for the player being targeted by the current shot
     * @param scanner the scanner used to read player input and control screen-clearing prompts
     */
    private static void playGame(GameBoard currentPlayer, GameBoard opponent, Scanner scanner) {
        System.out.println("The game starts!\n");
        System.out.println("Take a shot!\n");

        // continue playing until one player wins
        while (!currentPlayer.isGameOver() && !opponent.isGameOver()) {
            opponent.printGameBoard(true);
            System.out.println("---------------------\n");
            currentPlayer.printGameBoard(false);

            Coordinate target = inputShot(scanner);
            if (resolveShot(opponent, target)) {
                break;
            }

            clearScreen(scanner);
            GameBoard previousPlayer = currentPlayer;
            currentPlayer = opponent;
            opponent = previousPlayer;
        }
    }

    /**
     * Reads shot coordinates from the specified scanner until a valid board coordinate is entered.
     * Invalid coordinates are reported to the user, and input is requested again.
     *
     * @param scanner the scanner used to read the shot coordinate input
     * @return a valid coordinate for the shot with an initial fog state
     */
    private static Coordinate inputShot(Scanner scanner) {
        while (true) {
            try {
                return new Coordinate(scanner.nextLine(), GameBoard.PositionState.FOG);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Error! You entered an invalid coordinate!  Try again:");
            }
        }
    }

    /**
     * Resolves a shot against the opponent's board at the specified target coordinate.
     * Marks the target as a hit when it strikes a ship, reports the hit, and marks the
     * target as a miss when no ship is struck.
     *
     * @param opponent the game board being targeted by the shot
     * @param target the coordinate selected as the shot target
     * @return true if the shot hits a ship and all opponent ships have been sunk; false otherwise
     */
    private static boolean resolveShot(GameBoard opponent, Coordinate target) {
        boolean hit = false;
        for (Ship ship : opponent.ships) {
            if (ship.strikeTarget(target)) {
                hit = true;
                opponent.setCellState(target.xIndex, target.yIndex, GameBoard.PositionState.SHIP_HIT);
                reportHit(opponent, ship);
            }
        }

        if (!hit) {
            System.out.println("You missed!\n");
            opponent.setCellState(target.xIndex, target.yIndex, GameBoard.PositionState.MISS);
        }
        return hit && opponent.isGameOver();
    }

    /**
     * Reports the result of a successful hit on the opponent's ship.
     * Marks the ship as sunk if all of its positions have been hit and it has not already
     * been reported as sunk. Prints a victory message when the hit sinks the opponent's
     * last remaining ship; otherwise, prints either a ship-sunk message or a generic hit
     * message.
     *
     * @param opponent the game board belonging to the player whose ship was hit
     * @param ship the ship that was hit
     */
    private static void reportHit(GameBoard opponent, Ship ship) {
        int shipIndex = ship.type.ordinal();
        if (ship.isSunk() && !opponent.sunkShips[shipIndex]) {
            opponent.sunkShips[shipIndex] = true;
            if (opponent.isGameOver()) {
                System.out.println("You sank the last ship. You won. Congratulations!");
            } else {
                System.out.println("You sank my " + ship.type.name + "!\n");
            }
        } else {
            System.out.println("You hit a ship!\n");
        }
    }

    /**
     * Prompts the specified player to place each ship on their game board.
     * Displays the current board before placement begins and after each ship is
     * successfully added. Ship coordinates are read from the provided scanner and
     * validated by the ship coordinate input routine before being added to the board.
     *
     * @param board the game board for the player placing ships
     * @param scanner the scanner used to read ship coordinate input
     */
    protected static void inputPlayerShips(GameBoard board, Scanner scanner) {
        System.out.println(board.playerName + ", place your ships on the game field");
        board.printGameBoard(false);
        for (Ship.ShipType shipType : Ship.ShipType.values()) {
            Ship ship = inputShipCoordinates(scanner, board, shipType);
            board.addShipToGameBoard(ship);
            board.printGameBoard(false);
        }
    }

    /**
     * Pauses the game until the user presses Enter, then prints blank lines to visually
     * clear the console before the next player's turn.
     *
     * @param scanner the scanner used to wait for the user's Enter key input
     */
    public static void clearScreen(Scanner scanner) {
        System.out.println("Press Enter and pass the turn to another player");
        scanner.nextLine();
        System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.flush();
    }

    /**
     * Prompts for and reads the start and end coordinates of a ship of the specified type until
     * valid coordinates are entered. Validates that exactly two coordinates are provided, that the
     * resulting ship has the correct orientation and length, and that it is not adjacent to existing
     * ships on the board.
     *
     * @param scanner the scanner used to read ship coordinate input
     * @param board the game board used to validate ship placement against existing ships
     * @param shipType the type of ship being placed
     * @return a valid ship created from the entered coordinates
     */
    protected static Ship inputShipCoordinates(Scanner scanner, GameBoard board, Ship.ShipType shipType) {
        System.out.println("Enter the coordinates of the " + shipType.name + " ("
                + shipType.numCells + " cells): ");
        while (true) {
            try {
                String[] coordinates = scanner.nextLine().split(" ");
                if (coordinates.length != 2) {
                    throw new IllegalArgumentException("Error! Exactly 2 coordinates expected.");
                }
                // sorting for easier math
                Arrays.sort(coordinates);
                Coordinate start = new Coordinate(coordinates[0], GameBoard.PositionState.SHIP_CLEAR);
                Coordinate end = new Coordinate(coordinates[1], GameBoard.PositionState.SHIP_CLEAR);
                Ship ship = new Ship(start, end, shipType);
                board.checkForAdjacentShips(ship);
                return ship;
            } catch (IllegalArgumentException | IndexOutOfBoundsException | RangeException | IllegalStateException e) {
                System.out.println(e.getMessage() + "  Try again:");
            }
        }
    }

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
    static class GameBoard {
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
         * @throws IllegalStateException if any coordinate occupied by the ship is
         *         already occupied by another ship
         */
        public void addShipToGameBoard(Ship ship) {
            ships[ship.type.ordinal()] = ship;
            for (Coordinate position : ship.positions) {
                // since this is the start of the game, all positions should be fog if not occupied by a ship
                if (getCellState(position.xIndex, position.yIndex) != PositionState.FOG) {
                    throw new IllegalStateException("Error! Space(s) already occupied by another ship");
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
         * @throws IllegalStateException if a ship is located in any adjacent cell (vertical or horizontal)
         */
        public void checkForAdjacentShips(Ship ship) {
            for (Coordinate position : ship.positions) {
                checkNeighbor(position.xIndex - 1, position.yIndex);
                checkNeighbor(position.xIndex + 1, position.yIndex);
                checkNeighbor(position.xIndex, position.yIndex - 1);
                checkNeighbor(position.xIndex, position.yIndex + 1);
            }
        }

        /**
         * Checks whether the specified board coordinate is within bounds and occupied by
         * a non-fog position. If the coordinate is valid and not fog, it is considered
         * too close to another ship.
         *
         * @param x the row index of the neighboring cell to check
         * @param y the column index of the neighboring cell to check
         * @throws IllegalStateException if the specified in-bounds coordinate contains a non-fog position
         */
        private void checkNeighbor(int x, int y) {
            if (x >= 0 && x < boardSize && y >= 0 && y < boardSize
                    && board[x][y] != PositionState.FOG) {
                throw new IllegalStateException("Error! You placed it too close to another ship");
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

    /**
     * Represents a single coordinate on the game board.
     *
     * A coordinate stores its zero-based horizontal and vertical indexes, its alphabetic
     * horizontal label, and the visible state associated with the board position.
     * Coordinates may be created from a textual board position or from numeric indexes.
     */
    static class Coordinate {
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
         * @throws IndexOutOfBoundsException if either index is outside the board bounds
         */
        private static void validateBounds(int x, int y) {
            if (x < 0 || x >= GameBoard.boardSize || y < 0 || y >= GameBoard.boardSize) {
                throw new IndexOutOfBoundsException("Error! Coordinate is out of bounds");
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

    /**
     * Represents a ship placed on a game board.
     *
     * A ship occupies a fixed sequence of contiguous coordinates determined by its
     * type and boundary coordinates. The occupied coordinates are initialized as
     * clear ship cells and may later be marked as hit when targeted by a strike.
     *
     * Ship placement is limited to horizontal or vertical alignment, and the number
     * of occupied cells must match the length required by the ship type.
     *
     * The ship can report whether a target coordinate strikes one of its occupied
     * cells, expose its occupied coordinates, provide its length, and determine
     * whether all of its cells have been hit.
     */
    static class Ship {
        private final Coordinate[] positions;
        public ShipType type;

        enum ShipType {
            AIRCRAFT_CARRIER("Aircraft Carrier", 5),
            BATTLESHIP("Battleship", 4),
            SUBMARINE("Submarine", 3),
            CRUISER("Cruiser", 3),
            DESTROYER("Destroyer", 2);

            final String name;
            final int numCells;

            ShipType(String name, int numCells) {
                this.name = name;
                this.numCells = numCells;
            }
        }

        /**
         * Creates a ship of the specified type between the given boundary coordinates.
         *
         * The start and end coordinates must be aligned horizontally or vertically, and
         * the inclusive distance between them must match the required length of the
         * specified ship type. Coordinates for each occupied ship cell are initialized
         * with the clear ship state.
         *
         * @param start the first boundary coordinate of the ship
         * @param end the second boundary coordinate of the ship
         * @param shipType the type of ship to create
         * @throws RangeException if the boundary coordinates are not aligned horizontally
         *         or vertically, or if their distance does not match the ship type length
         */
        public Ship(Coordinate start, Coordinate end, ShipType shipType) {
            type = shipType;
            // check that the coordinates are in a line (vertical or horizontal)
            int distance = Coordinate.calculateDistance(start, end);
            if (distance == -1) {
                // coordinates are not aligned
                throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR,
                        "Error! Wrong ship location!");
            }
            if (distance != shipType.numCells) {
                // distance does not match the ship type length
                throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR,
                        "Error! Wrong length of the " + shipType.name + "!");
            }

            positions = new Coordinate[distance];
            boolean horizontal = start.xIndex == end.xIndex;
            // reordering coordinates if they are not in the correct order
            int firstX = Math.min(start.xIndex, end.xIndex);
            int firstY = Math.min(start.yIndex, end.yIndex);
            for (int i = 0; i < distance; i++) {
                int x = firstX + (horizontal ? 0 : i);
                int y = firstY + (horizontal ? i : 0);
                positions[i] = new Coordinate(x, y, GameBoard.PositionState.SHIP_CLEAR);
            }
        }

        /**
         * Strikes the specified target coordinate if it matches one of this ship's occupied positions.
         *
         * When the target matches a ship position, that position is marked as hit.
         *
         * @param target the coordinate to strike
         * @return true if the target matches one of this ship's positions; false otherwise
         */
        public boolean strikeTarget(Coordinate target) {
            for (Coordinate position : positions) {
                if (position.isSameCell(target)) {
                    // marking this ship position as hit (not gameboard position)
                    position.state = GameBoard.PositionState.SHIP_HIT;
                    return true;
                }
            }
            return false;
        }

        public Coordinate[] getShipCoordinates() {
            return positions;
        }

        public int getShipLength() {
            return positions.length;
        }

        /**
         * Determines whether all positions occupied by this ship have been hit.
         *
         * @return true if every ship position is marked as hit; false otherwise
         */
        public boolean isSunk() {
            for (Coordinate position : positions) {
                if (position.state != GameBoard.PositionState.SHIP_HIT) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Coordinate position : positions) {
                result.append(position).append(' ');
            }
            return result.toString();
        }
    }
}
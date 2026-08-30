package battleship;


import org.w3c.dom.ranges.RangeException;

import java.util.Arrays;
import java.util.Scanner;

import static battleship.Main.GameBoard.alphaAxis;


public class Main {

    public static void main(String[] args) {
        GameBoard player1Board = new GameBoard();
        player1Board.playerName = new String("Player 1");
        GameBoard player2Board = new GameBoard();
        player2Board.playerName = new String("Player 2");

        Scanner sc = new Scanner(System.in);
        boolean isPlayer1Turn = true;
        GameBoard currentPlayer = player1Board;
        GameBoard opponentPlayer = player2Board;

        try {
            inputPlayerShips(player1Board, sc);
            clearScreen(sc);
            inputPlayerShips(player2Board, sc);
            clearScreen(sc);

            // play game
            boolean isValidShot = false;
            Coordinate target = null;


            System.out.println("The game starts!\n");
            System.out.println("Take a shot!\n");
            // continue game until all ships are sunk
            while (!currentPlayer.isGameOver() && !opponentPlayer.isGameOver()) {
                if (isPlayer1Turn) {
                    currentPlayer = player1Board;
                    opponentPlayer = player2Board;
                } else {
                    currentPlayer = player2Board;
                    opponentPlayer = player1Board;
                }
                opponentPlayer.printGameBoard(true);
                System.out.println("---------------------\n");
                currentPlayer.printGameBoard(false);
                // ask for the next shot

                // reset variable
                isValidShot = false;
                while (!isValidShot) {
                    try {
                        target = new Coordinate(sc.nextLine(), GameBoard.PositionState.FOG);
                        isValidShot = true;
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Error! You entered an invalid coordinate!  Try again:");
                    }
                }
                // try the shot
                boolean isMiss = true;
                for (Ship s : opponentPlayer.ships) {
                    if(s.strikeTarget(target)) {
                        // this is a hit!  update the game board
                        opponentPlayer.setCellState(target.xIndex, target.yIndex, GameBoard.PositionState.SHIP_HIT);
                        isMiss = false;
                        // if it's sunk and it wasn't sunk before this hit, it's a new sunk ship
                        if (s.isSunk() && !opponentPlayer.sunkShips[s.type.ordinal()]) {
                            // ship is sunk!
                            opponentPlayer.sunkShips[s.type.ordinal()] = true;
                            if (opponentPlayer.isGameOver()) {
                                System.out.println("You sank the last ship. You won. Congratulations!");

                            } else {
                                System.out.println("You sank my " + s.type.name + "!\n");
                            }
                        } else {
                            System.out.println("You hit a ship!\n");
                        }
                    }
                }
                if (isMiss) {
                    System.out.println("You missed!\n");
                    opponentPlayer.setCellState(target.xIndex, target.yIndex, GameBoard.PositionState.MISS);
                } else if (opponentPlayer.isGameOver()) {
                    // game is over so quit
                    break;
                }
                // end of turn, switch player
                clearScreen(sc);
                isPlayer1Turn = !isPlayer1Turn;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }

    protected static void inputPlayerShips(GameBoard board, Scanner sc) throws IllegalArgumentException {
        System.out.println(board.playerName + ", place your ships on the game field");
        board.printGameBoard(false);
        Ship ship = null;
        for (Ship.ShipType shipType : Ship.ShipType.values()) {
            ship = inputShipCoordinates(sc, board, shipType);
            board.addShipToGameBoard(ship);
            board.printGameBoard(false);
        }
    }

    public static void clearScreen(Scanner sc) {
        System.out.println("Press Enter and pass the turn to another player");
        sc.nextLine();
        System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.flush();
    }


    protected static Ship inputShipCoordinates(Scanner sc, GameBoard board, Ship.ShipType shipType) throws IllegalArgumentException {
        boolean askAgain = true;
        String coordinateInput;
        String[] coordinates;
        Coordinate c1;
        Coordinate c2;
        Ship ship = null;

        System.out.println("Enter the coordinates of the " + shipType.name + " ("
                + shipType.numCells + " cells): ");
        do {
            try {
                coordinateInput = sc.nextLine();

                // split it into individual coordinates
                coordinates = coordinateInput.split(" ");

                // check that there are 2 coordinates
                if (coordinates.length != 2) {
                    throw new IllegalArgumentException("Error! Exactly 2 coordinates expected.");
                }
                // sorting for easier math
                Arrays.sort(coordinates);

                // these constructors will validate the ship coordinates
                //      and throw exceptions if they are not valid
                c1 = new Coordinate(coordinates[0], GameBoard.PositionState.SHIP_CLEAR);
                c2 = new Coordinate(coordinates[1], GameBoard.PositionState.SHIP_CLEAR);
                ship = new Ship(c1, c2, shipType);
                board.checkForAdjacentShips(ship);

                // if it got this far, the coordinates are valid
                askAgain = false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + "  Try again:");
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage() + "  Try again:");
            } catch (RangeException e) {
                System.out.println(e.getMessage() + "  Try again:");
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage() + "  Try again:");
            }
        } while (askAgain);
        return ship;
    }


    static class GameBoard {
        protected static final int boardSize = 10;
        protected  PositionState[][] board = new PositionState[boardSize][boardSize];
        static char[] alphaAxis = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        Ship[] ships = new Ship[Ship.ShipType.values().length];
        boolean[] sunkShips = new boolean[Ship.ShipType.values().length];
        boolean isWinner = false;
        String playerName = "";


        enum PositionState {
            FOG('~'),
            SHIP_CLEAR('O'),
            SHIP_HIT('X'),
            MISS('M');

            char marker;

            PositionState(char x) {
                marker = x;
            }
        }

        public GameBoard() {
            //initialize all to '~'
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    setCellState(i, j, PositionState.FOG);
                }
            }
        }

        public void setCellState(int x, int y, PositionState ps) {
            board[x][y] = ps;
        }

        public PositionState getCellState(int x, int y) { return board[x][y]; }

        // throws IllegalStateException if any spaces on the board are already occupied
        public void addShipToGameBoard(Ship ship) throws IllegalStateException {
            ships[ship.type.ordinal()] = ship;
            for (Coordinate c : ship.positions) {
                if (getCellState(c.xIndex, c.yIndex) == PositionState.FOG) {
                    setCellState(c.xIndex, c.yIndex, PositionState.SHIP_CLEAR);
                } else {
                    throw new IllegalStateException("Error! Space(s) already occupied by another ship");
                }

            }
        }

        // if all ships are sunk, returns tru
        public boolean isGameOver() {
            for (boolean sunk : this.sunkShips) {
                if (!sunk) {
                    return false;
                }
            }
            return true;
        }

        // does not change ship or board, only checks 4 surrounding cells
        // @throws IllegalStateException if a ship is located in any adjacent cell (vertical or horizontal)
        public void checkForAdjacentShips(Ship ship) throws IllegalStateException {
            String errorAdjacent = new String("Error! You placed it too close to another ship");
            // check to see if it is too close to an existing ship (can't be adjacent)
            for (Coordinate c : ship.positions) {
                try {
                    // check cell above
                    if (this.board[c.xIndex - 1][c.yIndex] != PositionState.FOG) {
                        throw new IllegalStateException(errorAdjacent);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // coordinate must be on the top edge so just continue
                }
                try {
                    // check cell below
                    if (this.board[c.xIndex + 1][c.yIndex] != PositionState.FOG) {
                        throw new IllegalStateException(errorAdjacent);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // coordinate must be on the bottom edge so just continue
                }
                try {
                    // check cell to the left
                    if (this.board[c.xIndex][c.yIndex - 1] != PositionState.FOG) {
                        throw new IllegalStateException(errorAdjacent);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // coordinate must be on the left edge so just continue
                }
                try {
                    // check cell to the right
                    if (this.board[c.xIndex][c.yIndex + 1] != PositionState.FOG) {
                        throw new IllegalStateException(errorAdjacent);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // coordinate must be on the right edge so just continue
                }
            }
        }

        // returns true if it was a hit (also marks it on the board)
        // returns false if it was a miss (also marks it on the board)
        public boolean strikeTarget(Coordinate target) {
            if(board[target.xIndex][target.yIndex] == PositionState.FOG) {
                // this is a miss
                board[target.xIndex][target.yIndex] = PositionState.MISS;
                return false;
            } else if (board[target.xIndex][target.yIndex] == PositionState.SHIP_CLEAR) {
                // this is a hit
                board[target.xIndex][target.yIndex] = PositionState.SHIP_HIT;
                return true;
            }
            return false;
        }

        // fogOn == true will print all SHIP_CLEAR spots with FOG instead
        public void printGameBoard(boolean fogOn) {
            // @TODO not dynamic with boardSize
            System.out.println("  1 2 3 4 5 6 7 8 9 10");
            for (int i = 0; i < boardSize; i++) {
                System.out.print(alphaAxis[i] + " ");
                for (int j = 0; j < boardSize; j++) {
                    if (fogOn && (getCellState(i,j) == PositionState.SHIP_CLEAR)) {
                        System.out.print(PositionState.FOG.marker + " ");
                    } else {
                        System.out.print(getCellState(i, j).marker + " ");
                    }
                }
                System.out.print("\n");
            }
            System.out.println("");
        }
    }


    static class Coordinate {
        protected char alphaX;
        protected int xIndex;
        protected int yIndex;
        protected GameBoard.PositionState state = GameBoard.PositionState.FOG;

        public Coordinate(String str) { this(str, GameBoard.PositionState.FOG); }

        public Coordinate(String cStr, GameBoard.PositionState state) throws IndexOutOfBoundsException {
            this.state = state;
            cStr = cStr.toUpperCase();
            StringBuilder sb = new StringBuilder(cStr);
            //char[] temp = cStr.toUpperCase().toCharArray();
            alphaX = sb.charAt(0);
            yIndex = Integer.parseInt(sb.substring(1)) - 1;
            xIndex = Arrays.binarySearch(alphaAxis, alphaX);
            if (xIndex < 0 || xIndex > GameBoard.boardSize - 1 ||
                    yIndex < 0 || yIndex > GameBoard.boardSize - 1) {
                throw new IndexOutOfBoundsException("Error! Coordinate is out of bounds");
            }
        }

        public Coordinate(int x, int y) { this(x, y, GameBoard.PositionState.FOG); }

        public boolean isSameCell(Coordinate c) {
            if((this.xIndex == c.xIndex) && (this.yIndex == c.yIndex)) {
                return true;
            } else {
                return false;
            }
        }

        public Coordinate(int x, int y, GameBoard.PositionState state) throws IndexOutOfBoundsException {
            this.state = state;
            this.xIndex = x;
            this.yIndex = y;
            alphaX = alphaAxis[x];
            if (xIndex < 0 || xIndex > GameBoard.boardSize - 1 ||
                    yIndex < 0 || yIndex > GameBoard.boardSize - 1) {
                throw new IndexOutOfBoundsException("Error! Coordinate is out of bounds");
            }
        }

        // returns -1 if coordinates are not in same vertical or horizontal line
        public static int calculateDistance(Coordinate a, Coordinate b) throws RangeException {
            if (a.xIndex == b.xIndex) {
                return Math.abs(a.yIndex - b.yIndex) + 1;
            } else if (a.yIndex == b.yIndex) {
                return Math.abs(a.xIndex - b.xIndex) + 1;
            } else {
                // coordinates are not in a vertical or horizontal line
                return -1;
            }
        }

        public String toString() {
            return new String("" + this.alphaX + (this.yIndex + 1));
        }
    }

    static class Ship {
        private Coordinate[] positions;
        public ShipType type = null;

        enum ShipType {
            AIRCRAFT_CARRIER("Aircraft Carrier", 5),
            BATTLESHIP("Battleship", 4),
            SUBMARINE("Submarine", 3),
            CRUISER("Cruiser", 3),
            DESTROYER("Destroyer", 2);

            String name;
            int numCells;

            ShipType(String s, int x) {
                this.name = s;
                this.numCells = x;
            }
        }

        // throws RangeException if the coordinates are not on the same vertical or horizontal plane
        // throws RangeException if the distance between coordinates doesn't match the given ship type
        public Ship(Coordinate start, Coordinate end, ShipType shipType) throws RangeException {
            this.type = shipType;
            try {
                int distance = Coordinate.calculateDistance(start, end);
                // check to see if the coordinate were in a line (vertical or horizontal)
                if (distance == -1) {
                    throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR,
                            "Error! Wrong ship location!");
                } else if (distance != shipType.numCells) {
                    throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR,
                            "Error! Wrong length of the " + shipType.name + "!");
                }
                positions = new Coordinate[distance];

                // check for horizontal line
                if (start.xIndex == end.xIndex) {
                    // horizontal line found
                    // reordering if they are not sorted
                    if (end.yIndex < start.yIndex) {
                        Coordinate temp = start;
                        start = end;
                        end = temp;
                    }
                    // populate the coordinates between the points
                    for (int i = start.yIndex, index = 0; i <= end.yIndex; i++) {
                        positions[index++] = new Coordinate(start.xIndex, i, GameBoard.PositionState.SHIP_CLEAR);
                    }
                    // check for vertical line
                } else if (start.yIndex == end.yIndex) {
                    // vertical line found
                    // reordering if they are not sorted
                    if (end.xIndex < start.xIndex) {
                        Coordinate temp = start;
                        start = end;
                        end = temp;
                    }
                    // populate the coordinates between the points
                    for (int i = start.xIndex, index = 0; i <= end.xIndex; i++) {
                        positions[index++] = new Coordinate(i, start.yIndex, GameBoard.PositionState.SHIP_CLEAR);
                    }
                }
            } catch (RangeException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("Found exception in Ship constructor: " + e.toString());
                //throw e;
            }
        }

        // returns true if it was a hit
        // returns false if it was a miss
        public boolean strikeTarget(Coordinate target) {
            for (Coordinate c : this.positions) {
                // if the strike is on this ship's cell, mark it as hit
                if (c.isSameCell(target)) {
                    c.state = GameBoard.PositionState.SHIP_HIT;
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

        public boolean isSunk() {
            int hitCount = 0;
            for (Coordinate c : this.positions) {
                if (c.state == GameBoard.PositionState.SHIP_HIT) {
                    hitCount++;
                }
            }
            if (hitCount == this.positions.length) {
                return true;
            } else {
                return false;
            }
        }

        public String toString() {
            if (positions != null) {
                StringBuilder sb = new StringBuilder();
                for (Coordinate x : this.positions) {
                    sb.append(x.toString() + " ");
                }
                return sb.toString();
            } else {
                return new String("Error! No positions for this ship");
            }
        }
    }

}
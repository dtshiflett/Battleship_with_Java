package battleship;

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
        boolean allowAdjacentShips = false;
        try {
            System.out.println("Enter q, exit, or quit at any prompt to exit.");
            // allow user to decide if ships can be placed adjacent to each other
            allowAdjacentShips = askUserForAdjacentShipsPlacement(scanner);

            // let each player specify their ship locations
            inputPlayerShips(player1, scanner, allowAdjacentShips);
            clearScreen(scanner);
            inputPlayerShips(player2, scanner, allowAdjacentShips);
            clearScreen(scanner);
            playGame(player1, player2, scanner);
        } catch (ExitGameException e) {
            System.out.println("Exiting game.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ExitGameException extends RuntimeException {
        ExitGameException(String message) {
            super(message);
        }
    }
    static class InvalidCoordinatesException extends RuntimeException {
        InvalidCoordinatesException(String message) {
            super(message);
        }
    }

    /**
     * Reads the next line of input from the specified scanner and checks whether it is
     * an exit command. If the trimmed input is q, exit, or quit, an ExitGameException
     * is thrown; otherwise, the original untrimmed input is returned.
     *
     * @param scanner the scanner used to read the next line of input
     * @return the original line of input read from the scanner
     * @throws ExitGameException if the input is an exit command
     */
    private static String readInput(Scanner scanner) {
        String input = scanner.nextLine();
        String command = input.trim();
        if (command.equalsIgnoreCase("q") || command.equalsIgnoreCase("exit")
                || command.equalsIgnoreCase("quit")) {
            throw new ExitGameException("User wants to exit the program.");
        }
        return input;
    }

    /**
     * Prompts the user to choose whether adjacent ship placement is allowed.
     * Continues reading input until the user enters yes or no, ignoring case.
     *
     * @param scanner the scanner used to read the user's response
     * @return true if the user answers yes; false if the user answers no
     * @throws ExitGameException if the input is an exit command
     */
    private static boolean askUserForAdjacentShipsPlacement(Scanner scanner) {
        System.out.println("Do you want to allow adjacent ship placement? ");
        while (true) {
            String answer = readInput(scanner).trim();
            if (answer.equalsIgnoreCase("yes")) {
                return true;
            }
            if (answer.equalsIgnoreCase("no")) {
                return false;
            }
            System.out.println("Please answer yes or no:");
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
     * @throws ExitGameException if the input is an exit command
     */
    private static Coordinate inputShot(Scanner scanner) {
        while (true) {
            try {
                return new Coordinate(readInput(scanner), GameBoard.PositionState.FOG);
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
     * Prompts the specified player to place each ship on the game board.
     * Displays the board before placement begins and after each ship is added.
     *
     * @param board the game board for the player placing ships
     * @param scanner the scanner used to read ship coordinate input
     * @param allowAdjacentShips whether adjacent ship placement is allowed
     */
    protected static void inputPlayerShips(GameBoard board, Scanner scanner, boolean allowAdjacentShips) {
        System.out.println(board.playerName + ", place your ships on the game field");
        board.printGameBoard(false);
        for (Ship.ShipType shipType : Ship.ShipType.values()) {
            Ship ship = inputShipCoordinates(scanner, board, shipType, allowAdjacentShips);
            board.addShipToGameBoard(ship);
            board.printGameBoard(false);
        }
    }

    /**
     * Pauses the game until the user presses Enter, then prints blank lines to visually
     * clear the console before the next player's turn.
     *
     * @param scanner the scanner used to wait for the user's Enter key input
     * @throws ExitGameException if the input is an exit command
     */
    public static void clearScreen(Scanner scanner) {
        System.out.println("Press Enter and pass the turn to another player");
        readInput(scanner);
        System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.flush();
    }

    /**
     * Prompts the user to enter the start and end coordinates for a ship of the specified type.
     * Continues reading input until exactly two valid coordinates are provided and the resulting
     * ship placement satisfies the board validation rules, including adjacent-ship restrictions
     * when they are not allowed.
     *
     * @param scanner the scanner used to read coordinate input from the user
     * @param board the game board used to validate the ship placement
     * @param shipType the type of ship being placed
     * @param allowAdjacentShips whether ships are allowed to be placed adjacent to one another
     * @return a valid ship constructed from the entered coordinates
     * @throws ExitGameException if the input is an exit command
     */
    protected static Ship inputShipCoordinates(Scanner scanner, GameBoard board, Ship.ShipType shipType, boolean allowAdjacentShips) {
        System.out.println("Enter the coordinates of the " + shipType.name + " ("
                + shipType.numCells + " cells): ");
        while (true) {
            try {
                String[] coordinates = readInput(scanner).split(" ");
                if (coordinates.length != 2) {
                    throw new InvalidCoordinatesException("Error! Exactly 2 coordinates expected.");
                }
                // sorting for easier math
                Arrays.sort(coordinates);
                Coordinate start = new Coordinate(coordinates[0], GameBoard.PositionState.SHIP_CLEAR);
                Coordinate end = new Coordinate(coordinates[1], GameBoard.PositionState.SHIP_CLEAR);
                Ship ship = new Ship(start, end, shipType);
                // only check for adjacent ships if user doesn't allow them
                if (!allowAdjacentShips) {
                    board.checkForAdjacentShips(ship);
                }
                return ship;
            } catch (IllegalArgumentException | IndexOutOfBoundsException | InvalidCoordinatesException e) {
                System.out.println(e.getMessage() + "  Try again:");
            }
        }
    }
}
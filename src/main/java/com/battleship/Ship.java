package battleship;

import battleship.Main.InvalidCoordinatesException;

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
class Ship {
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
     * @throws InvalidCoordinatesException if the boundary coordinates are not aligned horizontally
     *         or vertically, or if their distance does not match the ship type length
     */
    public Ship(Coordinate start, Coordinate end, ShipType shipType) {
        type = shipType;
        // check that the coordinates are in a line (vertical or horizontal)
        int distance = Coordinate.calculateDistance(start, end);
        if (distance == -1) {
            // coordinates are not aligned
            throw new Main.InvalidCoordinatesException("Error! Wrong ship location!");
        }
        if (distance != shipType.numCells) {
            // distance does not match the ship type length
            throw new Main.InvalidCoordinatesException("Error! Wrong length of the " + shipType.name + "!");
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

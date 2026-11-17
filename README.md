# Command-Line Battleship

A classic terminal-based implementation of the strategy game Battleship built in pure Java. Play against a computer opponent with smart targeting logic right from your command line. I built this project to refresh Java skills, practice Object-Oriented Programming (OOP) principles, 2D array manipulation, and clean separation between game state and console rendering.



## Key Features
* **Flexible Ship Placement:** Place your 5 ships manually with coordinate validation.
* **Input Validation:** Gracefully handles out-of-bounds coordinates, duplicate shots, and invalid formatting without crashing.
* **Clean Board Rendering:** Displays your fleet and your shot history at the beginning of your turn.  Clears screen between player turns.
* **House Rules - Adjacent Placement:** Allow user to decide if adjacent ship placement is allowed (house rules).

## Prerequisites
* **JDK 17** or higher installed and added to your system `PATH`.


## How to Play
1. **Setup Phase:** Choose whether you want to allow adjacent ships. Input the two end coordinates for each ship.
2. **Fleet Roster:** Both players command 5 ships:
   * **Carrier** (5 spaces)
   * **Battleship** (4 spaces)
   * **Cruiser** (3 spaces)
   * **Submarine** (3 spaces)
   * **Destroyer** (2 spaces)
3. **Battle Phase:** Enter a column letter (`A–J`) followed by a row number (`1–10`), such as `C7`, to fire a shot.
4. **Winning:** Sink all 5 enemy ships before the computer sinks yours.

## Project Structure & Design
I organized the codebase around Object-Oriented principles to keep helper classes independent of console logic:

```text
src/main/java/com/battleship/
├── Main.java           # Entry point and main game loop
├── GameBoard.java          # 10x10 grid state, ship placement, and hit/miss tracking
├── Ship.java           # Ship size, coordinates, hit count, and sunk status
├── Coordinate.java     # Immutable value object parsing inputs like "A5" to (0, 4)
```

### Lessons Learned
* **Decoupling helper classes from Logic and breaking up logic into smaller functions:** This will make it easier to write unit tests for `GameBoard` and `Ship` without mocking console streams.
* **Using AI to help with refactoring:** To practice AI-driven refactoring, I prompted the IntelliJ IDEA to break up the `Main` class into smaller functions and suggest simplifications to logic.

## Running Tests
TBD

## Future Improvements
* [ ] Add ANSI color codes for red hits, blue water, and green ship icons.
* [ ] Add AI opponent that creates its own board and makes its own shots.
* [ ] Add difficulty levels (Easy: pure random shots; Hard: parity-based checkerboard hunting).



## Contact
Danyell Shiflett – [GitHub](https://github.com/dtshiflett) | [LinkedIn](http://www.linkedin.com/in/danyell-shiflett)

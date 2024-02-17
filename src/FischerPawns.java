import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Simple Program to determine the maximum amount of unprotected pawns in every legal
 * Fischer-Random-Chess starting position. 1. Generates all legal starting positions 2. Marks
 * protected pawns 3. Outputs some stats and the four starting positions found to have three
 * unprotected pawns
 */
public class FischerPawns {
  static String initialPieces = "RNBKQBNR";
  static char[] pieces = initialPieces.toCharArray();
  static Set<String> pieceRows = new HashSet<>();
  static Map<String, int[]> allStartingPositions = new HashMap<>();

  public static void main(String[] args) {
    generatePermutations(pieces, pieces.length);
    allStartingPositions = generatePawnRows(pieceRows);
    printStats(allStartingPositions);
    printThreePawnPositions(allStartingPositions);
  }

  /**
   * Generate Permutations via Heap Algorithm
   *
   * @param pieces initial board configuration (classical starting position)
   * @param iterationCounter step counter (decremented)
   */
  static void generatePermutations(char[] pieces, int iterationCounter) {
    if (iterationCounter == 1) {
      String position = String.valueOf(pieces);
      if (checkValidPosition(position)) {
        pieceRows.add(position);
      }
    }
    for (int i = 0; i < iterationCounter; i++) {
      generatePermutations(pieces, iterationCounter - 1);
      // if iteration is odd, swap first and last element
      if (iterationCounter % 2 == 1) {
        char temp = pieces[0];
        pieces[0] = pieces[iterationCounter - 1];
        pieces[iterationCounter - 1] = temp;
      } else { // If iteration is even, swap ith and last element
        char temp = pieces[i];
        pieces[i] = pieces[iterationCounter - 1];
        pieces[iterationCounter - 1] = temp;
      }
    }
  }

  /**
   * Combines first and second ranks to starting positions with marked pawn protection
   *
   * @param pieceRows all possible first rank configurations
   * @return all possible starting positions
   */
  static Map<String, int[]> generatePawnRows(Set<String> pieceRows) {
    Map<String, int[]> completePositions = new HashMap<>();
    for (String position : pieceRows) {
      int[] pawnStatus = unprotectedPawns(position);
      completePositions.put(position, pawnStatus);
    }
    return completePositions;
  }

  /**
   * Checks if position string is valid (rook-rule for castling, bishop opposite color) with regular
   * expressions
   *
   * @param position String representation of the generated random starting position
   * @return boolean if position is valid fischer-random position
   */
  static boolean checkValidPosition(String position) {
    Pattern rookPattern = Pattern.compile("R.*K.*R");
    Matcher rookMatcher = rookPattern.matcher(position);
    if (!rookMatcher.find()) {
      return false;
    }
    Pattern bishopPattern = Pattern.compile("B(..|....|......|)B");
    Matcher bishopMatcher = bishopPattern.matcher(position);
    return bishopMatcher.find();
  }

  /**
   * returns the status of the pawn row: 0 = protected 1 = unprotected (eases the counting of
   * unprotected pawns)
   *
   * @param position sString representation of the generated random starting position
   * @return int-array marking protection status
   */
  static int[] unprotectedPawns(String position) {
    int[] pawns = new int[] {1, 1, 1, 1, 1, 1, 1, 1}; // initially, all pawns are unprotected
    char[] pieces = position.toCharArray();
    for (int i = 0; i < pieces.length; i++) {
      char piece = pieces[i];
      switch (piece) { // Knight covers pawns two squares to its left and right
        case 'N':
          int left = i - 2;
          int right = i + 2;
          if (isInBounds(left)) pawns[left] = 0;
          if (isInBounds(right)) pawns[right] = 0;
          break;
        case 'B': // Bishop covers pawns one square to its left and right
          left = i - 1;
          right = i + 1;
          if (isInBounds(left)) pawns[left] = 0;
          if (isInBounds(right)) pawns[right] = 0;
          break;
        case 'R':
          pawns[i] = 0; // Rook covers pawn in front of it
          break;
        case 'Q', 'K': // Queen and King cover pawns to the left, in front and to the right
          pawns[i] = 0;
          left = i - 1;
          right = i + 1;
          if (isInBounds(left)) pawns[left] = 0;
          if (isInBounds(right)) pawns[right] = 0;
          break;
      }
    }
    return pawns;
  }

  /**
   * Checks if protected square of a piece is within the borders of the chessboard (e.g. Bishop on
   * A1 protects B2 and "B0", the latter is disregarded)
   *
   * @param index calculated index of the protected pawn (0-7)
   * @return only valid indices
   */
  static boolean isInBounds(int index) {
    return index >= 0 && index <= 7;
  }

  /**
   * Counts the number of unprotected pawns for every starting position
   *
   * @param pawnCount the desired amount of unprotected pawns
   * @param allStartingPositions all possible positions including their pawn status
   * @return number of positions with the desired pawnCount
   */
  static int countUnprotectedPawns(int pawnCount, Map<String, int[]> allStartingPositions) {
    int counter = 0;
    for (Map.Entry<String, int[]> entry : allStartingPositions.entrySet()) {
      int sum = IntStream.of(entry.getValue()).sum();
      if (sum == pawnCount) {
        counter++;
      }
    }
    return counter;
  }

  static void printStats(Map<String, int[]> allStartingPositions) {
    System.out.println("Starting Positions generated: " + allStartingPositions.size());
    System.out.println(
        "Positions with one unprotected pawn: " + countUnprotectedPawns(1, allStartingPositions));
    System.out.println(
        "Positions with two unprotected pawns: " + countUnprotectedPawns(2, allStartingPositions));
    System.out.println(
        "Positions with three unprotected pawns: "
            + countUnprotectedPawns(3, allStartingPositions));
    System.out.println(
        "Positions with four unprotected pawns: " + countUnprotectedPawns(4, allStartingPositions));
  }

  private static void printThreePawnPositions(Map<String, int[]> allStartingPositions) {
    System.out.println("\nFour Positions with three unprotected pawns:");
    System.out.println("(unprotected pawns highlighted)\n");
    for (Map.Entry<String, int[]> entry : allStartingPositions.entrySet()) {
      int sum = IntStream.of(entry.getValue()).sum();
      if (sum == 3) {
        printSinglePosition(entry.getKey(), entry.getValue());
      }
    }
  }

  // Helper Methods for Printing

  static void printSinglePosition(String position, int[] pawns) {
    char[] pawnArray = convertInttoCharArray(pawns);
    char[] pieceArray = position.toCharArray();
    for (int i = 0; i < pawnArray.length; i++) {
      pawnArray[i] = insertChessSymbols(pawnArray[i]);
      pieceArray[i] = insertChessSymbols(pieceArray[i]);
    }
    String pawnString = Arrays.toString(pawnArray).replace(',', ' ');
    String pieceString = Arrays.toString(pieceArray).replace(',', ' ');
    System.out.println(pawnString + "\n" + pieceString + "\n");
  }

  static char[] convertInttoCharArray(int[] array) {
    char[] convertedArray = new char[array.length];
    for (int i = 0; i < convertedArray.length; i++) {
      convertedArray[i] = Character.forDigit(array[i], 10);
    }
    return convertedArray;
  }

  static char insertChessSymbols(char piece) {
    return switch (piece) {
      case 'K' -> '♔';
      case 'Q' -> '♕';
      case 'R' -> '♖';
      case 'B' -> '♗';
      case 'N' -> '♘';
      case '0' -> '♙';
      case '1' -> '♟';
      default -> piece;
    };
  }
}

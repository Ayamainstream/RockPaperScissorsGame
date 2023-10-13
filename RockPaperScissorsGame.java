import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;

public class RockPaperScissorsGame {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        List<String> moves = Arrays.asList(args);

        if (args.length < 3 || args.length % 2 == 0 || !areMovesUnique(moves)) {
            System.out.println("Usage: java RockPaperScissorsGame [move1] [move2] [move3] ... (odd number of non-repeating strings)");
            System.exit(1);
        }

        int userMove, computerMove;
        byte[] key, computerHMAC;

        key = HMACGenerator.generateKey();
        computerMove = new SecureRandom().nextInt(moves.size());
        computerHMAC = HMACGenerator.calculateHMAC(key, moves.get(computerMove));

        System.out.println("HMAC: " + bytesToHex(computerHMAC));
        GameRules.displayMoves(moves);

        System.out.print("Enter your move: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        if (input.equals("?")) {
            GameRules.displayHelpTable(moves);
        } else if (input.equals("0")) {
            System.exit(0);
        } else {
            userMove = Integer.parseInt(input);
            if (userMove < 1 || userMove > moves.size()) {
                System.out.println("Invalid move. Please enter a valid move.");
            } else {
                userMove--;
                System.out.println("Your move: " + moves.get(userMove));
                System.out.println("Computer move: " + moves.get(computerMove));
                System.out.println("HMAC key: " + bytesToHex(key));
                System.out.println(GameRules.determineWinner(moves, userMove, computerMove));
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexStringBuilder.append(String.format("%02x", b));
        }
        return hexStringBuilder.toString();
    }

    private static boolean areMovesUnique(List<String> moves) {
        Set<String> uniqueMoves = new HashSet<>(moves);
        return uniqueMoves.size() == moves.size();
    }
}

class HMACGenerator {
    public static byte[] generateKey() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] key = new byte[32];
        random.nextBytes(key);
        return key;
    }

    public static byte[] calculateHMAC(byte[] key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
        hmac.init(keySpec);
        return hmac.doFinal(data.getBytes());
    }
}

class GameRules {
    public static String determineWinner(List<String> moves, int userMove, int computerMove) {
        int half = moves.size() / 2;

        if (userMove == computerMove) {
            return "Draw!";
        } else if ((userMove - computerMove + moves.size()) % moves.size() <= half) {
            return "You win!";
        } else {
            return "PC wins!";
        }
    }

    public static void displayMoves(List<String> moves) {
        System.out.println("Available moves:");
        for (int i = 0; i < moves.size(); i++) {
            System.out.println((i + 1) + " - " + moves.get(i));
        }
        System.out.println("0 - exit");
        System.out.println("? - help");
    }

    public static void displayHelpTable(List<String> moves) {
        int totalMoves = moves.size();

        String columns = "| %-15s |" + String.join("", Collections.nCopies(totalMoves, " %-10s |")) + "%n";

        String headerLine = "+-----------------+" + String.join("", Collections.nCopies(totalMoves, "------------+")) + "%n";
        System.out.format(headerLine);

        Object[] headers = new Object[totalMoves + 1];
        headers[0] = "v PC\\User >";
        for (int i = 0; i < totalMoves; i++) {
            headers[i + 1] = moves.get(i);
        }
        System.out.format(columns, headers);
        System.out.format(headerLine);

        for (int i = 0; i < totalMoves; i++) {
            Object[] row = new Object[totalMoves + 1];
            row[0] = moves.get(i);
            for (int j = 0; j < totalMoves; j++) {
                row[j + 1] = GameRules.determineWinner(moves, j, i);
            }
            System.out.format(columns, row);
            System.out.format(headerLine);
        }
    }
}
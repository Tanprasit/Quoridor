package players;

import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;
import moves.Move;
import quoridor.GameState2P;
import quoridor.Quoridor;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComputerPlayer extends QuoridorPlayer {

    private int indexOpponent;
    private int maxDepth = 0;

    private static long maxTime = TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);

    public ComputerPlayer(GameState2P state, int index, Quoridor game) {
        super(state, index, game);
    }

    // Discussed possible methods to break during iterative deepening when time exceeds with Thomas Petty.
    public void chooseMove() {
        long startTime = System.nanoTime();
        Move bestMove = null;

        for (maxDepth = 1;(System.nanoTime() - startTime) <= maxTime; maxDepth++) {

            List<Move> legalMoves = GameState2P.getLegalMoves(state, index);
            double bestScore = 0;

            for (Move m : legalMoves) {
                if ((System.nanoTime() - startTime) >= maxTime) {
                    break;
                }

                GameState2P next = m.doMove(state);
                double score = getMinScoreAlphaBeta(next, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

                if (bestMove == null || score > bestScore) {
                    bestMove = m;
                    bestScore = score;
                }
            }
        }

        System.out.println("BREAK! depth level: " + maxDepth);
        GameState2P newState = bestMove.doMove(state);
        game.doMove(index, newState);
    }

    /*
     * Consider all possible moves by our opponent
     */
    private double getMinScoreAlphaBeta(GameState2P s, int depth, double alpha, double beta) {
        double res;
        if (depth == 0 || s.isGameOver()) {
            res = s.evaluateState(index);
        } else {
            List<Move> opponentMoves = GameState2P.getLegalMoves(s, indexOpponent);
            res = Double.POSITIVE_INFINITY;
            for (Move move : opponentMoves) {
                GameState2P next = move.doMove(s);
                double score = getMaxScoreAlphaBeta(next, depth - 1, alpha, beta);
                res = Math.min(res, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return res;
    }

    /*
     * Consider all possible moves we can play
     */
    private double getMaxScoreAlphaBeta(GameState2P s, int depth, double alpha, double beta) {
        double res;
        if (depth == 0 || s.isGameOver()) {
            res = s.evaluateState(index);
        } else {
            List<Move> myMoves = GameState2P.getLegalMoves(s, index);
            res = Double.NEGATIVE_INFINITY;
            for (Move move : myMoves) {
                GameState2P next = move.doMove(s);
                double score = getMinScoreAlphaBeta(next, depth - 1, alpha, beta);
                res = Math.max(res, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return res;
    }
}

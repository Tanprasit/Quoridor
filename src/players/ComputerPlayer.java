package players;

import moves.Move;
import quoridor.GameState2P;
import quoridor.Quoridor;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComputerPlayer extends QuoridorPlayer {

    /**
     * Q: A brief overview of the speed-up you have been able to achieve
     *
     * Note - All players were tested against basic computer with maxDepth set to 5. Player will repeat the test 5 times
     * to find the average.
     *
     * A: Computer Player or Iterative Deepening player achieves depth level 21 on average without aspiration
     * search.
     *
     * Iterative with Aspiration search performs roughly the same as Iterative deepening. On average it ends up at
     * depth 21.
     *
     * Iterative with Transposition achieves depth level of 13 when it had won.
     *
     *
     */

    private long maxTime = TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);
    private int indexOpponent;

    public ComputerPlayer(GameState2P state, int index, Quoridor game) {
        super(state, index, game);
        indexOpponent = (index + 1) % 2;
    }

    public void chooseMove() {
        List<Move> legalMoves = GameState2P.getLegalMoves(state, index);
        long startTime = System.nanoTime();
        Move bestMove = null;
        int maxDepth;
        double bestScore = 0;

        // For iterative deepening we need to create a loop where the depth starts at one and will be incremented by one.
        for (maxDepth = 1; (System.nanoTime() - startTime) <= maxTime; maxDepth++) {

            for (Move m : legalMoves) {
                //  Continue until the maximum time had been exceeded
                if ((System.nanoTime() - startTime) >= maxTime) {
                    break;
                }

                GameState2P next = m.doMove(state);
                double score = getMinScoreAlphaBeta(next, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

                // We use greater than and equal to bestscore because we consider putting up walls to be better.
                // Even if the score is the same.
                if (bestMove == null || score >= bestScore) {
                    bestScore = score;
                    bestMove = m;
                }
            }
        }

        System.out.println("Depth: " + maxDepth + " itera score: " + bestScore);
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

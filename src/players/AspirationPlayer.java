package players;

import moves.Move;
import quoridor.GameState2P;
import quoridor.Quoridor;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AspirationPlayer extends QuoridorPlayer {

    private int indexOpponent;
    private long maxTime = TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);

    public AspirationPlayer(GameState2P state, int index, Quoridor game) {
        super(state, index, game);
        indexOpponent = (index + 1) % 2;
    }

    // Discussed possible methods to break during iterative deepening when time exceeds with Thomas Petty.
    public void chooseMove() {
        List<Move> legalMoves = GameState2P.getLegalMoves(state, index);
        long startTime = System.nanoTime();
        Move bestMove = null;
        double previousBestScore = 0;
        double bestScore = 0;
        int maxDepth;

        // w - small constant we want to use as the window between
        double window = 50;

        // We need to initialize alpha and beta -infinity and + infinity respectively.
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        // Initialize the iterative deepening loop.
        for (maxDepth = 1; (System.nanoTime() - startTime) <= maxTime; maxDepth++) {
            bestScore = 0;

            for (Move m : legalMoves) {
                // If we run out time we break out.
                if ((System.nanoTime() - startTime) > maxTime) {
                    break;
                }

                GameState2P next = m.doMove(state);
                double score = getMinScoreAlphaBeta(next, maxDepth, alpha, beta);

                // If the score that we had found was greater than beta or if it was less than alpha.
                // Then the score is not correct as we had pruned too much of the tree.
                // We need to perform the minimax score again with -inf and +inf, in order to search
                // tree without cutting out too much of the tree straightaway.
                if (score >= beta) { // fail high
                    beta = Double.POSITIVE_INFINITY;
                    score = getMinScoreAlphaBeta(next, maxDepth, score, beta);
                } else if (score <= alpha){ // fail low
                    alpha = Double.NEGATIVE_INFINITY;
                    score = getMinScoreAlphaBeta(next, maxDepth, alpha, score);
                }

                // Reinitialize alpha and beta with the aspiration window.
                alpha = previousBestScore - window;
                beta = previousBestScore + window;

                if (bestMove == null || score >= bestScore) {
                    bestMove = m;
                    bestScore = score;
                }
            }

            // Set the previous best score after we have finished iterative deepening of a certain depth.
            previousBestScore = bestScore;
        }

        System.out.println("Depth: " + maxDepth + " aspir score: " + bestScore);
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

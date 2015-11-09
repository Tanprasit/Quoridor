package players;

import moves.Move;
import quoridor.GameState2P;
import quoridor.Quoridor;
import transpositiontable.TranspositionEntry;
import transpositiontable.TranspositionTable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TranspositionPlayer extends QuoridorPlayer {

    private int indexOpponent;
    private static long maxTime = TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);
    private TranspositionTable maxTable = new TranspositionTable();
    private TranspositionTable minTable = new TranspositionTable();

    public TranspositionPlayer(GameState2P state, int index, Quoridor game) {
        super(state, index, game);
        indexOpponent = (index + 1) % 2;
    }

    @Override
    public void chooseMove() {
        long startTime = System.nanoTime();
        Move bestMove = null;
        double bestScore = 0;

        // We need to initialize alpha and beta -infinity and + infinity respectively.
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        int maxDepth;
        for (maxDepth = 1; (System.nanoTime() - startTime) <= maxTime; maxDepth++) {
            List<Move> legalMoves = GameState2P.getLegalMoves(state, index);
            bestScore = 0;

            for (Move m : legalMoves) {
                // If we run out time we break out.
                if ((System.nanoTime() - startTime) >= maxTime) {
                    break;
                }

                GameState2P next = m.doMove(state);
                double score = getMinScoreAlphaBeta(next, maxDepth, alpha, beta);

                if (bestMove == null || score >= bestScore) {
                    bestMove = m;
                    bestScore = score;
                }
            }

        }

        System.out.println("Depth: " + maxDepth + " trans score: " + bestScore);
        GameState2P newState = bestMove.doMove(state);
        game.doMove(index, newState);
    }

    /*
     * Consider all possible moves by our opponent
     */
    private double getMinScoreAlphaBeta(final GameState2P state, int depth, double alpha, double beta) {
        double res = Double.POSITIVE_INFINITY;
        double score;

        // We try to get the current opponent state from the transposition table.
        TranspositionEntry entry = minTable.getEntryFromGameState(state);

        // Get the opponent's moves.
        final List<Move> opponentMoves = GameState2P.getLegalMoves(state, indexOpponent);

        // Check if current opp state existed before in table and that the depth is less than the depth
        // stored in the entry. We need to check the states as well, as it is possible that two state has the same hashcode.
        // Then return the previous best minimax score for this state.
        if (null != entry && state.equals(entry.getGameState2P()) && depth <= entry.getDepth()) {
            res = entry.getMinimax();
        }

        if (depth == 0 || state.isGameOver()) {
            res = state.evaluateState(index);
        } else {

            Comparator<Move> comparator = new Comparator<Move>() {
                @Override
                public int compare(Move move1, Move move2) {
                    // Check if the current opponent moves exist in the transposition table. If it doesn't set minimax
                    // to positive infinity, in order words the worst result for the opponent.
                    int lhs = (null != minTable.getEntryFromGameState(move1.doMove(state)))
                            ? (int) minTable.getEntryFromGameState(move1.doMove(state)).getMinimax()
                            : (int) Double.POSITIVE_INFINITY;
                    int rhs = (null != minTable.getEntryFromGameState(move2.doMove(state)))
                            ? (int) minTable.getEntryFromGameState(move2.doMove(state)).getMinimax()
                            : (int) Double.POSITIVE_INFINITY;

                    // If right is lower swap.
                    if (lhs > rhs) {
                        return 1;
                    } else if (lhs == rhs) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };

            // For the minimax score we need to sort the opponent's moves by their scores lowest first, in order to optimise
            // the search.
            Collections.sort(opponentMoves, comparator);

            // The opponent will go through their moves and try to get the best minimax score for them.
            // Once a promising child move is found store it with the minimax value in the transposition table.
            for (Move move : opponentMoves) {
                GameState2P next = move.doMove(state);
                score = getMaxScoreAlphaBeta(next, depth - 1, alpha, beta);
                res = Math.min(res, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    //
                    minTable.addEntry(next, res, depth);
                    break;
                }
            }
        }
        return res;
    }

    /*
     * Consider all possible moves we can play
     */
    private double getMaxScoreAlphaBeta(final GameState2P state, int depth, double alpha, double beta) {
        double res = Double.NEGATIVE_INFINITY;
        double score;

        // We try to get the current player state from the transposition table.
        TranspositionEntry entry = maxTable.getEntryFromGameState(state);

        // Get the opponent's move.
        List<Move> myMoves = GameState2P.getLegalMoves(state, index);

        // Check if current player state existed before in table and that the depth is less than the depth
        // stored in the entry. We need to check the states as well, as it is possible that two state has the same hashcode.
        // Then return the previous best minimax score for this state.
        if (null != entry && state.equals(entry.getGameState2P()) && depth <= entry.getDepth()) {
            res = entry.getMinimax();
        }

        if (depth == 0 || state.isGameOver()) {
            res = state.evaluateState(index);
        } else {
            // Check if the current player moves exist in the transposition table. If it doesn't set minimax to
            // negative infinity, in order words the worst result for the player.
            final Comparator<Move> comparator = new Comparator<Move>() {
                @Override
                public int compare(Move move1, Move move2) {
                    int lhs = (null != maxTable.getEntryFromGameState(move1.doMove(state)))
                            ? (int) maxTable.getEntryFromGameState(move1.doMove(state)).getMinimax()
                            : (int) Double.POSITIVE_INFINITY;
                    int rhs = (null != maxTable.getEntryFromGameState(move2.doMove(state)))
                            ? (int) maxTable.getEntryFromGameState(move2.doMove(state)).getMinimax()
                            : (int) Double.POSITIVE_INFINITY;

                    // If right is higher swap.
                    if (lhs < rhs) {
                        return 1;
                    } else if (lhs == rhs) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };

            // For the minimax score we need to sort the opponent's moves by their scores lowest first, in order to optimise
            // the search.
            Collections.sort(myMoves, comparator);

            // The player will go through their moves and try to get the best minimax score for them.
            // Once a promising child move is found store it with the minimax value in the transposition table.
            for (Move move : myMoves) {
                GameState2P next = move.doMove(state);
                score = getMinScoreAlphaBeta(next, depth - 1, alpha, beta);
                res = Math.max(res, score);
                alpha = Math.max(alpha, score);

                if (beta <= alpha) {
                    maxTable.addEntry(next, res, depth);
                    break;
                }
            }
        }
        return res;
    }
}

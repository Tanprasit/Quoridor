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
    private TranspositionTable minTable = new TranspositionTable();
    private TranspositionTable maxTable = new TranspositionTable();

    public TranspositionPlayer(GameState2P state, int index, Quoridor game) {
        super(state, index, game);
        indexOpponent = (index + 1) % 2;
    }

    @Override
    public void chooseMove() {
        long startTime = System.nanoTime();
        Move bestMove = null;
        double bestScore = 0;

        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        int maxDepth;
        for (maxDepth = 1; (System.nanoTime() - startTime) <= maxTime; maxDepth++) {
            List<Move> legalMoves = GameState2P.getLegalMoves(state, index);
            bestScore = 0;

            for (Move m : legalMoves) {
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
        TranspositionEntry entry = maxTable.getEntryFromGameState(state);
        List<Move> opponentMoves = GameState2P.getLegalMoves(state, indexOpponent);

        if (null != entry && state.equals(entry.getGameState2P()) && depth <= entry.getDepth()) {
            return entry.getMinimax();
        }

        if (depth == 0 || state.isGameOver()) {
            res = state.evaluateState(index);
        } else {
            Comparator<Move> comparator = new Comparator<Move>() {
                @Override
                public int compare(Move move1, Move move2) {
                    int lhs = (null != maxTable.getEntryFromGameState(move1.doMove(state)))
                            ? (int) maxTable.getEntryFromGameState(move1.doMove(state)).getMinimax()
                            : (int) Double.NEGATIVE_INFINITY;
                    int rhs = (null != maxTable.getEntryFromGameState(move2.doMove(state)))
                            ? (int) maxTable.getEntryFromGameState(move2.doMove(state)).getMinimax()
                            : (int) Double.NEGATIVE_INFINITY;

                    if (lhs > rhs) {
                        return 1;
                    } else if (lhs == rhs) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };


            Collections.sort(opponentMoves, comparator);

            for (Move move : opponentMoves) {
                GameState2P next = move.doMove(state);
                score = getMaxScoreAlphaBeta(next, depth - 1, alpha, beta);
                res = Math.min(res, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    maxTable.addEntry(state, res, depth);
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
        TranspositionEntry entry = minTable.getEntryFromGameState(state);
        List<Move> myMoves = GameState2P.getLegalMoves(state, index);

        if (null != entry && state.equals(entry.getGameState2P()) && depth <= entry.getDepth()) {
            return entry.getMinimax();
        }

        if (depth == 0 || state.isGameOver()) {
            res = state.evaluateState(index);
        } else {
            Comparator<Move> comparator = new Comparator<Move>() {
                @Override
                public int compare(Move move1, Move move2) {
                    int lhs = (null != minTable.getEntryFromGameState(move1.doMove(state)))
                            ? (int) minTable.getEntryFromGameState(move1.doMove(state)).getMinimax()
                            : (int) Double.POSITIVE_INFINITY;
                    int rhs = (null != minTable.getEntryFromGameState(move2.doMove(state)))
                            ? (int) minTable.getEntryFromGameState(move2.doMove(state)).getMinimax()
                            : (int) Double.POSITIVE_INFINITY;

                    if (lhs < rhs) {
                        return 1;
                    } else if (lhs == rhs) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };

            Collections.sort(myMoves, comparator);
            for (Move move : myMoves) {
                GameState2P next = move.doMove(state);
                score = getMinScoreAlphaBeta(next, depth - 1, alpha, beta);
                res = Math.max(res, score);
                alpha = Math.max(alpha, score);

                if (beta <= alpha) {
                    minTable.addEntry(state, res, depth);
                    break;
                }
            }
        }
        return res;
    }
}

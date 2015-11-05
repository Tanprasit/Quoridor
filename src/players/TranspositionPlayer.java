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

    private int maxDepth;
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
        double score;

        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        for (maxDepth = 1; (System.nanoTime() - startTime) <= maxTime; maxDepth++) {

            List<Move> legalMoves = GameState2P.getLegalMoves(state, index);

            for (Move m : legalMoves) {
                if ((System.nanoTime() - startTime) >= maxTime) {
                    break;
                }

                GameState2P next = m.doMove(state);
                score = getMinScoreAlphaBeta(next, maxDepth, alpha, beta);

                if (bestMove == null || score > bestScore) {
                    bestMove = m;
                    bestScore = score;
                }
            }

        }
        System.out.println("Depth level: " + maxDepth + " trans score: " + bestScore);
        GameState2P newState = bestMove.doMove(state);
        game.doMove(index, newState);
    }

    /*
     * Consider all possible moves by our opponent
     */
    private double getMinScoreAlphaBeta(final GameState2P s, int depth, double alpha, double beta) {
        double res;

        if (depth == 0 || s.isGameOver()) {
            res = s.evaluateState(index);
        } else {
            TranspositionEntry entry = maxTable.getEntryFromGameState(s);

            if (null == entry) {
                List<Move> opponentMoves = GameState2P.getLegalMoves(s, indexOpponent);
                res = Double.POSITIVE_INFINITY;
                for (Move move : opponentMoves) {
                    GameState2P next = move.doMove(s);
                    double score = getMaxScoreAlphaBeta(next, depth - 1, alpha, beta);
                    addNewEntryToTable(next, maxTable, score, depth - 1);
                    res = Math.min(res, score);
                    beta = Math.min(beta, score);
                    if (beta <= alpha) {
                        break;
                    }
                }
            } else if (depth <= entry.getDepth() && s.equals(entry.getGameState2P())) {
                res = entry.getMinimax();
            } else {
                List<Move> opponentMoves = GameState2P.getLegalMoves(s, indexOpponent);

                Comparator<Move> comparator = new Comparator<Move>() {
                    @Override
                    public int compare(Move move1, Move move2) {
                        int lhs = (null != maxTable.getEntryFromGameState(move1.doMove(s)))
                                ? (int) maxTable.getEntryFromGameState(move1.doMove(s)).getMinimax()
                                : (int) Double.NEGATIVE_INFINITY;
                        int rhs = (null != maxTable.getEntryFromGameState(move2.doMove(s)))
                                ? (int) maxTable.getEntryFromGameState(move2.doMove(s)).getMinimax()
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

                res = Double.POSITIVE_INFINITY;
                for (Move move : opponentMoves) {
                    GameState2P next = move.doMove(s);
                    double score = getMaxScoreAlphaBeta(next, depth - 1, alpha, beta);
                    addNewEntryToTable(next, maxTable, score, depth - 1);
                    res = Math.min(res, score);
                    beta = Math.min(beta, score);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return res;
    }

    /*
     * Consider all possible moves we can play
     */
    private double getMaxScoreAlphaBeta(final GameState2P s, int depth, double alpha, double beta) {
        double res;

        if (depth == 0 || s.isGameOver()) {
            res = s.evaluateState(index);
        } else {
            TranspositionEntry entry = minTable.getEntryFromGameState(s);

            if (null == entry) {
                List<Move> myMoves = GameState2P.getLegalMoves(s, indexOpponent);
                res = Double.NEGATIVE_INFINITY;
                for (Move move : myMoves) {
                    GameState2P next = move.doMove(s);
                    double score = getMinScoreAlphaBeta(next, depth - 1, alpha, beta);
                    addNewEntryToTable(next, minTable, score, depth - 1);
                    res = Math.min(res, score);
                    beta = Math.min(beta, score);
                    if (beta <= alpha) {
                        break;
                    }
                }
            } else if (depth <= entry.getDepth() && s.equals(entry.getGameState2P())) {
                res = entry.getMinimax();
            } else {
                List<Move> myMoves = GameState2P.getLegalMoves(s, index);

                Comparator<Move> comparator = new Comparator<Move>() {
                    @Override
                    public int compare(Move move1, Move move2) {
                        int lhs = (null != minTable.getEntryFromGameState(move1.doMove(s)))
                                ? (int) minTable.getEntryFromGameState(move1.doMove(s)).getMinimax()
                                : (int) Double.POSITIVE_INFINITY;
                        int rhs = (null != minTable.getEntryFromGameState(move2.doMove(s)))
                                ? (int) minTable.getEntryFromGameState(move2.doMove(s)).getMinimax()
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

                res = Double.NEGATIVE_INFINITY;
                for (Move move : myMoves) {
                    GameState2P next = move.doMove(s);
                    double score = getMinScoreAlphaBeta(next, depth - 1, alpha, beta);
                    addNewEntryToTable(next, minTable, score, depth - 1);
                    res = Math.max(res, score);
                    alpha = Math.max(alpha, score);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return res;
    }

    private void addNewEntryToTable(GameState2P gameState2P, TranspositionTable table, double score, int depth) {
        TranspositionEntry newEntry = new TranspositionEntry(gameState2P, score, depth);
        table.addTranspositionEntry(newEntry);
    }
}

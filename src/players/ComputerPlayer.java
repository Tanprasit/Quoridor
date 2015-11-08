package players;

import moves.Move;
import quoridor.GameState2P;
import quoridor.Quoridor;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComputerPlayer extends QuoridorPlayer {

    /**
     *
     * Q: A brief overview of the limitations of your implementation that you are aware of.
     *
     * A: All task had been implemented.
     *
     * Bugs - Going first improves the player's chance of winning. I think it is possible that when player 1 player
     *        somewhat optimally, player 2 will eventually run out of possible counter moves due to the board's
     *        size/ dimension.
     *
     *
     *
     * Q: A brief overview of any external sources you have used.
     *
     * A: I had used the following wikipedia pages extensively, especially the algorithms section.
     *      - https://en.wikipedia.org/wiki/Iterative_deepening_depth-first_search
     *      - https://en.wikipedia.org/wiki/Transposition_table
     *
     *  I had also used the following pdf to grasp a better understanding of each search.
     *      -  http://homepages.cwi.nl/~paulk/theses/Carolus.pdf
     *
     *  I had discussed on a high abstracted level the problems like the depth level that searches can achieve and
     *  the way reordering should work. I had also discussed whether or not a player could be the other.
     *      students:
     *      - Kyle Pinheiro
     *      - Thomas Petty
     *      - Christopher Paterson
     *      - Matthew Jones
     *
     *
     *
     * Q: A brief overview of the speed-up you have been able to achieve.
     *
     * Note - All players were tested against basic computer with maxDepth set to 3. Each player will be played 5 times
     * to find the average depth when the game is over. Because getting the depth at the end of the game is an unrealistic
     * comparison between the players I will take an average of the 6th depth level.
     *
     * A: Computer Player or Iterative Deepening player achieves depth level 12 on average without aspiration
     * search.
     *
     * Iterative with Aspiration search performs a little better than Iterative deepening. On average it ends up at
     * depth 14.
     *
     * Iterative with Transposition achieves depth level of 45. However unlike the other two it had lost against it's
     * opponent.
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

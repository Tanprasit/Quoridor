package transpositiontable;

import quoridor.GameState2P;

public class TranspositionEntry {

    private GameState2P gameState2P;
    private double minimax;
    private int depth;

    public TranspositionEntry(GameState2P gameState2P, double minimax, int depth) {
        this.gameState2P = gameState2P;
        this.minimax = minimax;
        this.depth = depth;
    }

    public GameState2P getGameState2P() {
        return gameState2P;
    }

    public void setGameState2P(GameState2P gameState2P) {
        this.gameState2P = gameState2P;
    }

    public double getMinimax() {
        return minimax;
    }

    public void setMinimax(double minimax) {
        this.minimax = minimax;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}

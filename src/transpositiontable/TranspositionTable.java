package transpositiontable;

import quoridor.GameState2P;

public class TranspositionTable {

    private TranspositionEntry[] transpositionTable;

    public TranspositionTable() {
        this.transpositionTable = new TranspositionEntry[(int) 10e6];
    }

    public void addEntry(GameState2P gameState2P, double minimax, int depth) {
        TranspositionEntry transpositionEntry = new TranspositionEntry(gameState2P, minimax, depth);
        int index = transpositionEntry.getGameState2P().hashCode() % transpositionTable.length;
        this.transpositionTable[index] = transpositionEntry;
    }

    public TranspositionEntry getEntryFromGameState(GameState2P gameState2P) {
        int index = gameState2P.hashCode() % transpositionTable.length;
        return this.transpositionTable[index];
    }
}

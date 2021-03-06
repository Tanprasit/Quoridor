package quoridor;

import players.*;

/**
 *
 * @author steven
 */
public class Quoridor {

    GameState2P state;
    QuoridorPlayer[] players;
    GameDisplay display;

    public Quoridor() {
        state = new GameState2P();
        display = new GameDisplay(state);
        players = new QuoridorPlayer[2];
        players[0] = new TranspositionPlayer(state, 0, this);
        players[1] = new BasicComputerPlayer(state, 1, this);
        for (int i = 0; i < 2; i++) {
            players[i].setDisplay(display);
        }
        players[0].chooseMove();
    }

    public void doMove(int playerIndex, GameState2P newState) {
        state = newState;
        for (int i = 0; i < 2; i++) {
            players[i].setState(newState);
        }
        final int nextIndex = (playerIndex + 1) % 2;
        display.updateState(newState);
        if (!newState.isGameOver()) {
            /*
             * Run the method chooseMove in a separate thread
             * to avoid the GUI from becoming unresponsive while 
             * the next move by the computer player is being computed
             */ 
            new Thread() {
                public void run() {
                    players[nextIndex].chooseMove();
                }
            }.start();
        }
        else
            java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public static void main(String[] args) {
        new Quoridor();
    }
}

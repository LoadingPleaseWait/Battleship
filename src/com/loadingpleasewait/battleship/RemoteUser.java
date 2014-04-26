package com.loadingpleasewait.battleship;

import java.rmi.RemoteException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class RemoteUser extends User implements RemotePlayer {

	public static final int MAX_GUESS = 1000;

	private static final long serialVersionUID = -4008309853242923001L;

	private String lastGuess;
	private boolean placedShips;
	private boolean opponentLeft;
	private boolean rematch;
	private boolean rematchRequested;
	private int guessedNumber = -1;
	private boolean goingFirst;
	private boolean goingFirstSet;

	public RemoteUser(Board playerBoard, JTextField field) throws RemoteException {
		super(playerBoard, null, field);
	}

	@Override
	public int getNumberGuess() throws RemoteException {
		while (guessedNumber == -1) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return guessedNumber;
	}

	/**
	 * @throws NullPointerException
	 *             user exited option pane
	 */
	public void guessNumber() throws NullPointerException {
		// ask user to guess a number
		while (guessedNumber == -1) {
			try {
				guessedNumber = Integer.parseInt(JOptionPane.showInputDialog(getTextField().getTopLevelAncestor(),
						"Enter number between 1 and 1000. Whoever's number is closer gets to go first."));
			} catch (NumberFormatException ex) {

			}
			if (guessedNumber == -1 || guessedNumber < 1 || guessedNumber > MAX_GUESS) {
				JOptionPane.showMessageDialog(getTextField().getTopLevelAncestor(), "That was not a valid number");
				guessedNumber = -1;
			}
		}
	}

	@Override
	public void requestGuessNumber() throws RemoteException {
		// previous numbers were the same so guessing must be done again
		guessNumber();
	}

	@Override
	public boolean wantsRematch() throws RemoteException {
		while (!rematchRequested) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return rematch;
	}

	public void requestRematch() {
		int choice = JOptionPane.NO_OPTION;
		try {
			// ask user if they want a rematch with an option pane
			choice = JOptionPane.showConfirmDialog(getTextField().getTopLevelAncestor(), "Would you like a rematch?",
					"Rematch", JOptionPane.YES_NO_OPTION);
		} catch (NullPointerException ex) {

		}
		rematch = choice == JOptionPane.YES_OPTION;
		rematchRequested = true;
	}

	@Override
	public synchronized String getRemoteGuess() throws RemoteException {
		// wait for local user to guess
		while (lastGuess == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {

			}
		}
		String guess = lastGuess;
		lastGuess = null;
		return guess;
	}

	@Override
	public void setRemoteGuessBoard(Board board) throws RemoteException {
		setGuessBoard(board);
	}

	@Override
	public Board getRemotePlayerBoard() throws RemoteException {
		return getPlayerBoard();
	}

	@Override
	public Board getRemoteGuessBoard() throws RemoteException {
		return getGuessBoard();
	}

	@Override
	public synchronized String getGuess() {
		lastGuess = super.getGuess();
		return lastGuess;
	}

	@Override
	public synchronized void placeShips() {
		super.placeShips();
		placedShips = true;
	}

	@Override
	public void notifyOpponentExit() throws RemoteException {
		opponentLeft = true;
	}

	public boolean lost() throws RemoteException{
		return opponentLeft || getPlayerBoard().isGameOver();
	}

	/**
	 * @return the placedShips
	 */
	public boolean isPlacedShips() throws RemoteException{
		return placedShips;
	}

	/**
	 * @return the goingFirst
	 */
	public boolean isGoingFirst() {
		while(!goingFirstSet){
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				
			}
		}
		return goingFirst;
	}

	@Override
	public void setGoingFirst(boolean goingFirst) throws RemoteException {
		this.goingFirst = goingFirst;
		goingFirstSet = true;
	}

}

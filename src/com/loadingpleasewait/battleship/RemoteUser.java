/*
 * Copyright (C) 2014 Michael Murphey
 * 
 * This file is part of Battleship LPW.
 * 
 * Battleship LPW is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Battleship LPW is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Battleship LPW. If not, see <http://www.gnu.org/licenses/>.
 */

package com.loadingpleasewait.battleship;

import java.rmi.RemoteException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class RemoteUser extends User implements RemotePlayer {

	public static final int MAX_GUESS = 1000;

	private static final long serialVersionUID = -4008309853242923001L;

	private String lastGuess;
	private boolean placedShips;
	private boolean rematch;
	private boolean rematchRequested;
	private int guessedNumber = -1;
	private boolean goingFirst;
	private boolean goingFirstSet;
	private Thread dialogThread;
	private String input = "";

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
	 * ask user to guess a number and set the instance variable
	 * 
	 * @throws NullPointerException
	 *             user exited option pane
	 * @throws InterruptedException
	 *             opponent exited
	 */
	public void guessNumber() throws NullPointerException, InterruptedException {
		//thread input dialog so it can be stopped
		dialogThread = new Thread(() -> {
			while (input != null && guessedNumber == -1) {
				input = JOptionPane.showInputDialog(getTextField().getTopLevelAncestor(),
						"Enter number between 1 and 1000. Whoever's number is closer to the one I'm thinking of picks who goes first. If neither is closer we will guess again.");
				try {
					guessedNumber = Integer.parseInt(input);
				} catch (NumberFormatException ex) {

				}
				if (input != null && input.equals("forfeit")){
					//don't place ships for debugging
					placedShips = true;
					guessedNumber = 3;
				}else if (input != null && (guessedNumber == -1 || guessedNumber < 1 || guessedNumber > MAX_GUESS)) {
					JOptionPane.showMessageDialog(getTextField().getTopLevelAncestor(), "That was not a valid number");
					guessedNumber = -1;
				}
			}
		});
		dialogThread.start();
		
		//wait for input
		while(input != null && guessedNumber == -1){
			try{
				Thread.sleep(50);
			}catch (InterruptedException ex){
				
			}
		}
		if(input == null)
			throw new NullPointerException("User exited");
	}

	@Override
	public void showNumber(int correctNumber, int opponentNumber) throws RemoteException {
		String message = "Correct number was " + correctNumber + "\n";
		message += "Opponent guessed " + opponentNumber + "\n";
		// tell user who is going first
		message += goingFirst ? "You " : "Opponent ";// add name of player that
														// is going first
		message += "will go first.";
		JOptionPane.showMessageDialog(getTextField().getTopLevelAncestor(), message);
		goingFirstSet = true;
	}

	@Override
	public boolean wantsToGoFirst() throws RemoteException {
		int choice = JOptionPane.NO_OPTION;
		try {
			// ask user if they want a to go first or second with an option pane
			choice = JOptionPane.showConfirmDialog(getTextField().getTopLevelAncestor(), "Would you like to go first?",
					"Rematch", JOptionPane.YES_NO_OPTION);
		} catch (NullPointerException ex) {
			notifyOpponentExit();
			System.exit(0);
		}
		return choice == JOptionPane.YES_OPTION;
	}

	@Override
	public void requestGuessNumber() throws RemoteException, NullPointerException {
		// previous numbers were the same so guessing must be done again
		guessedNumber = -1;
		try {
			guessNumber();
		} catch (InterruptedException ex) {
			System.exit(0);//opponent exited
		} catch (NullPointerException ex) {
			//wait then exit while passing on exception
			new Thread(() -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex1) {
					
				}
				System.exit(0);
			}).start();
			throw ex;
		}
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
		rematchRequested = false;
		return rematch;
	}

	/**
	 * @param rematch
	 *            the rematch to set
	 */
	public void setRematch(boolean rematch) {
		this.rematch = rematch;
		rematchRequested = true;
	}

	/**
	 * reset state
	 */
	public void reset() {
		guessedNumber = -1;
		goingFirstSet = false;
		placedShips = false;
	}

	@Override
	public String getRemoteGuess() throws RemoteException {
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
		if(!placedShips)
			super.placeShips();
		placedShips = true;
	}

	@Override
	public void notifyOpponentExit() throws RemoteException {
		JOptionPane.showMessageDialog(getTextField().getTopLevelAncestor(), "Opponent has left");
		dialogThread.interrupt();
		
		if(!goingFirstSet)
			System.exit(0);
	}

	public boolean lost() throws RemoteException {
		return getPlayerBoard().isGameOver();
	}

	/**
	 * @return the placedShips
	 */
	public boolean isPlacedShips() throws RemoteException {
		return placedShips;
	}

	/**
	 * @return the goingFirst
	 */
	public boolean isGoingFirst() {
		while (!goingFirstSet) {
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
	}

}

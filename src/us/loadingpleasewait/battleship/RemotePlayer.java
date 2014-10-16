/*
 * Copyright (C) 2014  Michael Murphey
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

package us.loadingpleasewait.battleship;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotePlayer extends Remote {

	public int getNumberGuess() throws RemoteException;

	public void requestGuessNumber() throws RemoteException, NullPointerException;

	public boolean wantsToGoFirst() throws RemoteException;

	public void showNumber(int correctNumber, int opponentNumber) throws RemoteException;

	public boolean wantsRematch() throws RemoteException;

	public String getRemoteGuess() throws RemoteException;

	public void setRemoteGuessBoard(Board board) throws RemoteException;

	public Board getRemotePlayerBoard() throws RemoteException;

	public Board getRemoteGuessBoard() throws RemoteException;

	public void notifyOpponentExit() throws RemoteException;

	public void setGoingFirst(boolean goingFirst) throws RemoteException;

	public boolean isPlacedShips() throws RemoteException;

	public boolean lost() throws RemoteException;
}

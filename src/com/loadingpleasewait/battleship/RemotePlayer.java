package com.loadingpleasewait.battleship;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotePlayer extends Remote {

	public int getNumberGuess() throws RemoteException;
	
	public void requestGuessNumber() throws RemoteException;
	
	public void showNumber(int number) throws RemoteException;
	
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

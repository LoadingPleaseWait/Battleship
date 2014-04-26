package com.loadingpleasewait.battleship;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PlaceHolder extends Remote {

	/**
	 * Replace this object with the parameter in the Register
	 * 
	 * 
	 * @param obj
	 *            a reference to a remote object (usually a stub)
	 * @throws RemoteException
	 */
	public void proxyRebind(Remote obj) throws RemoteException;

}

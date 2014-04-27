package com.loadingpleasewait.battleship;

import java.io.IOException;
import java.rmi.MarshalledObject;
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
	 * @throws IOException 
	 * @throws ClassNotFoundException
	 */
	public void proxyRebind(MarshalledObject<? extends RemoteUser> obj) throws RemoteException, ClassNotFoundException, IOException;

}

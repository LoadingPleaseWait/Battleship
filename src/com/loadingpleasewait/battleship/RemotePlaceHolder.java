package com.loadingpleasewait.battleship;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RemotePlaceHolder extends UnicastRemoteObject implements PlaceHolder {

	private static final long serialVersionUID = -6508357103120199188L;

	public RemotePlaceHolder() throws RemoteException{
		super(0);
	}

	@Override
	public void proxyRebind(Remote obj) throws RemoteException {
		LocateRegistry.getRegistry().rebind(Battleship.JOINER, obj);
	}

}

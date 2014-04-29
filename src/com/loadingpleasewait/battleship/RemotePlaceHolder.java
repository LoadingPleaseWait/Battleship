/**
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

package com.loadingpleasewait.battleship;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RemotePlaceHolder extends UnicastRemoteObject implements PlaceHolder {

	private static final long serialVersionUID = -6508357103120199188L;

	public RemotePlaceHolder() throws RemoteException{
		super(0);
	}

	@Override
	public void proxyRebind(MarshalledObject<? extends RemoteUser> obj) throws ClassNotFoundException, IOException{
		LocateRegistry.getRegistry().rebind(Battleship.JOINER, obj.get());
	}

}

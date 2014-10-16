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

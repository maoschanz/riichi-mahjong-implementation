package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongLobbyInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;

public class MahjongLobby extends UnicastRemoteObject implements MahjongLobbyInterface {
	protected int nbPlayers = 0; // XXX private + faire les accesseurs
	private ArrayList ids = new ArrayList();
	private int uriId;

	protected MahjongLobby() throws RemoteException {
		super();
		this.uriId = 0;
	}

	public String registerPlayer(int playerId, String pseudo) throws RemoteException {
		System.out.println("Requête d'un nouveau joueur (" + (this.nbPlayers+1) + "/4): "
		                                                     + pseudo + " (id : " + playerId + ")");
		MahjongLobbyWaiter waiter = new MahjongLobbyWaiter(this);
		String returnedStr = "";
		try {
			waiter.start();
			synchronized(this){
				notifyAll();
			}
			waiter.join();
			if (this.nbPlayers > 5) { // atomicité ?
				this.uriId++;
				this.nbPlayers = 1; // TODO à tester mdr
			}
			returnedStr = Integer.toString(this.uriId);
		} catch (Exception e) {
			System.out.println("probably a timeout or something : " + e);
		}
		System.out.println("Return to client with : " + returnedStr);
		return returnedStr;
	}

	public synchronized boolean unregisterPlayer(int playerId) throws RemoteException {
		return true; // TODO
	}

	public synchronized boolean isRegistered(int playerId) throws RemoteException {
		return true; // TODO
	}

}


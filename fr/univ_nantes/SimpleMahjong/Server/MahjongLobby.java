package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongLobbyInterface;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongTuile;

// entièrement TODO

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MahjongLobby extends UnicastRemoteObject implements MahjongLobbyInterface {
	protected int nbPlayers = 0; // XXX private + faire les accesseurs

	protected MahjongLobby() throws RemoteException {
		super();
	}

	//----------------------------------------------------------------------------------------------

	public boolean registerPlayer(int playerId, String pseudo) throws RemoteException {
		System.out.println("Requête d'un nouveau joueur (" + (this.nbPlayers+1) + "/4): "
		                                                     + pseudo + " (id : " + playerId + ")");
		MahjongLobbyWaiter waiter = new MahjongLobbyWaiter(this);
		boolean accepted = false;
		try {
			waiter.start();
			waiter.join();
			accepted = this.nbPlayers < 5;
		} catch (Exception e) {}
		return accepted;
	}

	public synchronized boolean unregisterPlayer(int playerId) throws RemoteException {
		return true;
	}

	public synchronized boolean isRegistered(int playerId) throws RemoteException {
		return true;
	}

}


package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongLobbyInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MahjongLobby extends UnicastRemoteObject implements MahjongLobbyInterface {
	protected int nbPlayers = 0; // XXX private + faire les accesseurs

	protected MahjongLobby() throws RemoteException {
		super();
	}

	public boolean registerPlayer(int playerId, String pseudo) throws RemoteException {
		System.out.println("Requête d'un nouveau joueur (" + (this.nbPlayers+1) + "/4): "
		                                                     + pseudo + " (id : " + playerId + ")");
		MahjongLobbyWaiter waiter = new MahjongLobbyWaiter(this);
		boolean accepted = false;
		try {
			waiter.start();
			synchronized(this){
				notifyAll();
			}
			waiter.join();
			accepted = this.nbPlayers < 5;
		} catch (Exception e) {
			System.out.println("probably a timeout or something : " + e);
		}
		System.out.println("Return to client with : " + accepted);
		return accepted;
	}

	public synchronized boolean unregisterPlayer(int playerId) throws RemoteException {
		return true;
	}

	public synchronized boolean isRegistered(int playerId) throws RemoteException {
		return true;
	}

}


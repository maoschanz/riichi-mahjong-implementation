package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le client, et connue par le serveur et les autres clients.
 */
public interface MahjongPlayerInterface extends Remote {
	// boolean testContact(int test) throws RemoteException;
	void discoverOther(MahjongPlayerInterface other, String vent, int index) throws RemoteException;
	void initHand(String vent) throws RemoteException;
	void initTable(MahjongTableInterface table) throws RemoteException;
	void startGame(boolean me) throws RemoteException; // XXX nul nul nul
}


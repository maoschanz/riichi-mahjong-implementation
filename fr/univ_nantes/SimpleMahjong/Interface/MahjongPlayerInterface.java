package fr.univ_nantes.SimpleMahjong.Interface;
import fr.univ_nantes.SimpleMahjong.Tuile.AbstractTuile;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le client, et connue par le serveur et les autres clients.
 * XXX comment séparer en 2 interfaces distinctes ?
 */
public interface MahjongPlayerInterface extends Remote {
	// Pour les autres clients
	String getVentChar() throws RemoteException;
	String getPseudo() throws RemoteException;
	String getRiviere() throws RemoteException;
	String getCombis() throws RemoteException;
	boolean isJoueurCourant() throws RemoteException;
	void continueGame(boolean me) throws RemoteException; // un peu naze
	AbstractTuile getLastTuile() throws RemoteException;
	void removeLastTuile() throws RemoteException;

	// Pour le serveur
	void discoverOther(MahjongPlayerInterface other) throws RemoteException;
	void initHand(String vent) throws RemoteException;
	void initTable(MahjongTableInterface table) throws RemoteException;
	void startGame(boolean me) throws RemoteException; // un peu naze
}


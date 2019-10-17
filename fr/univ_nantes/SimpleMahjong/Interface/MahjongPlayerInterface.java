package fr.univ_nantes.SimpleMahjong.Interface;
import fr.univ_nantes.SimpleMahjong.Tuile.AbstractTuile;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le client, et connue par le serveur et les autres clients.
 */
public interface MahjongPlayerInterface extends Remote {
	String getVentChar() throws RemoteException;
	String getPseudo() throws RemoteException;
	String getRiviere() throws RemoteException;
	String getCombis() throws RemoteException;
	AbstractTuile volLastTuile() throws RemoteException;
	void discoverOther(MahjongPlayerInterface other) throws RemoteException;
	void initHand(String vent) throws RemoteException;
	void initTable(MahjongTableInterface table) throws RemoteException;
	void startGame(boolean me) throws RemoteException; // XXX nul nul nul
}


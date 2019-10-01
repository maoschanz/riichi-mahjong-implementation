package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le serveur, et connue par le client qui l'utilisera pour faire toutes
 * ses requêtes.
 */
public interface MahjongInterface extends Remote {
	boolean registerPlayer(int playerId, String pseudo) throws RemoteException;
	MahjongTuile annonceEtVol(String s) throws Exception; // XXX
	MahjongTuile pioche() throws RemoteException;
	void pose(MahjongTuile t) throws RemoteException; // XXX
}


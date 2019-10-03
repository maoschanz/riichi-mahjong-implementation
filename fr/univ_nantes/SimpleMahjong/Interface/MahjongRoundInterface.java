package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le serveur, et connue par le client qui l'utilisera pour faire toutes
 * ses requêtes.
 */
public interface MahjongRoundInterface extends Remote {
	// boolean registerPlayer(int playerId, String pseudo) throws Exception; // TODO
	AbstractMahjongTuile annonceEtVol(String s) throws Exception; // XXX ?
	AbstractMahjongTuile pioche() throws RemoteException;
	void pose(AbstractMahjongTuile t) throws RemoteException; // XXX ?
}


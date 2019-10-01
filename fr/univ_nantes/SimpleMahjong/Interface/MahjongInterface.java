package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MahjongInterface extends Remote {
	boolean registerPlayer(int playerId, String pseudo) throws RemoteException;
	MahjongTuile annonceEtVol(String s) throws Exception;
	MahjongTuile pioche() throws RemoteException;
	void pose(MahjongTuile t) throws RemoteException;
}


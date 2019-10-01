package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MahjongInterface extends Remote {
	boolean registerPlayer(int playerId, String pseudo) throws RemoteException;
	MahjongTuile annonceEtVol(String s) throws RemoteException;
	MahjongTuile pioche() throws RemoteException;
	int test_print(String s) throws RemoteException; // XXX à virer TODO
}


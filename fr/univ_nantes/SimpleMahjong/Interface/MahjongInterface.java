package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MahjongInterface extends Remote {
	public MahjongTuile annonceEtVol(String s) throws RemoteException;
	public MahjongTuile pioche() throws RemoteException;
	public int test_print(String s) throws RemoteException; // XXX à virer TODO
}


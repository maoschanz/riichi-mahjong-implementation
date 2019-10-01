package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MahjongInterface extends Remote {
	public int test_print(String s) throws RemoteException;
}


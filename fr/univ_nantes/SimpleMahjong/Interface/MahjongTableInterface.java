package fr.univ_nantes.SimpleMahjong.Interface;
import fr.univ_nantes.SimpleMahjong.Tuile.AbstractTuile;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le serveur, et connue par le client qui l'utilisera pour faire toutes
 * ses requêtes.
 */
public interface MahjongTableInterface extends Remote {
	AbstractTuile pioche() throws RemoteException;
	// void gagne(boolean nonNul) throws RemoteException; // TODO
}


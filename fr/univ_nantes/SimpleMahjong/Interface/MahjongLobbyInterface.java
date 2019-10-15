package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le serveur, et connue par le client qui l'utilisera pour faire ses
 * requêtes de connexion.
 */
public interface MahjongLobbyInterface extends Remote {
	void registerPlayer(MahjongPlayerInterface player, String pseudo) throws RemoteException;
}


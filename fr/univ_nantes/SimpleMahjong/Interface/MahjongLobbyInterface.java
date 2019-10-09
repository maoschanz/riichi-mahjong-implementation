package fr.univ_nantes.SimpleMahjong.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface implémentée par le serveur, et connue par le client qui l'utilisera pour faire toutes
 * ses requêtes.
 */
public interface MahjongLobbyInterface extends Remote {
	String registerPlayer(int playerId, String pseudo) throws RemoteException;
	boolean unregisterPlayer(int playerId) throws RemoteException;
	boolean isRegistered(int playerId) throws RemoteException;
}


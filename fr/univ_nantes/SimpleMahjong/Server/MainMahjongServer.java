package fr.univ_nantes.SimpleMahjong.Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainMahjongServer {
	private static final int portnum = 8090;
	private static final String uri = "rmi://localhost:" + portnum + "/mahjong";
	public static void main (String[] args) throws Exception {
		try {

			LocateRegistry.createRegistry(portnum);
			MahjongServer server = new MahjongServer();
			Naming.bind(uri, server);

		} catch(Exception e) {
			System.out.println("[erreur au démarrage du serveur] " + e);
		}
	}
}


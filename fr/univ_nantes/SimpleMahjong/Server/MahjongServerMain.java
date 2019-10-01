package fr.univ_nantes.SimpleMahjong.Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MahjongServerMain {
	private static final int portnum = 8090;
	public static void main (String[] args) throws Exception {
		try {

			LocateRegistry.createRegistry(portnum);
			MahjongServer eeeee = new MahjongServer();
			String uri = "rmi://localhost:" + portnum + "/mahjong";
			Naming.bind(uri, eeeee);
			System.out.println("le serveur a démarré");

		} catch(Exception e) {
			System.out.println("erreur serveur : " + e);
		}
	}
}


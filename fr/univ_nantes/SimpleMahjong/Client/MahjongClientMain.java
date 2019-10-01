package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongInterface;

import java.rmi.Naming;

public class MahjongClientMain {
	private static final int portnum = 8090;
	private static final String serverUri = "rmi://localhost:" + portnum + "/mahjong";
	public static void main (String[] args) {
		try {

			MahjongInterface server = (MahjongInterface)Naming.lookup(serverUri);
			MahjongClient client = new MahjongClient(server);

		} catch(Exception e) {
			System.out.println("erreur à la création du client : " + e);
		}
	}
}


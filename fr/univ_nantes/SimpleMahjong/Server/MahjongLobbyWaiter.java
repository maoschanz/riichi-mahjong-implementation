package fr.univ_nantes.SimpleMahjong.Server;

public class MahjongLobbyWaiter extends Thread {
	private MahjongLobby lobby;

	protected MahjongLobbyWaiter(MahjongLobby lobby) {
		super();
		this.lobby = lobby;
		this.lobby.nbPlayers++; // XXX atomicité ????????????????
	}

	public void run() {
		while(this.lobby.nbPlayers < 4){
			synchronized(this.lobby) {
				try {
					this.lobby.wait();
				} catch (Exception e) {
					System.out.println("Erreur durant l'attente des joueurs (" +
					                                           this.lobby.nbPlayers + "/4) : " + e);
				}
			}
		}
		System.out.println("fin du run du waiter : " + this.lobby.nbPlayers);
	}
}


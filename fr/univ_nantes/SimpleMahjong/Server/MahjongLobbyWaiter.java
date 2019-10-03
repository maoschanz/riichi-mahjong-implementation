package fr.univ_nantes.SimpleMahjong.Server;

public class MahjongLobbyWaiter extends Thread {
	private int nbPlayers = 0;
	private MahjongLobby lobby;

	protected MahjongLobbyWaiter(MahjongLobby lobby) {
		super();
		this.lobby = lobby;
		this.lobby.nbPlayers++; // XXX atomicité ????????????????
	}

	public void run() /*throws Exception*/ {
		while(this.lobby.nbPlayers < 4){}
	}
}


package fr.univ_nantes.SimpleMahjong.Interface;

public class MahjongTuileDragon extends AbstractMahjongTuileHonneur {
	public MahjongTuileDragon (int chiffre, int exemplaire) {
		super(exemplaire, chiffre);
		switch(this.chiffre) {
			case 0:
				this.label = "Dragon rouge";
				break;
			case 1:
				this.label = "Dragon vert";
				break;
			default: //case 2:
				this.label = "Dragon blanc";
				break;
		}
	}
}

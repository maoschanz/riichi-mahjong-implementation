package fr.univ_nantes.SimpleMahjong.Interface;

public class MahjongTuileVent extends AbstractMahjongTuileHonneur {
	public MahjongTuileVent (int chiffre, int exemplaire) {
		super(exemplaire, chiffre);
		switch(this.chiffre) {
			case 0:
				this.label = "Vent d'Est";
				break;
			case 1:
				this.label = "Vent de Sud";
				break;
			case 2:
				this.label = "Vent d'Ouest";
				break;
			default: //case 3:
				this.label = "Vent du Nord";
				break;
		}
	}
}

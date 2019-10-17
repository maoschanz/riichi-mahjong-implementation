package fr.univ_nantes.SimpleMahjong.Tuile;

public class TuileVent extends AbstractTuileHonneur {
	public TuileVent (int chiffre, int exemplaire) {
		super(exemplaire, chiffre);
		this.sortId = 40 + this.chiffre;
		switch (this.chiffre) {
			case 0:
				this.name = "Vent d'Est";
				this.label = "東";
				break;
			case 1:
				this.name = "Vent de Sud";
				this.label = "南";
				break;
			case 2:
				this.name = "Vent d'Ouest";
				this.label = "西";
				break;
			default: //case 3:
				this.name = "Vent du Nord";
				this.label = "北";
				break;
		}
	}
}

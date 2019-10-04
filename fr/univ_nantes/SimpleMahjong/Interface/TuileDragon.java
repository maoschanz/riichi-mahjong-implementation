package fr.univ_nantes.SimpleMahjong.Interface;

public class TuileDragon extends AbstractTuileHonneur {
	public TuileDragon (int chiffre, int exemplaire) {
		super(exemplaire, chiffre);
		this.sortId = 30 + this.chiffre;
		switch(this.chiffre) {
			case 0:
				this.name = "Dragon rouge";
				this.label = "🀄️";
				break;
			case 1:
				this.name = "Dragon vert";
				this.label = "🐉";
				break;
			default: //case 2:
				this.name = "Dragon blanc";
				this.label = "⬜️";
				break;
		}
	}
}

package fr.univ_nantes.SimpleMahjong.Tuile;

enum TypeTuile { KANJI, BAMBOU, ROND };

public class TuileNombre extends AbstractTuile {
	private TypeTuile type;

	public TuileNombre (char type, int exemplaire, int chiffre) {
		super(exemplaire, chiffre);
		switch(type) {
			case 'k':
				this.sortId = 0 + this.chiffre;
				this.type = TypeTuile.KANJI;
				this.name = this.chiffre + " de caractère";
				this.label = "万" + this.chiffre;
				break;
			case 'b':
				this.sortId = 10 + this.chiffre;
				this.type = TypeTuile.BAMBOU;
				this.name = this.chiffre + " de bambou";
				this.label = "🎍" + this.chiffre;
				break;
			default: // case 'r'
				this.sortId = 20 + this.chiffre;
				this.type = TypeTuile.ROND;
				this.name = this.chiffre + " de rond";
				this.label = "🔵" + this.chiffre;
				break;
		}
	}

}

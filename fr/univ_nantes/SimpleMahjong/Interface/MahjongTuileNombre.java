package fr.univ_nantes.SimpleMahjong.Interface;

enum TypeTuile { KANJI, BAMBOU, ROND };

public class MahjongTuileNombre extends AbstractMahjongTuile {
	private TypeTuile type;

	public MahjongTuileNombre (char type, int exemplaire, int chiffre) {
		super(exemplaire, chiffre);
		switch(type) {
			case 'k':
				this.type = TypeTuile.KANJI;
				this.label = this.chiffre + " de caractère";
				break;
			case 'b':
				this.type = TypeTuile.BAMBOU;
				this.label = this.chiffre + " de bambou";
				break;
			default: // case 'r'
				this.type = TypeTuile.ROND;
				this.label = this.chiffre + " de rond";
				break;
		}
	}

}

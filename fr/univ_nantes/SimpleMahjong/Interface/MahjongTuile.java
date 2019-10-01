package fr.univ_nantes.SimpleMahjong.Interface;
import java.io.Serializable;

enum TypeTuile { DRAGON, VENT, KANJI, BAMBOU, ROND };

public class MahjongTuile implements Serializable {
	private int chiffre;
	private TypeTuile type;

	public MahjongTuile (int chiffre, char type) {
		this.chiffre = chiffre;
		switch(type) {
			case 'd': this.type = TypeTuile.DRAGON; break;
			case 'v': this.type = TypeTuile.VENT; break;
			case 'k': this.type = TypeTuile.KANJI; break;
			case 'b': this.type = TypeTuile.BAMBOU; break;
			default: this.type = TypeTuile.ROND; break; // case 'r'
		}
	}

	public String toString() {
		String msg = "";
		switch(this.type) {
			case DRAGON:
				if (this.chiffre == 0) {
					msg = "Dragon rouge";
				} else if (this.chiffre == 1) {
					msg = "Dragon vert";
				} else {
					msg = "Dragon blanc";
				}
				break;
			case VENT:
				if (this.chiffre == 0) {
					msg = "Vent d'Est";
				} else if (this.chiffre == 1) {
					msg = "Vent de Sud";
				} else if (this.chiffre == 2) {
					msg = "Vent d'Ouest";
				} else {
					msg = "Vent du Nord";
				}
				break;
			case KANJI: msg = this.chiffre + " de caractère"; break;
			case BAMBOU: msg = this.chiffre + " de bambou"; break;
			default: msg = this.chiffre + " de rond"; break; // case ROND
		}
		return msg;
	}

}



package fr.univ_nantes.SimpleMahjong.Interface;
import java.io.Serializable;

public abstract class AbstractMahjongTuile implements Serializable {
	protected int chiffre;
	protected int exemplaire;
	protected String label;

	public AbstractMahjongTuile (int exemplaire, int chiffre) {
		this.chiffre = chiffre;
		this.exemplaire = exemplaire;
	}

	public String toString() {
		return this.label;
	}
}



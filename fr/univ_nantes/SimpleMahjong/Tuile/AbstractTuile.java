package fr.univ_nantes.SimpleMahjong.Tuile;
import java.io.Serializable;

public abstract class AbstractTuile implements Comparable, Serializable {
	protected int chiffre;
	protected int exemplaire;
	protected String name;
	protected String label;
	public int sortId; // XXX

	public AbstractTuile (int exemplaire, int chiffre) {
		this.chiffre = chiffre;
		this.exemplaire = exemplaire;
	}

	public String toString() {
		return this.name;
	}

	public String getEmoji() {
		return this.label;
	}

	public int compareTo(Object autre) {
		return this.sortId - ((AbstractTuile)autre).sortId;
	}

}



package fr.isika.cda.Annuaire.models;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class Noeud {
	
	// Les attributs
	private int parent;
	private int filsDroit;
	private int filsGauche;
	private int hauteur;
	private int listeChainee;
	private Stagiaire stagiaire;
	private static final int LISTE_VIDE = -1;
	private static final int FILS_NUL = -1;
	public static final int TAILLE_INT_OCTETS = 4;
	
	public static final int TAILLE_NOEUD_OCTETS = Stagiaire.TAILLE_OBJET_OCTET + TAILLE_INT_OCTETS*5;
	
	//Constructeur
	public Noeud(Stagiaire stagiaire, int parent, int filsDroit, int filsGauche, int listeChainee, int hauteur) {
		super();
		this.stagiaire = stagiaire;
		this.listeChainee = listeChainee;
		this.parent = parent;
		this.filsGauche = filsGauche;
		this.filsDroit = filsDroit;
		this.hauteur = hauteur;
	}
	
	public Noeud(Stagiaire stagiaire, int parent, int hauteur) {
		super();
		this.stagiaire = stagiaire;
		this.listeChainee = LISTE_VIDE;
		this.parent = parent;
		this.filsGauche = FILS_NUL;
		this.filsDroit = FILS_NUL;
		this.hauteur = hauteur;
	}
	
	// Methodes
	@Override
	public String toString() {
		return "Noeud{" + "stagiaire=" + stagiaire + ", listeChainee=" + listeChainee + ", parent=" + parent + ", filsGauche=" + filsGauche + ", filsDroit=" + filsDroit + ", hauteur=" + hauteur + "}";
	}
	
	public void writeNoeudBinaire(RandomAccessFile raf) throws IOException {
		this.stagiaire.writeStagiaireBinaire(raf);
		raf.writeInt(listeChainee);
		raf.write(parent);
		raf.writeInt(filsGauche);
		raf.writeInt(filsDroit);
		raf.writeInt(hauteur);
	}
	
	public void addNoeud(Stagiaire stagiaireToAdd, GestionFichiers rafFichierDom) throws IOException {
		
		RandomAccessFile raf = rafFichierDom.getRaf();
		
		if (this.stagiaire.getNom().compareToIgnoreCase(stagiaireToAdd.getNom()) == 0) {
			//Stagiaire -> arbre binaire
			if (this.stagiaire.compareTo(stagiaireToAdd) == 0) {
				System.out.println("Il existe deja dans la liste");
			} else {
				//Stagiaire -> liste chainee
				Noeud noeudDeLaListeChainee = this;
				// raf dans la liste chainee pour avoir une fin
				while (noeudDeLaListeChainee.listeChainee != LISTE_VIDE) {
					// se mettre sur le stagiaire suivant de la liste
					raf.seek((long) noeudDeLaListeChainee.listeChainee * TAILLE_NOEUD_OCTETS);
					noeudDeLaListeChainee = rafFichierDom.lectureNoeud();
				}
				
				// Add stagiaire
				ajouterDansFichierBinaire(raf, 5, stagiaireToAdd);
			}
			//Recherche de l'emplacement pour ajouter
		} else if (this.stagiaire.getNom().compareToIgnoreCase(stagiaireToAdd.getNom()) > 0) {
			if (this.filsGauche == FILS_NUL) {
				ajouterDansFichierBinaire(raf, 3, stagiaireToAdd);
			} else {
				raf.seek((long) this.filsGauche * TAILLE_NOEUD_OCTETS);
				Noeud noeudFilsGauche = rafFichierDom.lectureNoeud();
				noeudFilsGauche.addNoeud(stagiaireToAdd, rafFichierDom);
			}
		}
	}
	
	private void ajouterDansFichierBinaire(RandomAccessFile raf, int nombreDeParametresAReculer, Stagiaire stagiaireToAdd) throws IOException {
		int indexNoeudParent = (int) (raf.getFilePointer() / TAILLE_NOEUD_OCTETS) -1;
		int indexStagiaireToAdd = (int) (raf.length() / TAILLE_NOEUD_OCTETS);
		raf.seek(raf.getFilePointer() - ((long) TAILLE_INT_OCTETS * nombreDeParametresAReculer));
		raf.writeInt(indexStagiaireToAdd);
		raf.seek(raf.length());
		new Noeud(stagiaireToAdd, indexNoeudParent, hauteur + 1).writeNoeudBinaire(raf);
	}
	
	public void ordreAlphabetique(List<Stagiaire> listStagiaire, GestionFichiers rafFichierDom) throws IOException {
		
		RandomAccessFile raf = rafFichierDom.getRaf();
		
		Noeud noeudDeLaListeChainee = this;
		
		if (this.filsGauche != -1) {
			raf.seek((long) this.filsGauche * TAILLE_NOEUD_OCTETS);
			Noeud noeudFilsGauche = rafFichierDom.lectureNoeud();
			noeudFilsGauche.ordreAlphabetique(listStagiaire, rafFichierDom);
		}
		
		listStagiaire.add(stagiaire);
		
		while (noeudDeLaListeChainee.listeChainee != LISTE_VIDE) {
			raf.seek((long) noeudDeLaListeChainee.listeChainee * TAILLE_NOEUD_OCTETS);
			noeudDeLaListeChainee = rafFichierDom.lectureNoeud();
			listStagiaire.add(noeudDeLaListeChainee.stagiaire);
		}
		
		if (this.filsDroit != -1) {
			raf.seek((long) this.filsDroit * TAILLE_NOEUD_OCTETS);
			Noeud noeudFilsDroit = rafFichierDom.lectureNoeud();
			noeudFilsDroit.ordreAlphabetique(listStagiaire, rafFichierDom);
		}
	}

	
	//Methode de recherche
	public void searchStagiere(List<Stagiaire> listResultats, Stagiaire stagiaireSearch, GestionFichiers rafFichierDom) throws IOException {
		
		RandomAccessFile raf = rafFichierDom.getRaf();
		
		// comparer entre stagiaire courant et stagiaire recherche
		if (stagiaireSearch.getNom().compareToIgnoreCase(this.stagiaire.getNom()) == 0) {
			listResultats.add(this.stagiaire);
			
			if (this.listeChainee != LISTE_VIDE) {
				raf.seek((long) this.listeChainee * TAILLE_NOEUD_OCTETS);
				Noeud noeudSuivant = rafFichierDom.lectureNoeud();
				noeudSuivant.searchStagiere(listResultats, stagiaireSearch, rafFichierDom);
			}
		} else if (stagiaireSearch.getNom().compareToIgnoreCase(this.stagiaire.getNom()) < 0) {
			if (this.filsGauche == FILS_NUL) {
				System.out.println("Aucun stagiaire correspondant.");
			} else {
				raf.seek((long) this.filsGauche * TAILLE_NOEUD_OCTETS);
				Noeud noeudFilsGauche = rafFichierDom.lectureNoeud();
				noeudFilsGauche.searchStagiere(listResultats, stagiaireSearch, rafFichierDom);
			}
			//recursivite Droit
		} else {
			if (this.filsDroit == FILS_NUL) {
				System.out.println("Aucun stagiaire correspondant");
			} else {
				raf.seek((long) this.filsDroit * TAILLE_NOEUD_OCTETS);
				Noeud noeudFilsDroit = rafFichierDom.lectureNoeud();
				noeudFilsDroit.searchStagiere(listResultats, stagiaireSearch, rafFichierDom);
			}
		}
	}
	
	public int deleteNoeud(Stagiaire stagiaireASupprimer, GestionFichiers rafFichierDom) throws IOException {

        RandomAccessFile raf = rafFichierDom.getRaf();

        int indexDuStagiaire = (int) ((raf.getFilePointer() - TAILLE_NOEUD_OCTETS) / TAILLE_NOEUD_OCTETS);

        //Recherche du stagiaire
        if (this.stagiaire.getNom().compareToIgnoreCase(stagiaireASupprimer.getNom()) > 0) {

            raf.seek((long) this.filsGauche * TAILLE_NOEUD_OCTETS);// pour positionner le curseur au début du fils gauche on
            // multiplie l'index du fils gauche par la taille d'un noeud
            Noeud noeudFilsGauche = rafFichierDom.lectureNoeud(); // on lit les valeurs du fils gauche où le curseur
            // s'est arrêté pour stocker les informations

            this.filsGauche = noeudFilsGauche.deleteNoeud(stagiaireASupprimer, rafFichierDom);
            raf.seek((long) indexDuStagiaire * TAILLE_NOEUD_OCTETS + Stagiaire.TAILLE_OBJET_OCTET + TAILLE_INT_OCTETS * 2); //car on est au début de notre Noeud. 2 pour arriver au début du fils gauche
            raf.writeInt(this.filsGauche);

        } else if (this.stagiaire.getNom().compareToIgnoreCase(stagiaireASupprimer.getNom()) < 0) { // on ne veut pas le contraire de
            // strictement plus petit (qui est strictement plus grand ou égal)
            // mais on veut juste le strictement plus grand.
            raf.seek((long) this.filsDroit * TAILLE_NOEUD_OCTETS);
            Noeud noeudFilsDroit = rafFichierDom.lectureNoeud();

            this.filsDroit = noeudFilsDroit.deleteNoeud(stagiaireASupprimer, rafFichierDom);
            raf.seek((long) indexDuStagiaire * TAILLE_NOEUD_OCTETS + Stagiaire.TAILLE_OBJET_OCTET + TAILLE_INT_OCTETS * 3); //car on est au début de notre Noeud. 3 pour arriver au début du fils droit
            raf.writeInt(this.filsDroit);

        } else { // cas où on a trouvé le nom du stagiaire à supprimer
            // Et il correspond au noeud de l'arbrebinaire et il n'y  pas de liste chainée.
            if (this.stagiaire.compareTo(stagiaireASupprimer) == 0 && this.listeChainee == LISTE_VIDE) { // cas où le stagiaire à supprimé est dans l'arbre binaire et n'a pas de liste chainée
                //Cas de la feuille ou cas avec un seul fils.
                if (this.filsGauche == FILS_NUL || this.filsDroit == FILS_NUL) {
                    if (this.filsGauche != FILS_NUL) {
                        return filsGauche;
                    } else {
                        return filsDroit;
                    }
                    //cas avec 2 fils.
                } else {
                    // il faut récupérer l'indice du stagiaire que l'on va supprimer
                    // (this.stagiaire) pour pouvoir l'utiliser après getSuccesseur qui a déplacé le
                    // curseur.

                    Noeud noeudDeRemplacement = this.getSuccesseur(rafFichierDom);
                    raf.seek((long) indexDuStagiaire * TAILLE_NOEUD_OCTETS); // on a positionné notre curseur au bon
                    // endroit: le début du stagiaire à supprimer.
                    noeudDeRemplacement.stagiaire.writeStagiaireBinaire(raf);// on a écrit les informations de notre
                    // stagiaire successeur et de sa liste chaînée dans notre noeud
                    raf.writeInt(noeudDeRemplacement.listeChainee);

                    raf.seek((long) this.filsDroit * TAILLE_NOEUD_OCTETS);
                    Noeud noeudFilsDroit = rafFichierDom.lectureNoeud();
                    this.filsDroit = noeudFilsDroit.deleteNoeud(noeudDeRemplacement.stagiaire, rafFichierDom);
                    raf.seek((long) indexDuStagiaire * TAILLE_NOEUD_OCTETS + Stagiaire.TAILLE_OBJET_OCTET + TAILLE_INT_OCTETS * 3); //car on est au début de notre Noeud. 3 pour arriver au début du fils droit
                    raf.writeInt(this.filsDroit);

                    return indexDuStagiaire;
                }

            } else if (this.stagiaire.compareTo(stagiaireASupprimer) == 0) { // Le stagiaire à supprimer est dans l'arbre et il a une liste chainee. Il faut que l'arbre pointe vers le deuxième maillon de la chaine.
                // on écrit le stagiaire suivant et son index de liste sur le stagiaire à supprimer
                Noeud noeudActuel = this;
                int indexNoeudActuel = indexDuStagiaire;

                raf.seek((long) noeudActuel.listeChainee * TAILLE_NOEUD_OCTETS);
                Noeud noeudChaineSuivant = rafFichierDom.lectureNoeud();

                raf.seek((long) indexNoeudActuel * TAILLE_NOEUD_OCTETS);
                noeudChaineSuivant.stagiaire.writeStagiaireBinaire(raf);

                raf.seek((long) indexDuStagiaire * TAILLE_NOEUD_OCTETS + Stagiaire.TAILLE_OBJET_OCTET);
                raf.writeInt(noeudChaineSuivant.listeChainee);

            } else  { //cas où le stagiaire à supprimer est dans la liste chainée mais pas au début.
                Noeud noeudCourant = this;
                int indexNoeudCourant = indexDuStagiaire;
                int indexPrecedent = LISTE_VIDE;
                while (noeudCourant.stagiaire.compareTo(stagiaireASupprimer) != 0) {
                    raf.seek((long) noeudCourant.listeChainee * TAILLE_NOEUD_OCTETS);
                    indexPrecedent = indexNoeudCourant;
                    indexNoeudCourant = noeudCourant.listeChainee;
                    noeudCourant = rafFichierDom.lectureNoeud();
                }

                raf.seek((long) indexPrecedent * TAILLE_NOEUD_OCTETS + Stagiaire.TAILLE_OBJET_OCTET);
                raf.writeInt(noeudCourant.listeChainee);
            }
        }
        return indexDuStagiaire;
    }


    //pour récupérer les informations du noeud qui succecede celui à supprimer (ici le plus petit des plus grands)
    private Noeud getSuccesseur(GestionFichiers rafFichierDom) throws IOException {

        RandomAccessFile raf = rafFichierDom.getRaf();

        raf.seek((long) this.filsDroit * TAILLE_NOEUD_OCTETS);
        Noeud noeudTemporaire = rafFichierDom.lectureNoeud();
        while (noeudTemporaire.filsGauche != FILS_NUL) {
            raf.seek((long) noeudTemporaire.filsGauche * TAILLE_NOEUD_OCTETS);
            noeudTemporaire = rafFichierDom.lectureNoeud();
        }
        return noeudTemporaire;
    }


    //getters & setters
    public Stagiaire getStagiaire() {
        return stagiaire;
    }

    public void setStagiaire(Stagiaire stagiaire) {
        this.stagiaire = stagiaire;
    }

    public int getFilsGauche() {
        return filsGauche;
    }

    public void setFilsGauche(int filsGauche) {
        this.filsGauche = filsGauche;
    }

    public int getFilsDroit() {
        return filsDroit;
    }

    public void setFilsDroit(int filsDroit) {
        this.filsDroit = filsDroit;
    }

    public int getListeChainee() {
        return listeChainee;
    }

    public void setListeChainee(int listeChainee) {
        this.listeChainee = listeChainee;
    }
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

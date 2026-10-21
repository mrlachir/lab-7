package com.example.model;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.util.*;

@Entity @Table(name="auteurs") @Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(name="Auteur.livres",attributeNodes=@NamedAttributeNode("livres"))
public class Auteur{
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String nom;
    @OneToMany(mappedBy="auteur",cascade=CascadeType.ALL,orphanRemoval=true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<Livre> livres=new ArrayList<>();
    public Auteur(){}
    public Auteur(String nom){this.nom=nom;}
    public Long getId(){return id;}
    public String getNom(){return nom;}
    public void setNom(String value){nom=value;}
    public List<Livre> getLivres(){return Collections.unmodifiableList(livres);}
    public void ajouterLivre(Livre livre){Objects.requireNonNull(livre).setAuteur(this);}
    public void retirerLivre(Livre livre){if(livre.getAuteur()==this)livre.setAuteur(null);}
    void attach(Livre livre){if(!livres.contains(livre))livres.add(livre);}
    void detach(Livre livre){livres.remove(livre);}
}

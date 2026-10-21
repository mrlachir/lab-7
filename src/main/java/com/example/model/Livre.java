package com.example.model;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity @Table(name="livres") @Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Livre{
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String titre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="auteur_id") private Auteur auteur;
    public Livre(){}
    public Livre(String titre){this.titre=titre;}
    public Long getId(){return id;}
    public String getTitre(){return titre;}
    public void setTitre(String value){titre=value;}
    public Auteur getAuteur(){return auteur;}
    public void setAuteur(Auteur value){
        if(auteur==value)return;
        Auteur old=auteur;auteur=value;
        if(old!=null)old.detach(this);if(value!=null)value.attach(this);
    }
}

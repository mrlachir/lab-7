package com.example.service;
import com.example.model.Auteur;
import javax.persistence.*;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import java.util.*;

public class PerformanceTestService{
    private final EntityManagerFactory emf;
    private final Statistics stats;
    public PerformanceTestService(EntityManagerFactory emf){
        this.emf=emf;stats=emf.unwrap(SessionFactory.class).getStatistics();
    }
    public void evictAll(){emf.unwrap(SessionFactory.class).getCache().evictAllRegions();}
    // A fresh EntityManager and transaction for every measurement avoids first-level reuse.
    private Measurement measure(String mode,Long id){
        stats.clear();long start=System.nanoTime();
        EntityManager em=emf.createEntityManager();EntityTransaction tx=em.getTransaction();
        try{
            tx.begin();List<Auteur> auteurs;
            if("find".equals(mode))auteurs=Collections.singletonList(em.find(Auteur.class,id));
            else{
                TypedQuery<Auteur> q=em.createQuery("join".equals(mode)
                    ?"SELECT DISTINCT a FROM Auteur a LEFT JOIN FETCH a.livres"
                    :"SELECT a FROM Auteur a",Auteur.class);
                if("graph".equals(mode))q.setHint("javax.persistence.fetchgraph",em.getEntityGraph("Auteur.livres"));
                if("query".equals(mode))q.setHint("org.hibernate.cacheable",true);
                auteurs=q.getResultList();
            }
            Set<Auteur> roots=new HashSet<>(auteurs);
            int books=0;for(Auteur a:roots)books+=a.getLivres().size();
            tx.commit();
            // Entity graphs can produce repeated root rows with some providers.
            int unique=roots.size();
            return new Measurement(stats.getPrepareStatementCount(),stats.getSecondLevelCacheHitCount(),
                stats.getQueryCacheHitCount(),System.nanoTime()-start,unique,books);
        }catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}
        finally{em.close();}
    }
    public Measurement testPerformanceSansCache(){return measure("lazy",null);}
    public Measurement testPerformanceAvecJoinFetch(){return measure("join",null);}
    public Measurement testPerformanceAvecEntityGraph(){return measure("graph",null);}
    public Measurement lectureParId(Long id){return measure("find",id);}
    public Measurement lectureQueryCache(){return measure("query",null);}
}

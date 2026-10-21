package com.example;
import com.example.model.*;
import com.example.service.*;
import com.example.util.Factories;
import javax.persistence.*;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.*;

public class CacheLabTest{
    private EntityManagerFactory emf;
    @After public void close(){if(emf!=null)emf.close();}
    private PerformanceTestService setup(boolean cached){
        emf=Factories.create(cached,"test_"+UUID.randomUUID());
        new DataInitService(emf).initData();
        return new PerformanceTestService(emf);
    }
    private Long first(){
        EntityManager em=emf.createEntityManager();
        try{return em.createQuery("SELECT MIN(a.id) FROM Auteur a",Long.class).getSingleResult();}
        finally{em.close();}
    }
    @Test public void nPlusOneUsesElevenStatements(){
        Measurement m=setup(false).testPerformanceSansCache();
        assertEquals(11,m.getSql());assertEquals(10,m.getAuteurs());assertEquals(50,m.getLivres());
    }
    @Test public void joinFetchUsesOneStatement(){
        Measurement m=setup(false).testPerformanceAvecJoinFetch();
        assertEquals(1,m.getSql());assertEquals(10,m.getAuteurs());assertEquals(50,m.getLivres());
    }
    @Test public void entityGraphUsesOneStatement(){
        Measurement m=setup(false).testPerformanceAvecEntityGraph();
        assertEquals(1,m.getSql());assertEquals(10,m.getAuteurs());assertEquals(50,m.getLivres());
    }
    @Test public void l2WorksAcrossDifferentEntityManagersIncludingCollection(){
        PerformanceTestService p=setup(true);p.evictAll();Long id=first();
        Measurement cold=p.lectureParId(id),warm=p.lectureParId(id);
        assertEquals(2,cold.getSql());assertEquals(0,warm.getSql());
        assertTrue(warm.getL2Hits()>=7);assertEquals(5,warm.getLivres());
    }
    @Test public void queryCacheRequiresExplicitCacheableHint(){
        PerformanceTestService p=setup(true);p.evictAll();
        Measurement cold=p.lectureQueryCache(),warm=p.lectureQueryCache();
        assertEquals(11,cold.getSql());assertEquals(0,warm.getSql());
        assertEquals(1,warm.getQueryHits());assertEquals(50,warm.getLivres());
    }
    @Test public void cacheEvictionForcesDatabaseReadAgain(){
        PerformanceTestService p=setup(true);Long id=first();p.evictAll();
        p.lectureParId(id);assertEquals(0,p.lectureParId(id).getSql());
        p.evictAll();assertEquals(2,p.lectureParId(id).getSql());
    }
    @Test public void updateInvalidatesCachedStateAndOrphanIsRemoved(){
        PerformanceTestService p=setup(true);Long id=first();p.evictAll();p.lectureParId(id);
        EntityManager em=emf.createEntityManager();Long removed;
        try{
            em.getTransaction().begin();Auteur auteur=em.find(Auteur.class,id);
            auteur.setNom("Auteur modifie");Livre livre=auteur.getLivres().get(0);removed=livre.getId();
            auteur.retirerLivre(livre);em.getTransaction().commit();
        }finally{em.close();}
        em=emf.createEntityManager();
        try{
            Auteur auteur=em.find(Auteur.class,id);
            assertEquals("Auteur modifie",auteur.getNom());assertEquals(4,auteur.getLivres().size());
            assertNull(em.find(Livre.class,removed));
        }finally{em.close();}
    }
}

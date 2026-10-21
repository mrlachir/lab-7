package com.example;
import com.example.service.*;
import com.example.util.Factories;
import javax.persistence.*;

public class App{
    public static void main(String[] args){
        tryFactoryWithoutCache();
        EntityManagerFactory emf=Factories.create(true,"cache_on");
        try{
            Long id=seedIfEmpty(emf);
            PerformanceTestService perf=new PerformanceTestService(emf);
            System.out.println("=== CACHE L2 : NOUVEAU CONTEXTE A CHAQUE LECTURE ===");
            perf.evictAll();
            System.out.println("L2 FROID : "+perf.lectureParId(id));
            System.out.println("L2 CHAUD : "+perf.lectureParId(id));
            perf.evictAll();
            System.out.println("QUERY CACHE FROID : "+perf.lectureQueryCache());
            System.out.println("QUERY CACHE CHAUD : "+perf.lectureQueryCache());
            System.out.println("DEMO TERMINEE");
        }finally{emf.close();}
    }
    private static void tryFactoryWithoutCache(){
        EntityManagerFactory emf=Factories.create(false,"cache_off");
        try{
            seedIfEmpty(emf);
            PerformanceTestService perf=new PerformanceTestService(emf);
            System.out.println("=== SANS CACHE : 10 AUTEURS / 50 LIVRES ===");
            System.out.println("N+1 : "+perf.testPerformanceSansCache());
            System.out.println("JOIN FETCH : "+perf.testPerformanceAvecJoinFetch());
            System.out.println("ENTITY GRAPH : "+perf.testPerformanceAvecEntityGraph());
        }finally{emf.close();}
    }
    private static Long seedIfEmpty(EntityManagerFactory emf){
        EntityManager em=emf.createEntityManager();
        try{
            java.util.List<Long> ids=em.createQuery("SELECT a.id FROM Auteur a ORDER BY a.id",Long.class).setMaxResults(1).getResultList();
            if(!ids.isEmpty())return ids.get(0);
        }finally{em.close();}
        return new DataInitService(emf).initData();
    }
}

package com.example.service;
public class Measurement{
    private final long sql,l2Hits,queryHits,nanos;
    private final int auteurs,livres;
    public Measurement(long sql,long l2Hits,long queryHits,long nanos,int auteurs,int livres){
        this.sql=sql;this.l2Hits=l2Hits;this.queryHits=queryHits;this.nanos=nanos;this.auteurs=auteurs;this.livres=livres;
    }
    public long getSql(){return sql;}
    public long getL2Hits(){return l2Hits;}
    public long getQueryHits(){return queryHits;}
    public int getAuteurs(){return auteurs;}
    public int getLivres(){return livres;}
    @Override public String toString(){return "SQL="+sql+", L2 hits="+l2Hits+", query hits="+queryHits
        +", auteurs="+auteurs+", livres="+livres+", duree="+String.format(java.util.Locale.ROOT,"%.3f",nanos/1000000.0)+" ms";}
}

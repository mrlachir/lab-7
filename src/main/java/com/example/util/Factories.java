package com.example.util;
import javax.persistence.*;
import java.util.*;
import java.nio.file.Paths;
public final class Factories{
    private Factories(){}
    public static EntityManagerFactory create(boolean cache,String database){
        Map<String,Object> p=new HashMap<>();
        p.put("javax.persistence.jdbc.url","jdbc:h2:mem:"+database+";DB_CLOSE_DELAY=-1");
        p.put("hibernate.cache.use_second_level_cache",String.valueOf(cache));
        p.put("hibernate.cache.use_query_cache",String.valueOf(cache));
        if("file".equals(System.getProperty("cachelab.db"))){
            String url="jdbc:h2:file:"+Paths.get("data",database).toAbsolutePath().normalize().toString().replace('\\','/');
            p.put("javax.persistence.jdbc.url",url);p.put("hibernate.hbm2ddl.auto","update");
            System.out.println("DATABASE URL: "+url);
        }
        return Persistence.createEntityManagerFactory("hibernate-cache-pu",p);
    }
}

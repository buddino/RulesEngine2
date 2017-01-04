package it.cnit.gaia.rulesengine.configuration;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedDBConfiguration {

    @Bean
    public DB embeddedDB() {
        DB db = DBMaker.fileDB("./db.data")
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
        return db;
    }
}

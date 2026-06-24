package com.example.demo;

import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

  private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

  @Bean
  Flyway flyway(
      DataSource dataSource,
      @Value("${spring.flyway.locations:classpath:db/migration}") List<String> locations,
      @Value("${spring.flyway.default-schema:${app.db.schema:public}}") String defaultSchema,
      @Value("${spring.flyway.schemas:${app.db.schema:public}}") List<String> schemas,
      @Value("${spring.flyway.baseline-on-migrate:true}") boolean baselineOnMigrate,
      @Value("${spring.flyway.create-schemas:true}") boolean createSchemas) {
    log.info(
        "Configuring Flyway with locations={}, defaultSchema={}, schemas={}",
        locations,
        defaultSchema,
        schemas);

    return Flyway.configure()
        .dataSource(dataSource)
        .locations(locations.toArray(String[]::new))
        .defaultSchema(defaultSchema)
        .schemas(schemas.toArray(String[]::new))
        .baselineOnMigrate(baselineOnMigrate)
        .createSchemas(createSchemas)
        .load();
  }

  @Bean
  ApplicationRunner flywayRunner(Flyway flyway) {
    return args -> {
      MigrateResult result = flyway.migrate();
      log.info(
          "Flyway migration complete: success={}, schema={}, migrationsExecuted={}",
          result.success,
          result.schemaName,
          result.migrationsExecuted);
    };
  }
}

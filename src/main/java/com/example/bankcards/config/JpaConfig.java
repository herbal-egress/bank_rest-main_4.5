
package com.example.bankcards.config;


import com.example.bankcards.entity.converter.YearMonthAttributeConverter;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j 
public class JpaConfig {

    
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.bankcards.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
        
        
        jpaProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                new CamelCaseToUnderscoresNamingStrategy());
        
        jpaProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                new ImplicitNamingStrategyLegacyJpaImpl());

        em.setJpaPropertyMap(jpaProperties);
        log.info("EntityManagerFactory настроен с CamelCaseToUnderscoresNamingStrategy"); 

        return em;
    }

    
    @Bean
    public YearMonthAttributeConverter yearMonthAttributeConverter() {
        return new YearMonthAttributeConverter();
    }
}
// config/JpaConfig.java
package com.example.bankcards.config;

// Добавленный код: Конфигурационный класс для регистрации конвертеров атрибутов JPA.
import com.example.bankcards.entity.converter.YearMonthAttributeConverter;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JpaConfig {

    // Изменено ИИ: Исправлен импорт SpringPhysicalNamingStrategy на PhysicalNamingStrategyStandardImpl
    // (удален в Spring Boot 3.x) и SpringImplicitNamingStrategy на ImplicitNamingStrategyComponentPathImpl
    // (соответствует стратегии по умолчанию в Hibernate 6.x).
    // Добавленный код: Бин для настройки фабрики EntityManager. Регистрирует конвертер YearMonthAttributeConverter.
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.bankcards.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
        // Изменено ИИ: Используем PhysicalNamingStrategyStandardImpl вместо удаленного SpringPhysicalNamingStrategy
        jpaProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, PhysicalNamingStrategyStandardImpl.class.getName());
        // Изменено ИИ: Используем ImplicitNamingStrategyComponentPathImpl вместо SpringImplicitNamingStrategy
        // (соответствует поведению Spring Boot 3.x по умолчанию)
        jpaProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, ImplicitNamingStrategyComponentPathImpl.class.getName());
        // Добавленный код: Регистрируем конвертер YearMonthAttributeConverter глобально для всех сущностей
        jpaProperties.put("hibernate.attributeConverter", yearMonthAttributeConverter());
        em.setJpaPropertyMap(jpaProperties);

        return em;
    }

    // Добавленный код: Объявляем бин нашего конвертера как Spring Bean
    @Bean
    public YearMonthAttributeConverter yearMonthAttributeConverter() {
        return new YearMonthAttributeConverter();
    }
}
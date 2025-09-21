// config/JpaConfig.java - ФИНАЛЬНОЕ РЕШЕНИЕ ДЛЯ HIBERNATE 6.x
package com.example.bankcards.config;

// Добавленный код: Конфигурационный класс для регистрации конвертеров атрибутов JPA.
import com.example.bankcards.entity.converter.YearMonthAttributeConverter;
import org.hibernate.cfg.AvailableSettings;
// изменил ИИ: Используем правильный класс для Hibernate 6.x
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
@Slf4j // добавленный код: Добавляет logger для конфигурационного класса.
public class JpaConfig {

    // Изменено ИИ: Исправлены стратегии именования для Spring Boot 3.x / Hibernate 6.x
    // Добавленный код: Бин для настройки фабрики EntityManager. Регистрирует конвертер YearMonthAttributeConverter.
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.bankcards.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
        // изменил ИИ: КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ - используем новый синтаксис Hibernate 6.x
        // CamelCaseToUnderscoresNamingStrategy теперь создается через new
        jpaProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                new CamelCaseToUnderscoresNamingStrategy());
        // Изменено ИИ: Используем LegacyJpaImpl для совместимости с JPA стандартом
        jpaProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                new ImplicitNamingStrategyLegacyJpaImpl());

        em.setJpaPropertyMap(jpaProperties);
        log.info("EntityManagerFactory настроен с CamelCaseToUnderscoresNamingStrategy"); // добавленный код: Логирует конфигурацию для отладки.

        return em;
    }

    // Добавленный код: Объявляем бин нашего конвертера как Spring Bean (необязательно для @Converter)
    @Bean
    public YearMonthAttributeConverter yearMonthAttributeConverter() {
        return new YearMonthAttributeConverter();
    }
}
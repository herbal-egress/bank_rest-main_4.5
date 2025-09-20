// entity/converter/YearMonthAttributeConverter.java
package com.example.bankcards.entity.converter;

// Добавленный код: Конвертер для преобразования между Java YearMonth и SQL Date.
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Date;
import java.time.YearMonth;

@Converter(autoApply = true) // Добавленный код: autoApply = true применяет конвертер ко всем полям типа YearMonth.
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, Date> {

    // Добавленный код: Конвертирует YearMonth в java.sql.Date для сохранения в БД. Дата всегда устанавливается на первое число месяца.
    @Override
    public Date convertToDatabaseColumn(YearMonth yearMonth) {
        if (yearMonth != null) {
            return Date.valueOf(yearMonth.atDay(1));
        }
        return null;
    }

    // Добавленный код: Конвертирует java.sql.Date обратно в YearMonth при загрузке из БД.
    @Override
    public YearMonth convertToEntityAttribute(Date date) {
        if (date != null) {
            return YearMonth.from(date.toLocalDate());
        }
        return null;
    }
}
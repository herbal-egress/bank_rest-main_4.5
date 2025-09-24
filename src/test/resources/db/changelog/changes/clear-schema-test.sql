-- добавленный код: SQL-скрипт для каскадной очистки всей схемы 'test'
-- Это удаляет схему целиком, включая все таблицы, индексы, последовательности и зависимости (CASCADE)
-- Рекомендуется для сброса тестового окружения перед/после тестов
DROP SCHEMA IF EXISTS test CASCADE;

-- добавленный код: Отладочная информация после очистки
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'test') THEN
            RAISE NOTICE 'Схема test успешно удалена';
        ELSE
            RAISE NOTICE 'Ошибка: схема test не удалена';
        END IF;
    END $$;
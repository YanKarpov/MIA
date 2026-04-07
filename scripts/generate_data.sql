DO $$
DECLARE
    i INTEGER;
    quest_type TEXT;
    targets TEXT[];
    target TEXT;
    amount INT;
    success BOOLEAN;
    kills INT := 0;
    deaths INT := 0;
    completed INT := 0;
    total INT := 0;
    success_rate FLOAT;
    percent INT;
BEGIN
    FOR i IN 1..100 LOOP
        quest_type := (ARRAY['Kill', 'Collect', 'Break'])[floor(random() * 3 + 1)];
        
        IF quest_type = 'Kill' THEN
            targets := ARRAY['ZOMBIE', 'SKELETON', 'SPIDER', 'CREEPER'];
        ELSIF quest_type = 'Collect' THEN
            targets := ARRAY['STICK', 'IRON_INGOT', 'DIAMOND'];
        ELSE
            targets := ARRAY['STONE', 'COBBLESTONE', 'DIRT'];
        END IF;
        target := targets[floor(random() * array_length(targets, 1) + 1)];
        
        -- Сложность зависит от прогресса
        IF i < 30 THEN
            amount := floor(random() * 5 + 1);  -- 1-5
            success := random() < 0.9;
        ELSIF i < 70 THEN
            amount := floor(random() * 15 + 5); -- 5-20
            success := random() < 0.6;
        ELSE
            amount := floor(random() * 30 + 20); -- 20-50
            success := random() < 0.3;
        END IF;
        
        -- Сохраняем состояние ДО
        success_rate := completed::FLOAT / NULLIF(total, 0);
        
        INSERT INTO quests (
            player_id, type, target, amount, reward, status,
            deaths_before, kills_before, success_rate_before,
            deaths_after, kills_after, issued_at, completed_at
        ) VALUES (
            1, quest_type, target, amount, amount * 5,
            CASE WHEN success THEN 'COMPLETED' ELSE 'FAILED' END,
            deaths, kills, COALESCE(success_rate, 0.5),
            deaths + CASE WHEN NOT success AND quest_type = 'Kill' THEN floor(random() * 3 + 1) ELSE 0 END,
            kills + CASE WHEN success AND quest_type = 'Kill' THEN amount ELSE 0 END,
            NOW() - (random() * interval '10 days'),
            NOW()
        );
        
        -- Обновляем статистику
        IF success THEN
            completed := completed + 1;
            IF quest_type = 'Kill' THEN
                kills := kills + amount;
            END IF;
        ELSE
            IF quest_type = 'Kill' THEN
                deaths := deaths + floor(random() * 3 + 1);
            END IF;
        END IF;
        total := total + 1;
    END LOOP;
    
    percent := round((completed::FLOAT / total) * 100);
    RAISE NOTICE 'Готово! Сгенерировано % записей. Успешных: %/% (%)', 
                 total, completed, total, percent;
END $$;

-- Обновляем статистику игрока
UPDATE players SET 
    total_deaths = (SELECT COALESCE(SUM(deaths_after), 0) FROM quests WHERE player_id = 1),
    total_kills = (SELECT COALESCE(SUM(kills_after), 0) FROM quests WHERE player_id = 1),
    total_quests = (SELECT COUNT(*) FROM quests WHERE player_id = 1),
    completed_quests = (SELECT COUNT(*) FROM quests WHERE player_id = 1 AND status = 'COMPLETED'),
    success_rate = (SELECT COALESCE(COUNT(*) FILTER (WHERE status = 'COMPLETED')::FLOAT / NULLIF(COUNT(*), 0), 0) 
                    FROM quests WHERE player_id = 1)
WHERE id = 1;
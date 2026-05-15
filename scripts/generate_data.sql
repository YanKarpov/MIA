DO $$
DECLARE
    i INTEGER;
    quest_type TEXT;
    targets TEXT[];
    target TEXT;
    amount INT;
    reward INT;
    success BOOLEAN;
    player_id INT := 33;
    successful_quests INT := 0;
    failed_quests INT := 0;
    total_kills_before INT := 0;
    total_deaths_before INT := 0;
    success_rate_before FLOAT := 0.0;
    quest_date TIMESTAMP;
    kills_gained INT;
    deaths_gained INT;
BEGIN
    FOR i IN 1..500 LOOP
        
        IF i < 150 THEN
            quest_type := (ARRAY['KILL', 'KILL', 'KILL', 'COLLECT', 'BREAK'])[floor(random() * 5 + 1)];
        ELSIF i < 350 THEN
            quest_type := (ARRAY['KILL', 'COLLECT', 'BREAK'])[floor(random() * 3 + 1)];
        ELSE
            quest_type := (ARRAY['KILL', 'COLLECT', 'COLLECT', 'BREAK', 'BREAK'])[floor(random() * 5 + 1)];
        END IF;
        
        IF quest_type = 'KILL' THEN
            targets := ARRAY['ZOMBIE', 'SKELETON', 'SPIDER', 'CREEPER', 'ENDERMAN', 'WITCH', 'DROWNED'];
            target := targets[floor(random() * array_length(targets, 1) + 1)];
        ELSIF quest_type = 'COLLECT' THEN
            targets := ARRAY['ROTTEN_FLESH', 'STRING', 'FEATHER', 'BONE', 'GUNPOWDER', 'ENDER_PEARL', 'SPIDER_EYE'];
            target := targets[floor(random() * array_length(targets, 1) + 1)];
        ELSE
            targets := ARRAY['STONE', 'GRAVEL', 'DIRT', 'COBBLESTONE', 'SAND', 'SANDSTONE', 'GRANITE'];
            target := targets[floor(random() * array_length(targets, 1) + 1)];
        END IF;
        
        IF i < 100 THEN
            amount := floor(random() * 5 + 1);
            reward := amount * 6 + floor(random() * 10);
            success := random() < 0.85;
        ELSIF i < 250 THEN
            amount := floor(random() * 12 + 3);
            reward := amount * 5 + floor(random() * 20);
            success := random() < 0.65;
        ELSIF i < 400 THEN
            amount := floor(random() * 20 + 8);
            reward := amount * 5 + floor(random() * 30);
            success := random() < 0.45;
        ELSE
            amount := floor(random() * 30 + 15);
            reward := amount * 5 + floor(random() * 50);
            success := random() < 0.25;
        END IF;
        
        kills_gained := 0;
        deaths_gained := 0;
        
        IF success THEN
            IF quest_type = 'KILL' THEN
                kills_gained := amount;
            END IF;
        ELSE
            IF quest_type = 'KILL' THEN
                deaths_gained := floor(random() * 3 + 1);
            END IF;
        END IF;
        
        success_rate_before := successful_quests::FLOAT / NULLIF(successful_quests + failed_quests, 0);
        
        quest_date := NOW() - (random() * interval '60 days');
        
        INSERT INTO quests (
            player_id, type, target, amount, reward, status,
            deaths_before, kills_before, success_rate_before,
            deaths_after, kills_after,
            issued_at, completed_at
        ) VALUES (
            player_id, quest_type, target, amount, reward,
            CASE WHEN success THEN 'COMPLETED' ELSE 'FAILED' END,
            total_deaths_before, total_kills_before, COALESCE(success_rate_before, 0.5),
            total_deaths_before + deaths_gained, total_kills_before + kills_gained,
            quest_date,
            CASE WHEN success THEN quest_date + interval '1 hour' ELSE NULL END
        );
        
        IF success THEN
            successful_quests := successful_quests + 1;
            total_kills_before := total_kills_before + kills_gained;
        ELSE
            failed_quests := failed_quests + 1;
            total_deaths_before := total_deaths_before + deaths_gained;
        END IF;
        
    END LOOP;
    
    RAISE NOTICE 'Generation completed for player %', player_id;
    RAISE NOTICE 'Total quests: %', successful_quests + failed_quests;
    RAISE NOTICE 'Successful: %', successful_quests;
    RAISE NOTICE 'Failed: %', failed_quests;
    RAISE NOTICE 'Total kills: %', total_kills_before;
    RAISE NOTICE 'Total deaths: %', total_deaths_before;
    
END $$;
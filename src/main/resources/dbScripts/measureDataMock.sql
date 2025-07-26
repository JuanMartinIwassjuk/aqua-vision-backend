DELIMITER $$

DROP PROCEDURE IF EXISTS measure_data_mock_sp$$

CREATE PROCEDURE measure_data_mock_sp()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE total_sectores INT;

SELECT COUNT(*) INTO total_sectores FROM Sector;

WHILE i < 1000000 DO
        INSERT INTO Medicion (flow, timestamp, sector_id)
        VALUES (
            FLOOR(RAND() * 100),
            NOW() - INTERVAL FLOOR(RAND() * 100000) MINUTE,
            (SELECT id FROM Sector ORDER BY RAND() LIMIT 1)
        );
        SET i = i + 1;
END WHILE;
END$$
DELIMITER ;

CALL measure_data_mock_sp();
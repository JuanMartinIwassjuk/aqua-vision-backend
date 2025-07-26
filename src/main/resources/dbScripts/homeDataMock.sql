DELIMITER $$

DROP PROCEDURE IF EXISTS home_data_mock_sp$$

CREATE PROCEDURE home_data_mock_sp()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE cant_sectores INT;
    DECLARE j INT;

    WHILE i <= 1000 DO
        INSERT INTO Hogar (miembros, localidad) VALUES (
            FLOOR(RAND() * 5) + 1,
            CONCAT('Localidad_', i)
        );

        SET cant_sectores = FLOOR(RAND() * 3) + 1; -- entre 1 y 3 sectores
        SET j = 1;

        WHILE j <= cant_sectores DO
            INSERT INTO Sector (nombre, categoria, hogar_id)
            VALUES (
                CONCAT('Sector_', i, '_', j),
                ELT(FLOOR(RAND() * 4) + 1, 'HOGAR', 'BAÑO', 'COCINA', 'PATIO'),
                i
            );
            SET j = j + 1;
END WHILE;

        SET i = i + 1;
END WHILE;
END$$
DELIMITER ;

CALL home_data_mock_sp();
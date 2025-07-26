CREATE TABLE IF NOT EXISTS Role_ (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE
    );


CREATE TABLE IF NOT EXISTS User_ (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL
    );


CREATE TABLE IF NOT EXISTS Usuarios_Roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES Role_(id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES Role_(id)
    );


CREATE TABLE IF NOT EXISTS Hogar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    miembros INT NOT NULL DEFAULT 1,
    localidad VARCHAR(255) NOT NULL DEFAULT ''
    );


CREATE TABLE IF NOT EXISTS Sector (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    categoria VARCHAR(20),
    hogar_id BIGINT,

    CONSTRAINT fk_sector_hogar FOREIGN KEY (hogar_id) REFERENCES Hogar(id)
    );


CREATE TABLE IF NOT EXISTS Medicion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow INT NOT NULL,
    timestamp DATETIME NOT NULL,
    sector_id BIGINT,

    CONSTRAINT fk_medicion_sector FOREIGN KEY (sector_id) REFERENCES Sector(id)
    );

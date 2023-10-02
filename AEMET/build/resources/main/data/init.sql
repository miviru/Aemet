drop table if exists tiempo_data
CREATE TABLE IF NOT EXISTS tiempo_data (
    localidad VARCHAR(255),
    provincia VARCHAR(255),
    temp_max FLOAT,
    hora_temp_max TIME,
    temp_min FLOAT,
    hora_temp_min TIME,
    precipitacion FLOAT,
    dia DATE
);
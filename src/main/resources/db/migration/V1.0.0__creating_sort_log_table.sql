CREATE TABLE sorting_logs (
    id SERIAL PRIMARY KEY,
    algorithm VARCHAR(50) NOT NULL,
    array_size INT NOT NULL,
    execution_time BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
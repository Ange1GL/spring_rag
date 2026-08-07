-- Habilita la extensión vectorial de PostgreSQL (pgvector)
CREATE EXTENSION IF NOT EXISTS vector;

-- Habilita la generación de UUIDs (usada para el id de la tabla vector_store)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Esquema esperado por PgVectorStore de Spring AI (verificado contra spring-ai-pgvector-store 2.0.0)
CREATE TABLE IF NOT EXISTS vector_store (
    id         uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content    text,
    metadata   json,
    embedding  vector(1536)
);

-- Índice HNSW para búsqueda por similitud coseno (defaults de Spring AI)
CREATE INDEX IF NOT EXISTS spring_ai_vector_index
    ON vector_store USING hnsw (embedding vector_cosine_ops);

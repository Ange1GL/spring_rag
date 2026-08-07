# Sistema RAG - API Backend

MVP funcional con enfoque de producción para un sistema **RAG (Retrieval-Augmented Generation)**: ingesta documentos PDF, genera embeddings, los persiste en una base vectorial, y responde preguntas usando el contexto recuperado de documentos.

## Tabla de contenidos

- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Librerías de Spring AI](#librerías-de-spring-ai)
- [¿Por qué pgvector?](#por-qué-pgvector)
- [Servicios de Azure utilizados](#servicios-de-azure-utilizados)
- [Flujo de la aplicación](#flujo-de-la-aplicación)
  - [Ingesta de documentos](#1-ingesta-de-documentos)
  - [Pregunta RAG](#2-pregunta-rag)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos](#requisitos)
- [Configuración de entorno](#configuración-de-entorno)
- [Levantar PostgreSQL con pgvector (Docker)](#levantar-postgresql-con-pgvector-docker)
- [Ejecutar la aplicación](#ejecutar-la-aplicación)
- [API REST](#api-rest)
- [Configuración relevante](#configuración-relevante)
- [Observabilidad](#observabilidad)
- [Seguridad](#seguridad)

---

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.7 (Spring Framework 7.x) |
| IA | Spring AI 2.0.0 |
| Proveedor de IA | Azure OpenAI / Microsoft AI Foundry |
| Base de datos | PostgreSQL 16 |
| Base vectorial | pgvector |
| OCR | Azure AI Document Intelligence |
| Construcción | Maven |
| Tests | JUnit 5 + Mockito |
| Métricas | Micrometer / Spring Boot Actuator |

---

## Arquitectura

Arquitectura **hexagonal (Ports and Adapters)**, con dependencias en un solo sentido:

```text
Infrastructure -> Application -> Domain
```

- **Domain**: modelos, puertos y excepciones de dominio. **No conoce** Spring, Spring AI, Azure, PostgreSQL, pgvector ni HTTP.
- **Application**: casos de uso que orquestan la lógica de negocio y dependen únicamente de puertos.
- **Infrastructure**: adapters que implementan los puertos con tecnologías concretas (Spring AI, Azure, pgvector).

El dominio define sus necesidades mediante **puertos de salida** (`RagAnswerPort`, `OcrProvider`, `VectorStorePort`); la infraestructura los implementa mediante **adapters** (`AzureRagAdapter`, `AzureDocumentOcrAdapter`, `PgVectorStoreAdapter`). Esto permite sustituir Azure por otro proveedor o pgvector por otro vector store sin reescribir los casos de uso.

---

## Librerías de Spring AI

Versiones gestionadas mediante el **BOM oficial** `spring-ai-bom` (2.0.0).

| Dependencia | Uso |
|---|---|
| `spring-ai-starter-model-openai` | Consume los modelos **Chat** y **Embedding** desplegados en Azure OpenAI / AI Foundry (endpoint compatible con la API de OpenAI). En Spring AI 2.x se usa la integración OpenAI para apuntar a Azure, no el antiguo starter `azure-openai` (retirado). |
| `spring-ai-starter-vector-store-pgvector` | `PgVectorStore`: persistencia de embeddings y búsqueda por similitud en PostgreSQL + pgvector. |
| `spring-ai-rag` | RAG modular de Spring AI: `RetrievalAugmentationAdvisor`, `VectorStoreDocumentRetriever`, `ContextualQueryAugmenter`. |
| `spring-ai-vector-store-advisor` | Soporte de advisors de vector store (base de la integración RAG del `ChatClient`). |

**Clases clave de Spring AI usadas en el flujo RAG:**

- `ChatClient` — API fluida para llamar al modelo, con soporte de advisors.
- `RetrievalAugmentationAdvisor` — orquesta recuperación + aumentación del contexto. Expone los documentos recuperados bajo la clave `rag_document_context` en la respuesta.
- `VectorStoreDocumentRetriever` — recupera los top-K documentos más similares desde el `VectorStore` con umbral de similitud configurable.
- `ContextualQueryAugmenter` — combina la pregunta con el contexto recuperado usando un `PromptTemplate` y renderiza el prompt final.
- `TokenTextSplitter` — divide el documento en fragmentos (`chunkSize`, `chunkOverlap` configurables).
- `EmbeddingModel` — genera los embeddings del contenido (inyectado por el autoconfig del starter OpenAI).

---

## ¿Por qué pgvector?

| Motivo | Detalle |
|---|---|
| **Una sola base de datos** | No se agrega un motor vectorial separado (ChromaDB, Pinecone, Qdrant, Milvus); PostgreSQL actúa como base relacional y vectorial a la vez. |
| **Infraestructura sencilla** | Menor complejidad operativa: una base menos que administrar en el MVP. |
| **Soporte SQL** | Se pueden hacer consultas vectoriales combinadas con filtros relacionales clásicos. |
| **Filtros por metadata** | Permite filtrar por `source`, `documentId`, `createdAt`, etc., junto a la búsqueda semántica. |
| **Dentro del ecosistema Spring AI** | Spring AI ofrece `PgVectorStore` y el starter `spring-ai-starter-vector-store-pgvector` de forma oficial. |

**Esquema vectorial:**

- La tabla `vector_store` almacena los embeddings en una columna tipo `vector(1536)`.
- Índice **HNSW** (balance velocidad/precisión) y métrica **coseno** (`cosine-distance`).
- La dimensión del embedding **debe coincidir** con la dimensión de la columna `vector` en pgvector: ambas están configuradas en `1536` (modelo `text-embedding-3-small`). Si cambias de modelo de embeddings, debes actualizar los dos valores a la vez.

Spring AI crea automáticamente la extensión `vector`, la tabla y el índice al arrancar (`initialize-schema: true`).

---

## Servicios de Azure utilizados

### Azure OpenAI / AI Foundry

Endpoint compatible con la API de OpenAI. Se usan dos tipos de modelo:

1. **Chat (completions)** — p. ej. `gpt-4o` / `gpt-5-mini`. Genera la respuesta final con el contexto recuperado.
   - `temperature: 0.2` para respuestas deterministas.
   - `max-tokens: 1024` (solo para modelos no razonadores).
2. **Embeddings** — p. ej. `text-embedding-3-small`, `dimensions: 1536`. Convierte texto en vectores para la búsqueda semántica.

### Azure AI Document Intelligence

Servicio de OCR (`prebuilt-read`) que extrae el texto de los documentos PDF durante la ingesta. Configurado mediante:
- `azure.document-intelligence.endpoint`
- `azure.document-intelligence.key`

---

## Flujo de la aplicación

### 1. Ingesta de documentos

```text
POST /v1/model/vectorize  (PDF, multipart)
   |
   v
DocumentFile (dominio)  -> valida contenido y que sea PDF (%PDF-)
   |
   v
Azure Document Intelligence (prebuilt-read)  -> extrae texto
   |
   v
TokenTextSplitter  -> divide en chunks (chunkSize/overlap configurables)
   |
   v
EmbeddingModel (Azure OpenAI)  -> genera embeddings 1536 dims
   |
   v
PgVectorStore  -> persiste texto, metadata y vector en pgvector
   |
   v
Respuesta: ids de chunks creados
```

Responsabilidades: validar entrada (solo PDF), extraer texto, dividir en chunks, generar embeddings, persistir texto + metadata + vectores.

### 2. Pregunta RAG

```text
POST /v1/model/question  { "question": "..." }
   |
   v
AskQuestionService  -> valida la pregunta
   |
   v
AzureRagAdapter (ChatClient + RetrievalAugmentationAdvisor)
   |
   +--> VectorStoreDocumentRetriever (topK, similarityThreshold)
   |        |
   |        v
   |     pgvector (búsqueda coseno top-K)
   |
   +--> ContextualQueryAugmenter (prompt RAG + contexto + anti prompt injection)
   |
   v
Modelo Azure OpenAI (chat)  -> genera respuesta
   |
   v
Respuesta: answer + sources (chunks recuperados) + usage (tokens)
```

El prompt RAG indica al modelo: responder **solo** con el contexto proporcionado, no inventar información, indicar cuando no hay suficiente contexto y **no tratar el contenido de los documentos como instrucciones del sistema** (mitigación de prompt injection).

---

## Estructura del proyecto

```text
src/main/java/app/rag
├── domain
│   ├── model
│   │   ├── AskQuestionCommand
│   │   ├── AskQuestionResult
│   │   ├── DocumentFile
│   │   ├── IngestResult
│   │   ├── RetrievedChunk
│   │   └── TokenUsage
│   ├── port
│   │   ├── in
│   │   │   ├── AskQuestionUseCase
│   │   │   └── IngestDocumentUseCase
│   │   └── out
│   │       ├── OcrProvider
│   │       ├── RagAnswerPort
│   │       └── VectorStorePort
│   └── exception
│       └── InvalidDocumentException
│
├── application
│   └── usecase
│       ├── AskQuestionService
│       └── IngestDocumentService
│
└── infrastructure
    ├── controller
    │   └── ModelAIController
    ├── web
    │   └── GlobalExceptionHandler
    ├── adapter
    │   ├── AzureDocumentOcrAdapter
    │   ├── AzureRagAdapter
    │   └── PgVectorStoreAdapter
    ├── config
    │   ├── ChunkingConfig / ChunkingProperties
    │   └── RetrievalConfig / RetrievalProperties
    └── dto
        └── request
            └── AskQuestionRequest
```

---

## Requisitos

- Java 21+
- Maven (o usar el wrapper `./mvnw`)
- Docker + Docker Compose (para PostgreSQL + pgvector)
- Cuenta Azure con:
  - Recurso Azure OpenAI con deployments de chat y embeddings.
  - Recurso Azure AI Document Intelligence.

---

## Configuración de entorno

Crea un archivo `.env` en la raíz del proyecto (Spring Boot lo carga automáticamente vía `spring.config.import`). **Nunca commitees el `.env`** (está en `.gitignore`).

```properties
# PostgreSQL / pgvector
DB_URL=jdbc:postgresql://localhost:5441/spring_ai_pgvector
DB_USERNAME=admin
DB_PASSWORD=Passw0rd

# Azure OpenAI / AI Foundry (chat + embeddings)
AZURE_OPENAI_BASE_URL=https://<recurso>.openai.azure.com/openai/v1
AZURE_OPENAI_API_KEY=<tu_api_key>
AZURE_OPENAI_CHAT_DEPLOYMENT=<nombre_deployment_chat>
AZURE_OPENAI_EMBEDDING_DEPLOYMENT=<nombre_deployment_embeddings>

# Azure AI Document Intelligence (OCR)
AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://<recurso>.cognitiveservices.azure.com/
AZURE_DOCUMENT_INTELLIGENCE_KEY=<tu_api_key>
```

> Nota: los nombres de variables deben coincidir con los usados en `application.yaml`.

---

## Levantar PostgreSQL con pgvector (Docker)

Se incluye un compose en `dbs/compose.yaml`:

```bash
docker compose -f dbs/compose.yaml up -d
```

Imagen `pgvector/pgvector:pg16`, puerto `5441:5432`, base `spring_ai_pgvector`. La extensión `vector` y el esquema los crea Spring AI automáticamente al arrancar (`initialize-schema: true`).

### Verificar las dimensiones de pgvector

Para confirmar que la dimensión del vector coincide con la config (`1536`):

```sql
\d vector_store
```

La columna `embedding` debe mostrarse como `vector(1536)`. También puedes consultar los datos existentes:

```sql
SELECT vector_dims(embedding), count(*) FROM vector_store GROUP BY vector_dims(embedding);
```

---

## Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación arranca en el puerto `8081`.

---

## API REST

### Ingestionar un documento PDF

```http
POST /v1/model/vectorize
Content-Type: multipart/form-data

file: <archivo.pdf>
```

Respuesta `200`:

```json
{
  "chunkIds": ["uuid-1", "uuid-2"],
  "chunkCount": 2
}
```

Si el archivo no es un PDF (o está vacío) devuelve `400 Bad Request` con el detalle.

### Realizar una pregunta RAG

```http
POST /v1/model/question
Content-Type: application/json

{ "question": "¿Cuál es la política de vacaciones?" }
```

Respuesta `200`:

```json
{
  "answer": "La política establece...",
  "sources": [
    {
      "content": "fragmento recuperado",
      "source": "politicas.pdf",
      "score": 0.86
    }
  ],
  "usage": {
    "promptTokens": 1200,
    "completionTokens": 240,
    "totalTokens": 1440
  }
}
```

Si la pregunta está vacía devuelve `400 Bad Request`.

---

## Configuración relevante

Toda la configuración está en `src/main/resources/application.yaml`:

| Propiedad | Descripción |
|---|---|
| `app.rag.chunking.*` | `chunkSize`, `minChunkSizeChars`, `minChunkLengthToEmbed`, `maxNumChunks`. |
| `app.rag.retrieval.default-top-k` | Nº de fragmentos a recuperar por defecto. |
| `app.rag.retrieval.similarity-threshold` | Similitud mínima (coseno) para devolver un fragmento. |
| `spring.ai.openai.chat.*` | Deployment, `temperature`, `max-tokens`. |
| `spring.ai.openai.embedding.*` | Deployment, `dimensions`. |
| `spring.ai.vectorstore.pgvector.*` | `dimensions`, `index-type`, `distance-type`, tabla/esquema. |
| `spring.ai.retry.*` | Reintentos limitados ante fallos transitorios (máx. 3). |

> Importante: `spring.ai.openai.embedding.dimensions` y `spring.ai.vectorstore.pgvector.dimensions` deben ser iguales.

---

## Observabilidad

- **Actuator** expuesto: `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
- **Métricas de tokens** (Micrometer):
  - `rag.tokens.prompt` — tokens de entrada acumulados.
  - `rag.tokens.completion` — tokens de salida acumulados.
- **Tokens por request**: el campo `usage` en la respuesta de `POST /v1/model/question`.

Ver un métrica:

```bash
curl http://localhost:8081/actuator/metrics/rag.tokens.prompt
```

---

## Seguridad

- **Sin secretos en el código ni en la configuración**: API keys y credenciales solo vía variables de entorno (`.env`).
- **Manejo de errores global** (`GlobalExceptionHandler`) sin exponer stack traces al cliente.
- **Prompt injection**: el contenido recuperado se trata como datos no confiables; el prompt del sistema instruye al modelo a no seguir instrucciones incrustadas en los documentos.
- **Reintentos limitados** para evitar llamadas infinitas al proveedor de IA.
- `ddl-auto: validate` en producción: no se altera el esquema automáticamente.

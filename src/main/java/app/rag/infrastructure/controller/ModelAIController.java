package app.rag.infrastructure.controller;

import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;
import app.rag.domain.model.IngestResult;
import app.rag.domain.port.in.AskQuestionUseCase;
import app.rag.domain.port.in.IngestDocumentUseCase;
import app.rag.domain.port.out.ModelAIProvider;
import app.rag.infrastructure.dto.request.AskModelAIRequest;
import app.rag.infrastructure.dto.request.AskQuestionRequest;
import app.rag.infrastructure.dto.request.VectorizeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/model")
@RequiredArgsConstructor
public class ModelAIController {

    private final ModelAIProvider modelAIProvider;
    private final IngestDocumentUseCase ingestDocumentUseCase;
    private final AskQuestionUseCase askQuestionUseCase;

    @PostMapping
    public ResponseEntity<String> ask(
            @RequestBody AskModelAIRequest request
            ) {
       String result =   modelAIProvider.ask(request.prompt());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/vectorize")
    public ResponseEntity<IngestResult> vectorize(
            @RequestBody VectorizeRequest request
            ) {
        IngestResult result = ingestDocumentUseCase.ingest(request.text());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/question")
    public ResponseEntity<AskQuestionResult> question(
            @RequestBody AskQuestionRequest request
            ) {
        AskQuestionCommand command = new AskQuestionCommand(
                request.question(),
                request.additionalContext(),
                request.topK() != null ? request.topK() : 0);
        return ResponseEntity.ok(askQuestionUseCase.ask(command));
    }
}

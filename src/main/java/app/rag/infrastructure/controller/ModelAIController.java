package app.rag.infrastructure.controller;

import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;
import app.rag.domain.model.IngestResult;
import app.rag.domain.port.in.AskQuestionUseCase;
import app.rag.domain.port.in.IngestDocumentUseCase;
import app.rag.infrastructure.dto.request.AskQuestionRequest;
import app.rag.infrastructure.dto.request.VectorizeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/model")
@RequiredArgsConstructor
public class ModelAIController {

    private final IngestDocumentUseCase ingestDocumentUseCase;
    private final AskQuestionUseCase askQuestionUseCase;


    @PostMapping("/vectorize")
    public ResponseEntity<IngestResult> vectorize(
            @RequestParam("file") MultipartFile file
            ) throws IOException {
        IngestResult result = ingestDocumentUseCase.ingest(file.getBytes());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/question")
    public ResponseEntity<AskQuestionResult> question(
            @RequestBody AskQuestionRequest request
            ) {
        AskQuestionCommand command = new AskQuestionCommand(
                request.question());
        return ResponseEntity.ok(askQuestionUseCase.ask(command));
    }
}

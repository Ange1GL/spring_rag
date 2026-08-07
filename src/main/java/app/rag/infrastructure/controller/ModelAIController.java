package app.rag.infrastructure.controller;

import app.rag.domain.port.out.ModelAIProvider;
import app.rag.infrastructure.dto.request.AskModelAIRequest;
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

    @PostMapping
    public ResponseEntity<String> ask(
            @RequestBody AskModelAIRequest request
            ) {
       String result =   modelAIProvider.ask(request.prompt());
        return ResponseEntity.ok(result);
    }
}

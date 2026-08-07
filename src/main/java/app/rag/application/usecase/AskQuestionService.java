package app.rag.application.usecase;

import app.rag.infrastructure.config.RetrievalProperties;
import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;
import app.rag.domain.model.RetrievedChunk;
import app.rag.domain.port.in.AskQuestionUseCase;
import app.rag.domain.port.out.ModelAIProvider;
import app.rag.domain.port.out.SemanticSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AskQuestionService implements AskQuestionUseCase {

    private final SemanticSearchPort semanticSearchPort;
    private final ModelAIProvider modelAIProvider;
    private final RagPromptBuilder ragPromptBuilder;
    private final RetrievalProperties retrievalProperties;

    @Override
    public AskQuestionResult ask(AskQuestionCommand command) {
        if (command.question() == null || command.question().isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        int topK = command.topK() > 0 ? command.topK() : retrievalProperties.defaultTopK();
        List<RetrievedChunk> chunks = semanticSearchPort.search(command.question(), topK);
        String prompt = ragPromptBuilder.build(command.question(), chunks, command.additionalContext());
        String answer = modelAIProvider.ask(prompt);
        return new AskQuestionResult(answer, chunks);
    }
}

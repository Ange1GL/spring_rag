package app.rag.application.usecase;

import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;
import app.rag.domain.port.in.AskQuestionUseCase;
import app.rag.domain.port.out.RagAnswerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AskQuestionService implements AskQuestionUseCase {

    private final RagAnswerPort ragAnswerPort;

    @Override
    public AskQuestionResult ask(AskQuestionCommand command) {
        if (command.question() == null || command.question().isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        return ragAnswerPort.ask(command);
    }
}

package app.rag.domain.port.in;

import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;

public interface AskQuestionUseCase {
    AskQuestionResult ask(AskQuestionCommand command);
}

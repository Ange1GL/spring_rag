package app.rag.domain.port.out;

import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;

public interface RagAnswerPort {
    AskQuestionResult ask(AskQuestionCommand command);
}

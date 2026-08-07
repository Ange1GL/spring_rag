package app.rag.infrastructure.adapter;

import app.rag.domain.port.out.ModelAIProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AzureOpenAIProvider
implements ModelAIProvider
{
    private final ChatModel chatModel;

    @Override
    public String ask(String prompt) {
        return chatModel.call(prompt);
    }
}

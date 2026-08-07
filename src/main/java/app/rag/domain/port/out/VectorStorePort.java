package app.rag.domain.port.out;

import java.util.List;

public interface VectorStorePort {
    List<String> store(String content, String source);
}

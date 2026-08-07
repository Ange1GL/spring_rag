package app.rag.infrastructure.adapter;

import app.rag.domain.port.out.OcrProvider;
import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AzureDocumentOcrAdapter
implements OcrProvider
{
    private final DocumentIntelligenceClient client;

    public AzureDocumentOcrAdapter(
            @Value("${azure.document-intelligence.endpoint}") String endpoint,
            @Value("${azure.document-intelligence.key}") String key
    ) {
        this.client = new DocumentIntelligenceClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
    }

    public String extractOCR(byte[] documentBytes) {
        BinaryData document = BinaryData.fromBytes(documentBytes);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller =
                client.beginAnalyzeDocument(
                        "prebuilt-read",
                        new AnalyzeDocumentOptions(document)
                );
        AnalyzeResult result = poller.getFinalResult();
        StringBuilder text = new StringBuilder();
        result.getPages().forEach(page ->
                page.getLines().forEach(line ->
                        text.append(line.getContent())
                                .append(System.lineSeparator())
                )
        );
        return text.toString();
    }
}

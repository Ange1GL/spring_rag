package app.rag.domain.model;

import app.rag.domain.exception.InvalidDocumentException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DocumentFile {

    private static final byte[] PDF_MAGIC_BYTES = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final byte[] content;
    private final String fileName;

    private DocumentFile(byte[] content, String fileName) {
        this.content = content;
        this.fileName = fileName;
    }

    public static DocumentFile of(byte[] content, String fileName, String contentType) {
        if (content == null || content.length == 0) {
            throw new InvalidDocumentException("file content is empty");
        }
        boolean isPdfByType = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean isPdfByContent = isPdfMagicBytes(content);
        if (!isPdfByType && !isPdfByContent) {
            throw new InvalidDocumentException("only PDF documents are supported");
        }
        return new DocumentFile(content, fileName);
    }

    private static boolean isPdfMagicBytes(byte[] content) {
        if (content.length < PDF_MAGIC_BYTES.length) {
            return false;
        }
        byte[] header = Arrays.copyOfRange(content, 0, PDF_MAGIC_BYTES.length);
        return Arrays.equals(header, PDF_MAGIC_BYTES);
    }

    public byte[] content() {
        return content;
    }

    public String fileName() {
        return fileName;
    }
}

package liquibase.util.xml;

import java.io.IOException;
import java.io.OutputStream;
import org.w3c.dom.Document;

public interface XmlWriter {
    public void write(Document doc, OutputStream outputStream) throws IOException;
}

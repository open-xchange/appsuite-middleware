package liquibase.dbdoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import liquibase.change.Change;
import liquibase.database.Database;

public class AuthorWriter extends HTMLWriter {

    public AuthorWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "authors"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes created by author "+object.toString();
    }

    @Override
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}

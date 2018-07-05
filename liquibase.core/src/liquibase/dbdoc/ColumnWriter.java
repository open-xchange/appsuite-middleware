package liquibase.dbdoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import liquibase.change.Change;
import liquibase.database.Database;

public class ColumnWriter extends HTMLWriter {


    public ColumnWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "columns"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes affecting column \""+object.toString() + "\"";
    }

    @Override
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}

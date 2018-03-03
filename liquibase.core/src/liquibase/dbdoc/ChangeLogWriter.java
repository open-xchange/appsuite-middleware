package liquibase.dbdoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

public class ChangeLogWriter {
    protected File outputDir;
    private ResourceAccessor resourceAccessor;

    public ChangeLogWriter(ResourceAccessor resourceAccessor, File rootOutputDir) {
        this.outputDir = new File(rootOutputDir, "changelogs");
        this.resourceAccessor = resourceAccessor;
    }

    public void writeChangeLog(String changeLog, String physicalFilePath) throws IOException {
        InputStream stylesheet = resourceAccessor.getResourceAsStream(physicalFilePath);
        if (stylesheet == null) {
            throw new IOException("Can not find "+changeLog);
        }

//        File file = outputDir;
//        String[] splitPath = (changeLog.getFilePath() + ".xml").split("/");
//        for (int i =0; i < splitPath.length; i++) {
//            String pathPart = splitPath[i];
//            file = new File(file, pathPart);
//            if (i < splitPath.length - 1) {
//                file.mkdirs();
//            }
//        }


        File xmlFile = new File(outputDir, changeLog + ".html");
        xmlFile.getParentFile().mkdirs();

        BufferedWriter changeLogStream = new BufferedWriter(new FileWriter(xmlFile, false));
        try {
            changeLogStream.write("<html><body><pre>\n");
            changeLogStream.write(StreamUtil.getStreamContents(stylesheet).replace("<", "&lt;").replace(">", "&gt;"));
            changeLogStream.write("\n</pre></body></html>");
        } finally {
            changeLogStream.close();
        }

    }


}

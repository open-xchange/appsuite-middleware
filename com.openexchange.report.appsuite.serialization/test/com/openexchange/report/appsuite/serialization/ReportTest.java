package com.openexchange.report.appsuite.serialization;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.report.appsuite.serialization.Report;

public class ReportTest {
    
    private Report report;
    private final String STORAGE_PATH = "./test/test/testfiles/";
    private final String UUID = "testreport";
    private final String PART1 = STORAGE_PATH + UUID + "_test1.part";
    private final String PART2 = STORAGE_PATH + UUID + "_test2.part";
    
    
    @Before
    public void setUp() {
        this.report = new Report(UUID, "default", new Date().getTime());
        this.report.setStorageFolderPath(STORAGE_PATH);
    }
    
    @After
    public void cleanUp() {
        LinkedList<File> parts = new LinkedList<>((Arrays.asList(new File(STORAGE_PATH).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".part");
            }
        }))));
        for (File file : parts) {
            file.delete();
        }
    }

    @Test
    public void test_reportComposition() {
        // setup testfiles
        createTestFilesReportComposition();
        // compose the report from stored parts
        this.report.composeReportFromStoredParts("container", Report.JsonObjectType.ARRAY, "root", 2);
        // does the report look like expected
        File result = new File(STORAGE_PATH + UUID + ".report");
        assertTrue("Report content was not composed. ",result.exists());
        File expected = new File(STORAGE_PATH + "expected.result");
        try {
            byte[] resultBytes = Files.readAllBytes(result.toPath());
            byte[] expectedBytes = Files.readAllBytes(expected.toPath());
            assertTrue("Composed report-content is not like expected.", Arrays.equals(resultBytes, expectedBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Are all .part files deleted
        LinkedList<File> parts = new LinkedList<>((Arrays.asList(new File(STORAGE_PATH).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".part");
            }
        }))));
        assertTrue("Part-Files were not deleted", parts.size() == 0);
    }
    
    private void createTestFilesReportComposition() {
        // copy content from permanent testfiles to .part files
        try {
            copyFileUsingStream(new File(STORAGE_PATH + "part1.test"), new File(PART1));
            copyFileUsingStream(new File(STORAGE_PATH + "part2.test"), new File(PART2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}

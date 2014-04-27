package com.openexchange.admin.diff.file.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.admin.diff.file.type.ConfFileHandler;
import com.openexchange.admin.diff.file.type.ConfigurationFileTypes;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * {@link ConfFolderFileProviderTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileUtils.class, ConfFileHandler.class })
public class ConfFolderFileProviderTest {

    @InjectMocks
    private ConfFolderFileProvider fileProvider;

    @Mock
    private File configurationFile;

    @Rule
    private final TemporaryFolder folder = new TemporaryFolder();

    List<File> configurationFiles = new ArrayList<File>();

    private final String rootFolder = "/opt/open-xchange/etc";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(ConfFileHandler.class);

        configurationFiles.add(configurationFile);
    }

    @Test
    public void testReadConfigurationFiles_listFilesNull_returnEmptyArray() {
        PowerMockito.when(FileUtils.listFiles((File) Matchers.any(), Matchers.any(String[].class), Matchers.anyBoolean())).thenReturn(null);

        List<File> readConfigurationFiles = fileProvider.readConfigurationFiles(new DiffResult(), rootFolder, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE);

        Assert.assertEquals(0, readConfigurationFiles.size());
    }

    @Test
    public void testReadConfigurationFiles_fileFound_fileInList() {
        PowerMockito.when(FileUtils.listFiles((File) Matchers.any(), Matchers.any(String[].class), Matchers.anyBoolean())).thenReturn(configurationFiles);

        List<File> readConfigurationFiles = fileProvider.readConfigurationFiles(new DiffResult(), rootFolder, ConfigurationFileTypes.CONFIGURATION_FILE_TYPE);

        Assert.assertEquals(1, readConfigurationFiles.size());
    }

    @Test
    public void testAddFilesToDiffQueue_filesNull_noFileAddedToQueue() throws IOException {
        fileProvider.addFilesToDiffQueue(new DiffResult(), null, true);

        PowerMockito.verifyStatic(Mockito.never());
        ConfFileHandler.addConfigurationFile(Matchers.anyString(), Matchers.anyString(), Matchers.anyBoolean());
    }

    @Test
    public void testAddFilesToDiffQueue_filesNotInConfFolder_noFileAddedToQueue() throws IOException {
        File newFile = folder.newFile("file1.properties");
        File newFile2 = folder.newFile("file2.properties");
        List<File> files = new ArrayList<File>();
        files.add(newFile);
        files.add(newFile2);

        fileProvider.addFilesToDiffQueue(new DiffResult(), files, true);

        PowerMockito.verifyStatic(Mockito.never());
        ConfFileHandler.addConfigurationFile(Matchers.anyString(), Matchers.anyString(), Matchers.anyBoolean());
    }

    @Test
    public void testAddFilesToDiffQueue_filesInConfFolder_addedToQueue() throws IOException {
        File newFolder = folder.newFolder("/conf/");

        File newFile = folder.newFile("file1.properties");
        FileUtils.writeStringToFile(newFile, RandomStringUtils.randomAlphanumeric(100));
        FileUtils.moveFileToDirectory(newFile, newFolder, true);

        File newFile2 = folder.newFile("file2.properties");
        FileUtils.writeStringToFile(newFile2, RandomStringUtils.randomAlphanumeric(100));
        FileUtils.moveFileToDirectory(newFile2, newFolder, true);

        List<File> files = new ArrayList<File>();
        files.add(newFile);
        files.add(newFile2);

        fileProvider.addFilesToDiffQueue(new DiffResult(), files, true);

        PowerMockito.verifyStatic(Mockito.times(2));
        ConfFileHandler.addConfigurationFile(Matchers.anyString(), Matchers.anyString(), Matchers.anyBoolean());
    }
}

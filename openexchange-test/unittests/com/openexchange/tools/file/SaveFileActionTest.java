package com.openexchange.tools.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import com.openexchange.groupware.tx.AbstractActionTest;
import com.openexchange.groupware.tx.UndoableAction;
import com.openexchange.tools.file.external.FileStorageException;
import com.openexchange.tools.file.internal.LocalFileStorageFactory;

public class SaveFileActionTest extends AbstractActionTest {

	private static final String content = "I am the test content";

	private File tempFile;

	private SaveFileAction saveFile = null;
	private FileStorage storage = null;

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        FileStorage.setFileStorageStarter(new LocalFileStorageFactory());
    }

    @Override
    protected void tearDown() throws Exception {
        rmdir(new File("file:" + tempFile.toString()));
        FileStorage.setFileStorageStarter(null);
        super.tearDown();
    }

    private static void rmdir(final File tempFile) {
        if (tempFile.isDirectory()) {
            for (final File f : tempFile.listFiles()) {
                rmdir(f);
            }
        }
        tempFile.delete();
    }

	@Override
	protected UndoableAction getAction() throws Exception {
		storage = FileStorage.getInstance(new URI("file:"+tempFile.toString()));
		saveFile = new SaveFileAction();
		saveFile.setStorage(storage);
		saveFile.setIn(new ByteArrayInputStream(content.getBytes("UTF-8")));
		return saveFile;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		assertTrue(null != saveFile.getId());
		InputStream in = null;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			in = new BufferedInputStream(storage.getFile(saveFile.getId()));
			int b = 0;
			while((b = in.read()) != -1) {
				out.write(b);
			}
		} finally {
			if(in!=null) {
				in.close();
			}
			if(out!=null) {
				out.close();
			}
		}
		final String got = new String(out.toByteArray(), "UTF-8");
		assertEquals(content, got);
	}

	@Override
	protected void verifyUndone() throws Exception {
		try {
			storage.getFile(saveFile.getId());
			fail("Expected Exception");
		} catch (final FileStorageException x) {
			assertTrue(true);
		}
	}

}

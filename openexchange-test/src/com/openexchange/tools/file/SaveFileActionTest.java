package com.openexchange.tools.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.openexchange.groupware.tx.AbstractActionTest;
import com.openexchange.groupware.tx.UndoableAction;

public class SaveFileActionTest extends AbstractActionTest {

	private static final String content = "I am the test content";
	
	private SaveFileAction saveFile = null;
	private FileStorage storage = null;
	
	@Override
	protected UndoableAction getAction() throws Exception {
		final File tempFile = File.createTempFile("filestorage", ".tmp");
		tempFile.delete();
		storage = FileStorage.getInstance(tempFile);
		saveFile = new SaveFileAction();
		saveFile.setStorage(storage);
		saveFile.setIn(new ByteArrayInputStream(content.getBytes("UTF-8")));
		return saveFile;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		assertTrue(null != saveFile.getId());
		InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			in = new BufferedInputStream(storage.getFile(saveFile.getId()));
			int b = 0;
			while((b = in.read()) != -1) {
				out.write(b);
			}
		} finally {
			if(in!=null)
				in.close();
			if(out!=null)
				out.close();
		}
		String got = new String(out.toByteArray(), "UTF-8");
		assertEquals(content, got);
	}

	@Override
	protected void verifyUndone() throws Exception {
		try {
			storage.getFile(saveFile.getId());
			fail("Expected Exception");
		} catch (IOException x) {
			assertTrue(true);
		}
	}

}

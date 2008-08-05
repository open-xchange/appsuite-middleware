package com.openexchange.ajax.attach;


public class SimpleAttachmentTest extends AbstractAttachmentTest {

	public SimpleAttachmentTest(final String name) {
		super(name);
	}

	@Override
	public int createExclusiveWritableAttachable(final String sessionId, final int folderId) throws Exception {
		return 22;
	}

	@Override
	public int getExclusiveWritableFolder(final String sessionId) throws Exception {
		return 22;
	}

	@Override
	public void removeAttachable(final int folder, final int id, final String sessionId) throws Exception {
		
	}

	@Override
	public int getModule() throws Exception {
		return 22;
	}
	
	public void testMultiple() throws Exception {
		doMultiple();
	}
	
	public void testDetach() throws Exception {
		doDetach();
	}
	
	
	public void testUpdates() throws Exception {
		doUpdates();
	}
	
	public void testAll() throws Exception {
		doAll();
	}
	
	public void testGet() throws Exception {
		doGet();
	}
	
	public void testDocument() throws Exception{
		doDocument();
	}
	
	public void testList() throws Exception {
		doList();
	}
	
	public void testQuota() throws Exception {
		doQuota();
	}
    /*public void testMany() throws Exception {
		while(true) {
			upload();
		}
	}*/


}

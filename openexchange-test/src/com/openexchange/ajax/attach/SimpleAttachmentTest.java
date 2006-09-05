package com.openexchange.ajax.attach;


public class SimpleAttachmentTest extends AbstractAttachmentTest {

	@Override
	public int createExclusiveWritableAttachable(String sessionId, int folderId) throws Exception {
		return 22;
	}

	@Override
	public int getExclusiveWritableFolder(String sessionId) throws Exception {
		return 22;
	}

	@Override
	public void removeAttachable(int folder, int id, String sessionId) throws Exception {
		
	}

	@Override
	public int getModule() throws Exception {
		return 22;
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
	
	public void notestAddMany() throws Exception {
		for(int i = 1; i < 1000000; i++) {
			if(i % 1000 == 0)
				System.out.println(i);
			upload();
		}
		clean.clear();
	}


}

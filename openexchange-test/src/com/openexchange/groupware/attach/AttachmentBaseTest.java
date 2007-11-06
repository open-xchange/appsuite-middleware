package com.openexchange.groupware.attach;

import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tx.ConfigurableDBProvider;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.tools.iterator.SearchIterator;

import java.io.*;
import java.util.*;

public class AttachmentBaseTest extends AbstractAttachmentTest {

	private static final Mode MODE = new INTEGRATION();
	
	private AttachmentBase attachmentBase;
	
	private List<AttachmentMetadata> clean = new ArrayList<AttachmentMetadata>();

	private File testFile;
	
	public void testAttach() throws Exception{
		doAttach(22,22,22);
	}
	
	public void testDetach() throws Exception {
		doDetach(22,22,22);
	}
	
	public void testGetAttachments() throws Exception{
		doGetAttachments(22,22,22);
	}
	
	public void testDelta() throws Exception {
		doDelta(22,22,22);
	}
	
	public void testNotify() throws Exception {
		doNotify(22,22,22);
	}
	
	public void testCheckPermissions() throws Exception {
		doCheckPermissions(22,22,22);
	}
	
	public void testNotExists() throws Exception {
		doNotExists(22,22,22);
	}
	
	public void testUpdate() throws Exception {
		doUpdate(22,22,22);
	}

    public void testDeleteAll() throws Exception {
        int folderId = 22;
        int attachedId = 22;
        int moduleId = 22;
        
        AttachmentMetadata attachment = getAttachment(testFile,folderId, attachedId, moduleId,true);

	
		InputStream in = null;

		try {
		    for(int i = 0; i < 10; i++) {
                AttachmentMetadata copy = new AttachmentImpl(attachment);
                attachmentBase.attachToObject(copy,in = new FileInputStream(testFile),MODE.getContext(),MODE.getUser(), null);
                clean.add(copy);

            }
		    clean.add(attachment);
		 } finally {
			 if(in != null)
				 in.close();
		 }

        attachmentBase.deleteAll(MODE.getContext());


        TimedResult res = attachmentBase.getAttachments(22,22,22,MODE.getContext(),MODE.getUser(), null);
        assertFalse("All attachments should have been deleted", res.results().hasNext());
        clean.clear();
    }

    public void doNotExists(int folderId, int attachedId, int moduleId) throws Exception{
		try {
			attachmentBase.getAttachment(folderId, attachedId, moduleId,Integer.MAX_VALUE,MODE.getContext(), MODE.getUser(),null);
			fail("Got Wrong Exception");
		} catch (OXException x) {
			assertTrue(true);
		} catch (Throwable t) {
			t.printStackTrace();
			fail("Got Wrong Exception: "+t);
		}
	}
	
	public void doUpdate(int folderId, int attachedId, int moduleId) throws Exception {
		AttachmentMetadata attachment = getAttachment(testFile,folderId, attachedId,moduleId,true);
		
		
		InputStream in = null;
		
		try {
			attachmentBase.attachToObject(attachment,in = new FileInputStream(testFile),MODE.getContext(),MODE.getUser(), null);
			clean.add(attachment);
		 } finally {
			 if(in != null)
				 in.close();
		 }
		 assertFalse(0 == attachment.getId());
		 byte[] data  = "Hallo Welt".getBytes("UTF-8");
	
		 attachment.setFilesize(data.length);
		 Date oldCreationDate = attachment.getCreationDate();
		 try {
			attachmentBase.attachToObject(attachment,in = new ByteArrayInputStream(data),MODE.getContext(),MODE.getUser(), null);
		 } finally {
			 if(in != null)
				 in.close();
		 }
		 
		 AttachmentMetadata reload = attachmentBase.getAttachment(folderId, attachedId, moduleId, attachment.getId(), MODE.getContext(), MODE.getUser(), null);
		 
		 assertFalse(reload.getCreationDate().getTime() == oldCreationDate.getTime());
		 assertEquals(reload.getFilesize(), data.length);
		 
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 in = attachmentBase.getAttachedFile(folderId, attachedId, moduleId, attachment.getId(), MODE.getContext(), MODE.getUser(), null);
		 int b;
		 while((b = in.read()) != -1) {
			 out.write(b);
		 }
		 assertEquals("Hallo Welt", new String(out.toByteArray(), "UTF-8"));
	}

	public void doAttach(int folderId, int attachedId, int moduleId) throws Exception{
		AttachmentMetadata attachment = getAttachment(testFile,folderId, attachedId,moduleId,true);
		
		AttachmentMetadata copy = new AttachmentImpl(attachment);
		
		InputStream in = null;
		
		Date now = null;
		 try {
			now = new Date();
			attachmentBase.attachToObject(attachment,in = new FileInputStream(testFile),MODE.getContext(),MODE.getUser(), null);
			clean.add(attachment);
		 } finally {
			 if(in != null)
				 in.close();
		 }
		 assertFalse(0 == attachment.getId());
		 
		 AttachmentMetadata reload = attachmentBase.getAttachment(folderId, attachedId, moduleId, attachment.getId(), MODE.getContext(), MODE.getUser(), null);
		 
		 copy.setCreationDate(now);
		 copy.setCreatedBy(MODE.getUser().getId());
		 copy.setId(attachment.getId());
		 
		 assertEquals(copy,reload,10000);
		 
		 in = null;
		 InputStream in2 = null;
		 try {
			 in2 = attachmentBase.getAttachedFile(folderId, attachedId, moduleId, attachment.getId(), MODE.getContext(), MODE.getUser(), null);
			 AbstractAJAXTest.assertSameContent(in = new FileInputStream(testFile),in2);
		 } finally {
			 if(in != null)
				 in.close();
			 if(in2 != null)
				 in.close();
			 
		 }
	}
	
	public void doDetach(int folderId, int attachedId, int moduleId) throws Exception {
		doAttach(folderId,attachedId,moduleId);
		int id = clean.get(0).getId();

		for(AttachmentMetadata m : clean) {
			attachmentBase.detachFromObject(m.getFolderId(), m.getAttachedId(), m.getModuleId(), new int[]{m.getId()},MODE.getContext(),MODE.getUser(), null);
		}

		try {
			attachmentBase.getAttachment(folderId,attachedId,moduleId,id,MODE.getContext(), MODE.getUser(), null);
			fail("The attachment wasn't removed");
		} catch (Exception x) {
			assertTrue(true);
		}
	}

	public void doGetAttachments(int folderId, int attachedId, int moduleId) throws Exception {

		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		SearchIterator iterator = attachmentBase.getAttachments(folderId, attachedId, moduleId, MODE.getContext(), MODE.getUser(), null).results();
		
		Set<AttachmentMetadata> metadata = new HashSet<AttachmentMetadata>(clean);
		
		while(iterator.hasNext()) {
			AttachmentMetadata m = (AttachmentMetadata) iterator.next();
			assertTrue(metadata.remove(m));
		}
		
		assertTrue(metadata.toString(),metadata.isEmpty());
		
		iterator.close();
		
		iterator = attachmentBase.getAttachments(folderId, attachedId, moduleId, new AttachmentField[]{AttachmentField.ID_LITERAL}, AttachmentField.ID_LITERAL, AttachmentBase.ASC, MODE.getContext(), MODE.getUser(), null).results();
		
		List<Integer> ids = new ArrayList<Integer>();
		for(AttachmentMetadata m : clean) {
			ids.add(m.getId());
		}
		
		Collections.sort(ids);
		
		Iterator idIterator = ids.iterator();
		
		while(iterator.hasNext()) {
			assertTrue(idIterator.hasNext());
			assertEquals(idIterator.next(), ((AttachmentMetadata)iterator.next()).getId());
		}
		assertFalse(iterator.hasNext());
		
		iterator.close();
		
		int[] idsToFetch = new int[]{
				clean.get(0).getId(),
				clean.get(2).getId(),
				clean.get(4).getId()
		};
		
		iterator = attachmentBase.getAttachments(folderId, attachedId, moduleId, idsToFetch, new AttachmentField[]{AttachmentField.ID_LITERAL, AttachmentField.FILENAME_LITERAL}, MODE.getContext(), MODE.getUser(), null).results();
		
		int i = 0;
		for(; iterator.hasNext(); i++) {
			assertEquals(idsToFetch[i], ((AttachmentImpl) iterator.next()).getId());
		}
		assertEquals(idsToFetch.length, i);
	}
	
	public void doDelta(int folderId, int attachedId, int moduleId) throws Exception {
		doAttach(folderId, attachedId, moduleId);
		long ts = clean.get(0).getCreationDate().getTime();
		Thread.sleep(1000);
		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		doAttach(folderId, attachedId, moduleId);
		
		Delta delta = attachmentBase.getDelta(folderId, attachedId, moduleId, ts,  true, MODE.getContext(), MODE.getUser(), null);	
		
		Set<AttachmentMetadata> metadata = new HashSet<AttachmentMetadata>(clean.subList(1,clean.size()));
		
		SearchIterator iterator = delta.getNew();
		
		while(iterator.hasNext()) {
			AttachmentMetadata m = (AttachmentMetadata) iterator.next();
			assertTrue(metadata.remove(m));
		}
		assertTrue(metadata.isEmpty());
		
		iterator.close();
		delta.getDeleted().close();
		delta.getModified().close();
		
		delta = attachmentBase.getDelta(folderId, attachedId, moduleId, ts, true, new AttachmentField[]{AttachmentField.ID_LITERAL}, AttachmentField.ID_LITERAL, AttachmentBase.ASC, MODE.getContext(), MODE.getUser(), null);
		
		List<Integer> ids = new ArrayList<Integer>();
		for(AttachmentMetadata m : clean.subList(1,clean.size())) {
			ids.add(m.getId());
		}
		
		Collections.sort(ids);
		
		Iterator idIterator = ids.iterator();
		
		iterator = delta.getNew();
		
		while(iterator.hasNext()) {
			assertTrue(idIterator.hasNext());
			assertEquals(idIterator.next(), ((AttachmentMetadata)iterator.next()).getId());
		}
		assertFalse(iterator.hasNext());
		
		iterator.close();
		delta.getDeleted().close();
		delta.getModified().close();

		
		List<AttachmentMetadata> all = new ArrayList<AttachmentMetadata>(clean);
		
		for(AttachmentMetadata m : clean) {
			attachmentBase.detachFromObject(m.getFolderId(), m.getAttachedId(), m.getModuleId(), new int[]{m.getId()},MODE.getContext(),MODE.getUser(), null);
		}
		clean.clear();
		
		
		delta = attachmentBase.getDelta(folderId, attachedId, moduleId, ts, false , MODE.getContext(), MODE.getUser(), null);
		
		metadata = new HashSet<AttachmentMetadata>(all);
		
		iterator = delta.getDeleted();
		
		while(iterator.hasNext()) {
			AttachmentMetadata m = (AttachmentMetadata) iterator.next();
			assertTrue(metadata.remove(m));
		}
		assertTrue(metadata.toString(),metadata.isEmpty());

		iterator.close();
		delta.getNew().close();
		delta.getModified().close();
		
		
		delta = attachmentBase.getDelta(folderId, attachedId, moduleId, ts, false, new AttachmentField[]{AttachmentField.ID_LITERAL}, AttachmentField.ID_LITERAL, AttachmentBase.ASC, MODE.getContext(), MODE.getUser(), null);
		
		metadata = new HashSet<AttachmentMetadata>(all);
		
		iterator = delta.getDeleted();
		
		while(iterator.hasNext()) {
			AttachmentMetadata m = (AttachmentMetadata) iterator.next();
			assertTrue(metadata.remove(m));
		}
		assertTrue(metadata.isEmpty());
		
		iterator.close();
		delta.getNew().close();
		delta.getModified().close();

	}
	
	public void doNotify(int folderId, int attachedId, int moduleId) throws Exception{
		TestAttachmentListener listener = new TestAttachmentListener();
		attachmentBase.registerAttachmentListener(listener,moduleId);

		AttachmentMetadata attachment = getAttachment(testFile,folderId, attachedId,moduleId,false);
		
		
		InputStream in = null;
		
		try {
			attachmentBase.attachToObject(attachment,in = new FileInputStream(testFile),MODE.getContext(),MODE.getUser(), null);
			clean.add(attachment);
		} finally {
			 if(in != null)
				 in.close();
		}
		
		AttachmentEvent e = listener.getEvent();
		
		assertEquals(attachment, e.getAttachment());
		assertEquals(folderId, e.getFolderId());
		assertEquals(attachedId, e.getAttachedId());
		assertEquals(moduleId, e.getModuleId());
		
		in = null;
		InputStream in2 = null;
		try {
			 in2 = e.getAttachedFile();
			 AbstractAJAXTest.assertSameContent(in = new FileInputStream(testFile),in2);
		} finally {
			 if(in != null)
				 in.close();
			 if(in2 != null)
				 in.close();
		}
		 
		int id = clean.get(0).getId();
		 
		attachmentBase.detachFromObject(folderId,attachedId,moduleId,new int[]{id},MODE.getContext(), MODE.getUser(), null);
		 
		e = listener.getEvent();
		 
		assertEquals(folderId, e.getFolderId());
		assertEquals(attachedId, e.getAttachedId());
		assertEquals(moduleId, e.getModuleId());
		assertEquals(1, e.getDetached().length);
		assertEquals(id, e.getDetached()[0]);
		
		attachmentBase.removeAttachmentListener(listener,moduleId);
	}
	
	public void doCheckPermissions(int folderId, int attachedId, int moduleId) throws Exception {
		TestAttachmentAuthz authz = new TestAttachmentAuthz();
		
		attachmentBase.addAuthorization(authz,moduleId);
		try {
			try {
				AttachmentMetadata attachment = getAttachment(testFile, folderId,attachedId,moduleId,false);
				attachmentBase.attachToObject(attachment,null,MODE.getContext(),MODE.getUser(), null);
				clean.add(attachment);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayAttach();
			}
			
			try {
				attachmentBase.getAttachment(folderId,attachedId,moduleId,-1,MODE.getContext(),MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayRead();
			}
			
			try {
				attachmentBase.getAttachedFile(folderId,attachedId,moduleId,-1,MODE.getContext(),MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayRead();
			}
			
			try {
				attachmentBase.getAttachments(folderId, attachedId, moduleId, MODE.getContext(), MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayRead();
			}
			
			try {
				attachmentBase.getDelta(folderId, attachedId, moduleId, 0, false, MODE.getContext(), MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayRead();
			}
			
			try {
				attachmentBase.getAttachments(folderId, attachedId, moduleId, new AttachmentField[]{AttachmentField.ID_LITERAL}, AttachmentField.ID_LITERAL, AttachmentBase.ASC, MODE.getContext(), MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayRead();
			}
			
			try {
				attachmentBase.getDelta(folderId, attachedId, moduleId, 0, false, new AttachmentField[]{AttachmentField.ID_LITERAL}, AttachmentField.ID_LITERAL, AttachmentBase.ASC, MODE.getContext(), MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayRead();
			}
			
			try {
				attachmentBase.detachFromObject(folderId,attachedId,moduleId,new int[]{},MODE.getContext(), MODE.getUser(), null);
				fail("Disallow failed");
			} catch (OXPermissionException x) {
				authz.assertMayDetach();
			}
			
		} finally {
			attachmentBase.removeAuthorization(authz,moduleId);
		}
	}
	
	private AttachmentMetadata getAttachment(File file,int folderId, int attachedId, int moduleId, boolean rtfFlag) {
		AttachmentMetadata m = new AttachmentImpl();
		m.setFileMIMEType("text/plain");
		m.setFilesize(file.length());
		m.setFilename(file.getName());
		m.setAttachedId(attachedId);
		m.setModuleId(moduleId);
		m.setRtfFlag(rtfFlag);
		m.setFolderId(folderId);
		m.setId(AttachmentBase.NEW);
	
		return m;
	}
	
	public static final void assertEquals(AttachmentMetadata m1, AttachmentMetadata m2, int clockSkew) {
		if(m1 == null && m2 == null) {
			assertTrue(true);
		}
		
		if(m1 != null && m2 == null)
			fail(m1+" != "+m2);
		
		if(m1 == null && m2 != null)
			fail(m1+" != "+m2);

		
		GetSwitch get1  = new GetSwitch(m1);
		GetSwitch get2 = new GetSwitch(m2);
		
		for(AttachmentField field : AttachmentField.VALUES) {
			if(field == AttachmentField.FILE_ID_LITERAL)
				continue;
			Object v1 = field.doSwitch(get1);
			Object v2 = field.doSwitch(get2);
			
			if(v1 instanceof Date || v2 instanceof Date) {
				assertEquals((Date) v1, (Date) v2,clockSkew);
			} else {
				assertEquals(
					v1,
					v2
				);
			}
		}
	}
	
	public static final void assertEquals(Date d1, Date d2, int clockSkew) {
		long diff = d1.getTime() - d2.getTime();
		if(diff<0)
			diff = -diff;
		assertTrue(d1+" != "+d2+" diff is "+diff,diff <= clockSkew);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		
		testFile = new File(System.getProperty("test.propfile"));
		
		attachmentBase = new AttachmentBaseImpl(MODE.getProvider());
		attachmentBase.setTransactional(true);
		attachmentBase.startTransaction();
		clean.clear();
	}
	
	public void tearDown() throws Exception {
		for(AttachmentMetadata m : clean) {
			attachmentBase.detachFromObject(m.getFolderId(), m.getAttachedId(), m.getModuleId(), new int[]{m.getId()},MODE.getContext(),MODE.getUser(), null);
		}
		clean.clear();
		
		attachmentBase.commit();
		attachmentBase.finish();
	
		super.tearDown();
		
	}

	public Mode getMode(){
		return MODE;
	}
	
	public static interface Mode extends AbstractAttachmentTest.Mode {
		public DBProvider getProvider();

		public Context getContext();

		public User getUser();
	}
	
	public static class ISOLATION extends AbstractAttachmentTest.ISOLATION implements Mode {

		public DBProvider getProvider() {
			// TODO Auto-generated method stub
			return null;
		}


		public Context getContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public User getUser() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class INTEGRATION extends AbstractAttachmentTest.INTEGRATION implements Mode {

		public DBProvider getProvider() {
			return new DBPoolProvider();
		}


		public Context getContext()  {
            ContextStorage cs = ContextStorage.getInstance();
			try {
				return cs.getContext(cs.getContextId("defaultcontext"));
			} catch (ContextException e) {
				e.printStackTrace();
				return null;
			}
		}

		public User getUser() {
			try {
				UserStorage users = UserStorage.getInstance(getContext());
				int id = users.getUserId("francisco");
				return users.getUser(id);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public static class STATIC extends AbstractAttachmentTest.ISOLATION implements Mode {

		public DBProvider getProvider() {
			ConfigurableDBProvider provider = new ConfigurableDBProvider();
			try {
				provider.setDriver("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				System.err.println("Can't find MySQL Driver. Add mysql.jar to the classpath");
			}
			provider.setUrl("jdbc:mysql://localhost/openexchange");
			provider.setLogin("openexchange");
			provider.setPassword("secret");
			
			return provider;
		}

		public Context getContext() {
			return new ContextImpl(1);
		}

		public User getUser() {
			MockUser u = new MockUser();
			u.setId(23);
			return u;
		}

		public void setUp() throws Exception {
			
		}
	}
	
	private static final class TestAttachmentListener implements AttachmentListener {

		private AttachmentEvent e;

		public long attached(AttachmentEvent e) throws Exception {
			this.e = e;
			return System.currentTimeMillis();
		}

		public long detached(AttachmentEvent e) throws Exception {
			this.e = e;
			return System.currentTimeMillis();
		}
		
		public AttachmentEvent getEvent() {
			return e;
		}
		
	}
	
	private static final class TestAttachmentAuthz implements AttachmentAuthorization {
		
		private int checked = -1;
		
		public void checkMayAttach(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
			checked = 1;
			throw new OXPermissionException(Component.INFOSTORE, AbstractOXException.Category.USER_INPUT,0,"Badaaam!",null);
		}
		
		public void checkMayDetach(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
			checked = 2;
			throw new OXPermissionException(Component.INFOSTORE, AbstractOXException.Category.USER_INPUT,0,"Badaaam!",null);
		}
		
		public void checkMayReadAttachments(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
			checked = 3;
			throw new OXPermissionException(Component.INFOSTORE, AbstractOXException.Category.USER_INPUT,0,"Badaaam!",null);
		}
		
		public void assertMayAttach(){
			assertEquals(1,checked);
		}
		
		public void assertMayDetach(){
			assertEquals(2,checked);
		}
		
		public void assertMayRead(){
			assertEquals(3,checked);
		}
	}
}

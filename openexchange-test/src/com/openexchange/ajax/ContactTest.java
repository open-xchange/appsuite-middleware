package com.openexchange.ajax;

import com.meterware.httpunit.PutMethodWebRequest;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.ContactObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import org.json.JSONObject;

public class ContactTest extends CommonTest {
	
	private String url = "/ajax/contact";
	
	public void testInsertContact() throws Exception {
		ContactObject contactobject = new ContactObject();
		contactobject.setGivenName("Herbert");
		contactobject.setSurName("Meier");
		contactobject.setParentFolderID(-1);
		
		int object_id = insertContact(contactobject);
		
		compareContactObjects(contactobject, getContact(object_id, -1));
	}
	
	public void testUpdateContact() throws Exception {
		ContactObject contactobject = new ContactObject();
		contactobject.setGivenName("Herbert");
		contactobject.setSurName("Mustermeier");
		contactobject.setParentFolderID(-1);
		
		int object_id = insertContact(contactobject);
		
		contactobject.setGivenName("Hans");
		contactobject.setSurName("Meier");
		
		updateContact(contactobject);
		
		compareContactObjects(contactobject, getContact(object_id, -1));
	}
	
	public void testListContactsInFolderBetween() throws Exception {
		listContact(-1, 0, 10);
	}
	
	public void testDeleteContact() throws Exception {
		ContactObject contactobject = new ContactObject();
		int object_id = insertContact(contactobject);
		
		contactobject.setObjectID(object_id);
		
		deleteContact(contactobject);
	}
	
	protected int insertContact(ContactObject contactobject) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		ContactWriter contactwriter = new ContactWriter(pw);
		contactwriter.writeContact(contactobject);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();

		return insert(b);
	}
	
	protected void updateContact(ContactObject contactobject) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		
		ContactWriter contactwriter = new ContactWriter(pw);
		contactwriter.writeContact(contactobject);
		
		pw.flush();
		
		byte b[] = baos.toByteArray();
		
		update(b);
	}
	
	protected void deleteContact(ContactObject contactobject) throws Exception{
		delete(contactobject);
	}
	
	protected void listContact(int folder_id, int from, int to) throws Exception {
		list(folder_id, from, to);
	}
	
	protected ContactObject getContact(int object_id, int folder_id) throws Exception {
		WebResponse resp = getObject(object_id);
		JSONObject jsonobject = new JSONObject(resp.getText());
		
		ContactParser contactparser = new ContactParser(null);
		
		ContactObject contactobject = new ContactObject();
		contactparser.parse(contactobject, jsonobject);
		
		return contactobject;
	}
	
	protected void compareContactObjects(ContactObject c1, ContactObject c2) throws Exception {
		assertEquals("compare given name", c1.getGivenName(), c2.getGivenName());
		assertEquals("compare sur name", c1.getSurName(), c2.getSurName());
	}
	
	protected String getURL() {
		return url;
	}
}

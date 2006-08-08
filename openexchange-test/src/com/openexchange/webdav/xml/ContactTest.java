package com.openexchange.webdav.xml;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import java.util.Date;
import org.jdom.Element;

public class ContactTest extends AbstractWebdavTest {
	
	public static final String CONTACT_URL = "/servlet/webdav.contacts";
	
	protected int contactFolderId = -1;
	
	/*
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNewContact() throws Exception {
		ContactObject contactObj = createContactObject("testNewContact");
		saveContact(contactObj);
	}
	
	public void testNewContactWithDistributionList() throws Exception {
		int objectId = createContactWithDistributionList("testNewWithDistributionList");
	}
	
	public void testNewContactWithLinks() throws Exception {
		int objectId = createContactWithLinks("testNewWithLinks");
	}
	
	public void testUpdate() throws Exception {
		ContactObject contactObj = createContactObject("testUpdate");
		int objectId = saveContact(contactObj);
		
		contactObj.setObjectID(objectId);
		
		contactObj.setTelephoneBusiness1("+49009988776655");
		contactObj.setStateBusiness(null);
		
		saveContact(contactObj);
	}
	
	public void testUpdateWithDistributionList() throws Exception {
		int objectId = createContactWithDistributionList("testUpdateWithDistributionList");
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithDistributionList");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[2];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		
		contactObj.setDistributionList(entry);
		
		saveContact(contactObj);
	}
	
	public void testUpdateWithLinks() throws Exception {
		int objectId = createContactWithLinks("testUpdateWithLinks");
		
		ContactObject link1 = createContactObject("link1");
		int linkId1 = saveContact(link1);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("testUpdateWithLinks");
		contactObj.setParentFolderID(contactFolderId);
		contactObj.setObjectID(objectId);
		
		LinkEntryObject[] links = new LinkEntryObject[1];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		
		contactObj.setLinks(links);
		
		saveContact(contactObj);
	}
	
	public void testDelete() throws Exception {
		ContactObject contactObj = createContactObject("testDelete");
		int objectId = saveContact(contactObj);
		
		contactObj = new ContactObject();
		contactObj.setObjectID(objectId);
		deleteObject(contactObj, contactFolderId);
	}
	
	public void testPropFind() throws Exception {
		listObjects(contactFolderId, new Date(0), false);
	}

	public void testPropFindWithDelete() throws Exception {
		listObjects(contactFolderId, new Date(0), false);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		ContactObject contactObj = createContactObject("testPropFindWithObjectId");
		int objectId = saveContact(contactObj);
		
		loadObject(objectId);
	}
	
	protected int saveContact(ContactObject contactObj) throws Exception {
		ContactWriter contactWriter = new ContactWriter(sessionObj);
		Element e_prop = new Element("prop", webdav);
		contactWriter.addContent2PropElement(e_prop, contactObj, false);
		byte[] b = null; //  writeRequest(e_prop);
		return sendPut(b);
	}
	
	protected int createContactWithDistributionList(String title) throws Exception {
		ContactObject contactEntry = createContactObject(title);
		contactEntry.setEmail1("internalcontact@x.de");
		int contactId = saveContact(contactEntry);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactEntry.getDisplayName(), contactEntry.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[2].setEntryID(contactId);
		
		contactObj.setDistributionList(entry);
		return saveContact(contactObj);
	} 
	
	protected int createContactWithLinks(String title) throws Exception {
		ContactObject link1 = createContactObject("link1");
		ContactObject link2 = createContactObject("link2");
		int linkId1 = saveContact(link1);
		int linkId2 = saveContact(link2);
		
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName(title);
		contactObj.setParentFolderID(contactFolderId);
		
		LinkEntryObject[] links = new LinkEntryObject[2];
		links[0] = new LinkEntryObject();
		links[0].setLinkID(linkId1);
		links[0].setLinkDisplayname(link1.getDisplayName());
		links[1] = new LinkEntryObject();
		links[1].setLinkID(linkId2);
		links[1].setLinkDisplayname(link2.getDisplayName());
		
		contactObj.setLinks(links);
		
		return saveContact(contactObj);
	} 
	
	private ContactObject createContactObject(String title) throws Exception {
		ContactObject contactObj = new ContactObject();
		contactObj.setSurName("Meier");
		contactObj.setGivenName("Herbert");
		contactObj.setDisplayName(title);
		contactObj.setStreetBusiness("Franz-Meier Weg 17");
		contactObj.setCityBusiness("Test Stadt");
		contactObj.setStateBusiness("NRW");
		contactObj.setCountryBusiness("Deutschland");
		contactObj.setTelephoneBusiness1("+49112233445566");
		contactObj.setCompany("Internal Test AG");
		contactObj.setParentFolderID(contactFolderId);
		
		return contactObj;
	}
	 */
}


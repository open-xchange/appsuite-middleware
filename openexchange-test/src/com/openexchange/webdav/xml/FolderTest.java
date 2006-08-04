package com.openexchange.webdav.xml;

import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class FolderTest extends AbstractWebdavTest {
	
	public void testNewPrivateFolder() throws Exception {
		FolderObject folderObj = createFolderObject("testNewPrivateFolder", FolderObject.CALENDAR, false);
		saveFolder(folderObj, false);
	}
	
	public void testNewPublicFolder() throws Exception {
		FolderObject folderObj = createFolderObject("testNewPublicFolder", FolderObject.CALENDAR, true);
		saveFolder(folderObj, false);
	}
	
	public void testUpdatePrivateFolder() throws Exception {
		FolderObject folderObj = createFolderObject("testUpdatePrivateFolder", FolderObject.CALENDAR, false);
		int objectId = saveFolder(folderObj, false);
		
		OCLPermission[] permissions = new OCLPermission[3];
		permissions[0] = createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		permissions[1] = createPermission(groupParticipantId1, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		permissions[2] = createPermission(userParticipantId2, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		
		folderObj = new FolderObject();
		folderObj.setObjectID(objectId);
		folderObj.setPermissionsAsArray(permissions);
		
		saveFolder(folderObj, true);
	}
	
	public void testUpdatePublicFolder() throws Exception {
		FolderObject folderObj = createFolderObject("testUpdatePublicFolder", FolderObject.CALENDAR, false);
		int objectId = saveFolder(folderObj, false);
		
		OCLPermission[] permissions = new OCLPermission[3];
		permissions[0] = createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		permissions[1] = createPermission(groupParticipantId1, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		permissions[2] = createPermission(userParticipantId2, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		
		folderObj = new FolderObject();
		folderObj.setObjectID(objectId);
		folderObj.setPermissionsAsArray(permissions);
		
		saveFolder(folderObj, true);
	}
	
	public void testDelete() throws Exception {
		FolderObject folderObj = createFolderObject("testDeleteFolder", FolderObject.CONTACT, false);
		int objectId = saveFolder(folderObj, false);
		
		folderObj = new FolderObject();
		folderObj.setObjectID(objectId);
		deleteObject(folderObj, -1);
	}
	
	public void testPropFind() throws Exception {
		listFolders(new Date(0), false);
	}
	
	public void testPropFindWithDelete() throws Exception {
		listFolders(new Date(0), false);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		FolderObject folderObj = createFolderObject("testPropFindWithObjectId", FolderObject.TASK, false);
		int objectId = saveFolder(folderObj, false);
		
		loadObject(objectId);
	}
	
	protected int saveFolder(FolderObject folderObj, boolean isUpdate) throws Exception {
		InternalFolderWriter folderWriter = new InternalFolderWriter(sessionObj);
		Element e_prop = new Element("prop", webdav);
		folderWriter.addContent2PropElement(e_prop, folderObj, isUpdate);
		byte[] b = writeRequest(e_prop);
		return sendPut(b);
	}
	
	protected void listFolders(Date lastSync, boolean delete) throws Exception {
		Element e_propfind = new Element("propfind", webdav);
		Element e_prop = new Element("prop", webdav);
		
		Element e_lastSync = new Element("lastsync", XmlServlet.NS);
		Element e_objectmode = new Element("objectmode", XmlServlet.NS);
		
		e_lastSync.addContent(String.valueOf(lastSync.getTime()));
		
		e_propfind.addContent(e_prop);
		e_prop.addContent(e_lastSync);
		
		if (delete) {
			e_objectmode.addContent("NEW_AND_MODIFIED,DELETED");
			e_prop.addContent(e_objectmode);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(e_propfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		sendPropFind(baos.toByteArray());
	}
	
	private FolderObject createFolderObject(String title, int module, boolean isPublic) throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName(title + System.currentTimeMillis());
		folderObj.setModule(module);
		
		if (isPublic) {
			folderObj.setType(FolderObject.PUBLIC);
			folderObj.setParentFolderID(2);
		} else {
			folderObj.setType(FolderObject.PRIVATE);
			folderObj.setParentFolderID(1);
		}
		
		folderObj.setPermissionsAsArray(new OCLPermission[] { createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) } );
		
		return folderObj;
	}
	
	private OCLPermission createPermission(int entity, boolean isGroup, int fp, int orp, int owp, int odp) throws Exception {
		OCLPermission oclp = new OCLPermission();
		oclp.setEntity(entity);
		oclp.setGroupPermission(isGroup);
		oclp.setFolderAdmin(true);
		oclp.setFolderPermission(fp);
		oclp.setReadObjectPermission(orp);
		oclp.setWriteObjectPermission(owp);
		oclp.setDeleteObjectPermission(odp);
		
		return oclp;
	}
	
	protected String getURL() {
		return folderUrl;
	}
	
	private class InternalFolderWriter extends FolderWriter {
		
		public InternalFolderWriter(SessionObject sessionObj) {
			super(sessionObj);
		}
		
		public void addContent2PropElement(Element e_prop, FolderObject folderobject, boolean isUpdate) throws Exception {
			int type = folderobject.getType();
			int owner = folderobject.getCreator();
			int parentFolderId = folderobject.getParentFolderID();
			int module = folderobject.getModule();
			
			addElement("title", folderobject.getFolderName(), e_prop);

			if (folderobject.containsObjectID()) {
				addElement("object_id", folderobject.getObjectID(), e_prop);
			}
			
			if (folderobject.containsParentFolderID()) {
				addElement("folder_id", folderobject.getParentFolderID(), e_prop);
			}
			
			if (!isUpdate) {
				switch (module) {
					case FolderObject.CALENDAR:
						addElement("module", "calendar", e_prop);
						break;
					case FolderObject.CONTACT:
						addElement("module", "contact", e_prop);
						break;
					case FolderObject.TASK:
						addElement("module", "task", e_prop);
						break;
					default:
						throw new OXConflictException("invalid module");
				}
				
				if (type == FolderObject.PRIVATE) {
					addElement("type", PRIVATE_STRING, e_prop);
				} else {
					addElement("type", PUBLIC_STRING, e_prop);
				}
			}
			
			addElementPermission(folderobject.getPermissions(), e_prop);
		}
	}
}


/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.webdav.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.fields.FolderFields;
import com.openexchange.webdav.xml.folder.FolderTools;
import com.openexchange.webdav.xml.folder.actions.AbstractFolderRequest;
import com.openexchange.webdav.xml.framework.WebDAVClient;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;

public class FolderTest extends AbstractWebdavXMLTest {

    /**
     * @deprecated Use {@link AbstractFolderRequest#FOLDER_URL} instead
     */
    @Deprecated
    public static final String FOLDER_URL = AbstractFolderRequest.FOLDER_URL;

    protected int userParticipantId2 = -1;

    protected int userParticipantId3 = -1;

    protected int groupParticipantId1 = -1;

    protected String userParticipant2 = null;

    protected String userParticipant3 = null;

    protected String groupParticipant = null;

    public FolderTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
        userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");

        groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");
    }

    protected static Date decrementDate(final Date date) {
        return new Date(date.getTime() - 1);
    }

    protected void compareFolder(final FolderObject folderObj1, final FolderObject folderObj2) throws Exception {
        assertEqualsAndNotNull("id is not equals", folderObj1.getObjectID(), folderObj2.getObjectID());
        assertEqualsAndNotNull("folder name is not equals", folderObj1.getFolderName(), folderObj2.getFolderName());

        if (folderObj1.containsType()) {
            assertEqualsAndNotNull("type is not equals", folderObj1.getType(), folderObj2.getType());
        }

        if (folderObj1.containsModule()) {
            assertEqualsAndNotNull("module name is not equals", folderObj1.getModule(), folderObj2.getModule());
        }

        assertEqualsAndNotNull("parent folder id is not equals", folderObj1.getParentFolderID(), folderObj2.getParentFolderID());

        if (folderObj1.containsPermissions()) {
            assertEqualsAndNotNull("permissions are not equals" , permissions2String(folderObj1.getPermissionsAsArray()), permissions2String(folderObj2.getPermissionsAsArray()));
        }
    }

    public static FolderObject createFolderObject(int entity, String title, int module, boolean isPublic) {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName(title + System.currentTimeMillis());
        folderObj.setModule(module);
        if (isPublic) {
            folderObj.setType(FolderObject.PUBLIC);
            folderObj.setParentFolderID(2);
        } else {
            folderObj.setType(FolderObject.PRIVATE);
            folderObj.setParentFolderID(1);
        }
        folderObj.setPermissionsAsArray(new OCLPermission[] { createPermission( entity, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) } );
        return folderObj;
    }

    public static OCLPermission createPermission(int entity, boolean isGroup, int fp, int orp, int owp, int odp) {
        return createPermission(entity, isGroup, fp, orp, owp, odp, true);
    }

    public static OCLPermission createPermission(int entity, boolean isGroup, int fp, int orp, int owp, int odp, boolean isAdmin) {
        final OCLPermission oclp = new OCLPermission();
        oclp.setEntity(entity);
        oclp.setGroupPermission(isGroup);
        oclp.setFolderAdmin(isAdmin);
        oclp.setFolderPermission(fp);
        oclp.setReadObjectPermission(orp);
        oclp.setWriteObjectPermission(owp);
        oclp.setDeleteObjectPermission(odp);
        return oclp;
    }

    public static int insertFolder(final WebConversation webCon, FolderObject folderObj, String host, final String login, final String password, String context) throws Exception, OXException {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        int objectId = 0;

        folderObj.removeObjectID();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        DataWriter.addElement(FolderFields.TITLE, folderObj.getFolderName(), eProp);
        DataWriter.addElement(FolderFields.FOLDER_ID, folderObj.getParentFolderID(), eProp);
        addElementType(folderObj.getType(), eProp);
        addElementModule(folderObj.getModule(), eProp);
        FolderWriter.addElementPermission(folderObj.getPermissions(), eProp);

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractFolderRequest.FOLDER_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }

        if (response[0].getStatus() != 200) {
            throw new TestException(response[0].getErrorMessage());
        }

        folderObj = (FolderObject)response[0].getDataObject();
        objectId = folderObj.getObjectID();

        assertTrue("check objectId", objectId > 0);

        return objectId;
    }

    public static void updateFolder(final WebConversation webCon, final FolderObject folderObj, String host, final String login, final String password, String context) throws Exception, OXException {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        DataWriter.addElement(FolderFields.TITLE, folderObj.getFolderName(), eProp);
        DataWriter.addElement(FolderFields.OBJECT_ID, folderObj.getObjectID(), eProp);
        if (folderObj.containsParentFolderID()) {
            DataWriter.addElement("folder", folderObj.getParentFolderID(), eProp);
        }

        if (folderObj.containsPermissions()) {
            FolderWriter.addElementPermission(folderObj.getPermissions(), eProp);
        }

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractFolderRequest.FOLDER_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            fail("xml error: " + response[0].getErrorMessage());
        }

        if (response[0].getStatus() != 200) {
            throw new TestException(response[0].getErrorMessage());
        }
    }

    public static int[] deleteFolder(final WebConversation webCon, final int[] id, final String host, final String login, final String password, String context) throws Exception, OXException {
        return deleteFolder(webCon, id, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, login, password, context);
    }

    public static int[] deleteFolder(final WebConversation webCon, final int[] id, final Date lastModified, String host, final String login, final String password, String context) throws Exception, OXException {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element rootElement = new Element("multistatus", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int a = 0; a < id.length; a++) {
            final Element eProp = new Element("prop", webdav);
            DataWriter.addElement(FolderFields.OBJECT_ID, id[a], eProp);
            DataWriter.addElement(FolderFields.LAST_MODIFIED, lastModified, eProp);
            DataWriter.addElement("method", "DELETE", eProp);

            rootElement.addContent(addProp2PropertyUpdate(eProp));
        }

        final Document doc = new Document(rootElement);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractFolderRequest.FOLDER_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);

        assertEquals("check response", id.length, response.length);

        final List<Integer> idList = new ArrayList<Integer>();

        for (int a = 0; a < response.length; a++) {
            if (response[a].hasError()) {
                final FolderObject folderObj = (FolderObject)response[a].getDataObject();
                idList.add(Integer.valueOf(folderObj.getObjectID()));
            }

            if (response[0].getStatus() != 200) {
                throw new TestException(response[0].getErrorMessage());
            }
        }

        final int[] failed = new int[idList.size()];

        for (int a = 0; a < failed.length; a++) {
            failed[a] = idList.get(a).intValue();
        }

        return failed;
    }

    public static int[] clearFolder(final WebConversation webCon, final int[] id, final String[] modules, final Date lastModified, String host, final String login, final String password, String context) throws Exception, OXException {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element rootElement = new Element("multistatus", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int a = 0; a < id.length; a++) {
            final Element eProp = new Element("prop", webdav);
            DataWriter.addElement(FolderFields.OBJECT_ID, id[a], eProp);
            DataWriter.addElement(FolderFields.MODULE, modules[a], eProp);
            DataWriter.addElement(FolderFields.LAST_MODIFIED, lastModified, eProp);
            DataWriter.addElement("method", "CLEAR", eProp);

            rootElement.addContent(addProp2PropertyUpdate(eProp));
        }

        final Document doc = new Document(rootElement);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractFolderRequest.FOLDER_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);

        assertEquals("check response", id.length, response.length);

        final List<Integer> idList = new ArrayList<Integer>();

        for (int a = 0; a < response.length; a++) {
            if (response[a].hasError()) {
                final FolderObject folderObj = (FolderObject)response[a].getDataObject();
                idList.add(Integer.valueOf(folderObj.getObjectID()));
            }

            if (response[0].getStatus() != 200) {
                throw new TestException(response[0].getErrorMessage());
            }
        }

        final int[] failed = new int[idList.size()];

        for (int a = 0; a < failed.length; a++) {
            failed[a] = (idList.get(a)).intValue();
        }

        return failed;
    }

    public static int[] listFolder(final WebConversation webCon, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element ePropfind = new Element("propfind", webdav);
        final Element eProp = new Element("prop", webdav);

        final Element eObjectmode = new Element("objectmode", XmlServlet.NS);

        eObjectmode.addContent("LIST");

        eProp.addContent(eObjectmode);

        ePropfind.addContent(eProp);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(ePropfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, getCredentials(login, password, context));
        final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractFolderRequest.FOLDER_URL);
        propFindMethod.setDoAuthentication( true );

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.FOLDER, true);

        assertEquals("response length not is 1", 1, response.length);

        return (int[])response[0].getDataObject();
    }

    private static Credentials getCredentials(String login, String password,
			String context) {
		return new UsernamePasswordCredentials((context == null || context.equals("")) ? login : login+"@"+context, password);
	}

	public static FolderObject[] listFolder(final WebConversation webCon, final Date modified, final boolean changed, final boolean deleted, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        if (!changed && !deleted) {
            return new FolderObject[] { };
        }

        final Element ePropfind = new Element("propfind", webdav);
        final Element eProp = new Element("prop", webdav);

        final Element eLastSync = new Element("lastsync", XmlServlet.NS);
        final Element eObjectmode = new Element("objectmode", XmlServlet.NS);

        eLastSync.addContent(String.valueOf(modified.getTime()));

        final StringBuffer objectMode = new StringBuffer();

        if (changed) {
            objectMode.append("NEW_AND_MODIFIED,");
        }

        if (deleted) {
            objectMode.append("DELETED,");
        }

        objectMode.delete(objectMode.length()-1, objectMode.length());

        eObjectmode.addContent(objectMode.toString());
        eProp.addContent(eObjectmode);

        ePropfind.addContent(eProp);
        eProp.addContent(eLastSync);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(ePropfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, getCredentials(login, password, context));
        final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractFolderRequest.FOLDER_URL);
        propFindMethod.setDoAuthentication( true );

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.FOLDER);

        final FolderObject[] folderArray = new FolderObject[response.length];
        for (int a = 0; a < folderArray.length; a++) {
            if (response[a].hasError()) {
                fail("xml error: " + response[a].getErrorMessage());
            }

            folderArray[a] = (FolderObject)response[a].getDataObject();
        }

        return folderArray;
    }

    public static FolderObject loadFolder(final WebConversation webCon, final int objectId, String host, final String login, final String password, String context) throws Exception, OXException {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element ePropfind = new Element("propfind", webdav);
        final Element eProp = new Element("prop", webdav);

        final Element eObjectId = new Element(FolderFields.OBJECT_ID, XmlServlet.NS);
        eObjectId.addContent(String.valueOf(objectId));

        ePropfind.addContent(eProp);
        eProp.addContent(eObjectId);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(ePropfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, getCredentials(login, password, context));
        final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractFolderRequest.FOLDER_URL);
        propFindMethod.setDoAuthentication( true );

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.FOLDER);

        assertTrue("no response object found", response.length > 0);

        final FolderObject[] folderArray = new FolderObject[response.length];
        for (int a = 0; a < folderArray.length; a++) {
            if (response[a].hasError()) {
                throw new TestException(response[a].getErrorMessage());
            }

            folderArray[a] = (FolderObject)response[a].getDataObject();
        }

        assertEquals("id is not equals", objectId, folderArray[0].getObjectID());

        return folderArray[0];
    }

    public static FolderObject getAppointmentDefaultFolder(final WebConversation webCon, final String host, final String login, final String password, String context) throws Exception {
        final WebDAVClient client = new WebDAVClient(login+"@"+context, password);
        return new FolderTools(client).getDefaultAppointmentFolder(AbstractWebdavXMLTest.appendPrefix(host));
    }

    public static FolderObject getContactDefaultFolder(final WebConversation webCon, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final FolderObject[] folderArray = listFolder(webCon, new Date(0), true, false, host, login, password, context);

        final int userId = GroupUserTest.getUserId(webCon, host, login, password, context);
        assertTrue("user not found", userId != -1);

        for (int a = 0; a < folderArray.length; a++) {
            final FolderObject folderObj = folderArray[a];
            if (folderObj.isDefaultFolder() && folderObj.getModule() == FolderObject.CONTACT && folderObj.getCreatedBy() == userId) {
                return folderObj;
            }
        }

        throw OXException.general("no contact default folder found!");
    }

    public static FolderObject getTaskDefaultFolder(final WebConversation webCon, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final FolderObject[] folderArray = listFolder(webCon, new Date(0), true, false, host, login, password, context);

        final int userId = GroupUserTest.getUserId(webCon, host, login, password, context);
        assertTrue("user not found", userId != -1);

        for (int a = 0; a < folderArray.length; a++) {
            final FolderObject folderObj = folderArray[a];
            if (folderObj.isDefaultFolder() && folderObj.getModule() == FolderObject.TASK && folderObj.getCreatedBy() == userId) {
                return folderObj;
            }
        }

        throw OXException.general("no task default folder found!");
    }

    protected static void addElementType(final int type, final Element parent) throws Exception {
        if (type == FolderObject.PRIVATE) {
            DataWriter.addElement(FolderFields.TYPE, FolderWriter.PRIVATE_STRING, parent);
        } else {
            DataWriter.addElement(FolderFields.TYPE, FolderWriter.PUBLIC_STRING, parent);
        }
    }

    protected static void addElementModule(final int module, final Element parent) throws Exception {
        switch (module) {
            case FolderObject.CALENDAR:
                DataWriter.addElement(FolderFields.MODULE, "calendar", parent);
                break;
            case FolderObject.CONTACT:
                DataWriter.addElement(FolderFields.MODULE, "contact", parent);
                break;
            case FolderObject.TASK:
                DataWriter.addElement(FolderFields.MODULE, "task", parent);
                break;
            default:
                throw OXException.general("invalid module: " + module);
        }
    }

    private HashSet permissions2String(final OCLPermission[] oclp) throws Exception {
        if (oclp == null) {
            return null;
        }

        final HashSet hs = new HashSet();

        for (int a = 0; a < oclp.length; a++) {
            hs.add(permission2String(oclp[a]));
        }

        return hs;
    }

    private String permission2String(final OCLPermission oclp) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("ENTITY" + oclp.getEntity());
        sb.append("GROUP" + oclp.isGroupPermission());
        sb.append("ADMIN" + oclp.isFolderAdmin());
        sb.append("FP" + oclp.getFolderPermission());
        sb.append("ORP" + oclp.getReadPermission());
        sb.append("OWP" + oclp.getWritePermission());
        sb.append("ODP" + oclp.getDeletePermission());

        return sb.toString();
    }
}

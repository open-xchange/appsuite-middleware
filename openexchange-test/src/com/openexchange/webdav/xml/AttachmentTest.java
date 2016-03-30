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
import java.io.InputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.webdav.attachments;
import com.openexchange.webdav.xml.fields.DataFields;

public class AttachmentTest extends AbstractWebdavXMLTest {

    public static final String ATTACHMENT_URL = "/servlet/webdav.attachments";

    public static final String CONTENT_TYPE = "image/png";

    public static final byte[] data = { -119, 80, 78, 71, 13, 10, 26, 10, 0,
    0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0,
    37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1,
    -1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26,
    -40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0,
    9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126,
    -4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0,
    1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };

    public AttachmentTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public static int insertAttachment(final WebConversation webCon, final AttachmentMetadata attachmentObj, final InputStream is, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);
        final WebRequest webRequest = new PutMethodWebRequest(host + ATTACHMENT_URL, is, attachmentObj.getFileMIMEType());
        webRequest.setHeaderField("Authorization", "Basic " + getAuthData(login, password, context));
        webRequest.setHeaderField(attachments.FILENAME, attachmentObj.getFilename());
        webRequest.setHeaderField(attachments.MODULE, String.valueOf(attachmentObj.getModuleId()));
        webRequest.setHeaderField(attachments.TARGET_ID, String.valueOf(attachmentObj.getAttachedId()));
        webRequest.setHeaderField(attachments.TARGET_FOLDER_ID, String.valueOf(attachmentObj.getFolderId()));

        if (attachmentObj.getRtfFlag()) {
            webRequest.setHeaderField(attachments.RTF_FLAG, String.valueOf(attachmentObj.getRtfFlag()));
        }

        final WebResponse webResponse = webCon.getResponse(webRequest);
        assertEquals(207, webResponse.getResponseCode());

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(webResponse.getText().getBytes(com.openexchange.java.Charsets.UTF_8));

        final Document doc = new SAXBuilder().build(byteArrayInputStream);
        return parseResponse(doc, false);
    }

    public static InputStream loadAttachment(final WebConversation webCon, final AttachmentMetadata attachmentObj, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);
        final WebRequest webRequest = new GetMethodWebRequest(host + ATTACHMENT_URL);
        webRequest.setHeaderField("Authorization", "Basic " + getAuthData(login, password, context));
        webRequest.setHeaderField(attachments.MODULE, String.valueOf(attachmentObj.getModuleId()));
        webRequest.setHeaderField(attachments.TARGET_ID, String.valueOf(attachmentObj.getAttachedId()));
        webRequest.setHeaderField(attachments.TARGET_FOLDER_ID, String.valueOf(attachmentObj.getFolderId()));
        webRequest.setHeaderField(DataFields.OBJECT_ID, String.valueOf(attachmentObj.getId()));

        final WebResponse webResponse = webCon.getResponse(webRequest);

        assertEquals(200, webResponse.getResponseCode());

        return webResponse.getInputStream();
    }

    protected void deleteAttachment(final WebConversation webCon, final AttachmentMetadata attachmentObj, String host, final String login, final String password) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);
        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        final DeleteMethod deleteMethod = new DeleteMethod(host + ATTACHMENT_URL);
        deleteMethod.setDoAuthentication( true );
        deleteMethod.setRequestHeader(attachments.MODULE, String.valueOf(attachmentObj.getModuleId()));
        deleteMethod.setRequestHeader(attachments.TARGET_ID, String.valueOf(attachmentObj.getAttachedId()));
        deleteMethod.setRequestHeader(DataFields.OBJECT_ID, String.valueOf(attachmentObj.getId()));
        deleteMethod.setRequestHeader(attachments.TARGET_FOLDER_ID, String.valueOf(attachmentObj.getFolderId()));

        httpclient.executeMethod(deleteMethod);

        assertEquals(deleteMethod.getResponseBodyAsString(), 200, deleteMethod.getStatusCode());
    }

    public void compareAttachments(final AttachmentMetadata attachmentObj1, final AttachmentMetadata attachmentObj2) throws Exception {
        assertEquals("filename is not equals", attachmentObj1.getFilename(), attachmentObj2.getFilename());
        assertEquals("module is not equals", attachmentObj1.getModuleId(), attachmentObj2.getModuleId());
        assertEquals("target id is not equals", attachmentObj1.getAttachedId(), attachmentObj2.getAttachedId());
        assertEquals("target folder id is not equals", attachmentObj1.getFolderId(), attachmentObj2.getFolderId());
        assertEquals("rtf flag is not equals", attachmentObj1.getRtfFlag(), attachmentObj2.getRtfFlag());
    }
}

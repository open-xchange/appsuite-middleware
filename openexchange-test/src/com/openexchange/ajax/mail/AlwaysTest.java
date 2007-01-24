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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.mail;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.MailTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;

/**
 * @author marcus
 *
 */
public class AlwaysTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AlwaysTest.class);

    /**
     * Random number generator.
     */
    private static final Random rand = new Random(System.currentTimeMillis());

    /**
     * This attributes of mails are requested when a mail folder is listed.
     */
    private static final int[] listAttributes = new int[] {
        JSONMessageObject.FIELD_ID, JSONMessageObject.FIELD_FOLDER_ID,
        JSONMessageObject.FIELD_THREAD_LEVEL, JSONMessageObject.FIELD_ATTACHMENT,
        JSONMessageObject.FIELD_FROM, JSONMessageObject.FIELD_SUBJECT,
        JSONMessageObject.FIELD_RECEIVED_DATE, JSONMessageObject.FIELD_SIZE,
        JSONMessageObject.FIELD_FLAGS, JSONMessageObject.FIELD_PRIORITY,
        CommonObject.COLOR_LABEL
    };
    
    /**
     * 
     * @param name
     */
    public AlwaysTest(final String name) {
        super(name);
    }

    public void testFolderListing() throws Throwable {
        final FolderObject imapRoot = getIMAPRootFolder();
        final List<FolderObject> list = FolderTest.getSubfolders(
            getWebConversation(), getHostName(), getSessionId(),
            imapRoot.getFullName(), false);
        for (FolderObject fo : list) {
            recListFolder(fo);
        }
    }

    public void recListFolder(final FolderObject folder) throws IOException,
        SAXException, JSONException, OXException {
        LOG.trace("Listing " + folder.getFullName());
        final JSONObject json = MailTest.getAllMails(getWebConversation(),
            getHostName(), getSessionId(), folder.getFullName(), listAttributes,
            false);
        final Response response = Response.parse(json.toString());
        assertFalse(response.getErrorMessage(), response.hasError());
        final JSONArray array = (JSONArray) response.getData();
        final int count = Math.min(100, array.length());
        for (int i = 0; i < count; i++) {
            final JSONArray mailInfo = array
                .getJSONArray(rand.nextInt(array.length()));
            final String mailId = mailInfo.getString(0);
            LOG.trace("Getting mail: " + mailId);
            final Response mailResponse = MailTest.getMail(getWebConversation(),
                getHostName(), getSessionId(), mailId);
            assertFalse(mailResponse.getErrorMessage(), mailResponse.hasError());
        }
        final List<FolderObject> list = FolderTest.getSubfolders(
            getWebConversation(), getHostName(), getSessionId(),
            folder.getFullName(), false);
        for (FolderObject sub : list) {
            recListFolder(sub);
        }
    }
    
    public FolderObject getIMAPRootFolder() throws OXException, IOException,
        SAXException, JSONException {
        final List<FolderObject> list = FolderTest.getSubfolders(
            getWebConversation(), getHostName(), getSessionId(),
            String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), false);
        FolderObject defaultIMAPFolder = null;
        for (FolderObject fo : list) {
            if (fo.containsFullName() && fo.getFullName().equals(
                MailFolderObject.DEFAULT_IMAP_FOLDER)) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertTrue("Can't find IMAP root folder.", defaultIMAPFolder != null
            && defaultIMAPFolder.hasSubfolders());
        return defaultIMAPFolder;
    }

}

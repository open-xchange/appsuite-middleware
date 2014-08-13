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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.share;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.junit.Assert;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareRequest;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.TimeZones;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link GuestClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GuestClient extends AJAXClient {

    private final ResolveShareResponse shareResponse;

    /**
     * Initializes a new {@link GuestClient}, trying to login via resolving the supplied share automatically.
     *
     * @param share The share to access as guest
     * @throws Exception
     */
    public GuestClient(ParsedShare share) throws Exception {
        super(new AJAXSession(), true);
        getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        this.shareResponse = resolve(share);
    }

    public String getUser() {
        return shareResponse.getUser();
    }

    public int getUserId() {
        return shareResponse.getUserId();
    }

    public String getLanguage() {
        return shareResponse.getLanguage();
    }

    public boolean isStore() {
        return shareResponse.isStore();
    }

    public String getModule() {
        return shareResponse.getModule();
    }

    public String getFolder() {
        return shareResponse.getFolder();
    }

    public int getIntFolder() {
        return Integer.parseInt(getFolder());
    }

    public String getItem() {
        return shareResponse.getItem();
    }

    /**
     * Checks that a share is accessible for the guest according to the granted permissions.
     *
     * @param permissions The guest permissions
     * @throws Exception
     */
    public void checkShareAccessible(OCLGuestPermission permissions) throws Exception {
        if (permissions.canReadOwnObjects()) {
            /*
             * verify "all" request in module
             */
            AbstractColumnsResponse response = performAll();
            Assert.assertFalse("Errors in response", response.hasError());
        }
        if (permissions.canCreateObjects()) {
            /*
             * verify item creation, retrieval & deletion
             */
            String id = createItem();
            Assert.assertNotNull("No ID for created item", id);
            Object item = getItem(id);
            Assert.assertNotNull("No created item found", item);
            deleteItem(id);
        }
    }

    private void deleteItem(String id) throws Exception {
        if ("io.ox/contacts".equals(getModule())) {
            com.openexchange.ajax.contact.action.DeleteRequest deleteRequest = new com.openexchange.ajax.contact.action.DeleteRequest(
                getIntFolder(), Integer.parseInt(id), getFutureTimestamp());
            CommonDeleteResponse deleteResponse = execute(deleteRequest);
            Assert.assertFalse("Errors in response", deleteResponse.hasError());
            return;
        }
        if ("io.ox/files".equals(getModule())) {
            DeleteInfostoreRequest deleteRequest = new DeleteInfostoreRequest(Integer.parseInt(id), getIntFolder(), getFutureTimestamp());
            DeleteInfostoreResponse deleteResponse = execute(deleteRequest);
            Assert.assertFalse("Errors in response", deleteResponse.hasError());
            return;
        }
        Assert.fail("no delete item request for " + getModule() + " implemented");
    }

    private Object getItem(String id) throws Exception {
        if ("io.ox/contacts".equals(getModule())) {
            Contact contact = new Contact();
            contact.setParentFolderID(getIntFolder());
            contact.setDisplayName(UUIDs.getUnformattedString(UUID.randomUUID()));
            com.openexchange.ajax.contact.action.GetRequest getRequest = new com.openexchange.ajax.contact.action.GetRequest(
                getIntFolder(), Integer.parseInt(id), TimeZones.UTC);
            GetResponse getResponse = execute(getRequest);
            Assert.assertFalse("Errors in response", getResponse.hasError());
            return getResponse.getContact();
        }
        if ("io.ox/files".equals(getModule())) {
            GetInfostoreRequest getRequest = new GetInfostoreRequest(Integer.parseInt(id));
            GetInfostoreResponse getResponse = execute(getRequest);
            Assert.assertFalse("Errors in response", getResponse.hasError());
            return getResponse.getDocumentMetadata();
        }
        Assert.fail("no get item request for " + getModule() + " implemented");
        return null;
    }

    private String createItem() throws Exception {
        if ("io.ox/contacts".equals(getModule())) {
            Contact contact = new Contact();
            contact.setParentFolderID(getIntFolder());
            contact.setDisplayName(UUIDs.getUnformattedString(UUID.randomUUID()));
            com.openexchange.ajax.contact.action.InsertRequest insertRequest = new com.openexchange.ajax.contact.action.InsertRequest(contact);
            InsertResponse insertResponse = execute(insertRequest);
            Assert.assertFalse("Errors in response", insertResponse.hasError());
            insertResponse.fillObject(contact);
            return String.valueOf(contact.getObjectID());
        }
        if ("io.ox/files".equals(getModule())) {
            byte[] data = UUIDs.toByteArray(UUID.randomUUID());
            DocumentMetadataImpl metadata = new DocumentMetadataImpl();
            metadata.setFolderId(getIntFolder());
            metadata.setFileName(UUIDs.getUnformattedString(UUID.randomUUID()) + ".test");
            NewInfostoreRequest newRequest = new NewInfostoreRequest(metadata, new ByteArrayInputStream(data));
            NewInfostoreResponse newResponse = execute(newRequest);
            Assert.assertFalse("Errors in response", newResponse.hasError());
            return String.valueOf(newResponse.getID());
        }
        Assert.fail("no create item request for " + getModule() + " implemented");
        return null;
    }

    private AbstractColumnsResponse performAll() throws OXException, IOException, JSONException {
        if ("io.ox/contacts".equals(getModule())) {
            com.openexchange.ajax.contact.action.AllRequest allRequest = new com.openexchange.ajax.contact.action.AllRequest(
                getIntFolder(), Contact.ALL_COLUMNS);
            return execute(allRequest);
        }
        if ("io.ox/files".equals(getModule())) {
            int[] columns = new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL, Metadata.FOLDER_ID };
            AllInfostoreRequest allRequest = new AllInfostoreRequest(getFolder(), columns, Metadata.ID, Order.ASCENDING);
            return execute(allRequest);
        }
        Assert.fail("no all request for " + getModule() + " implemented");
        return null;
    }

    /**
     * Resolves the supplied share, i.e. accesses the share link and authenticates using the share's credentials.
     *
     * @param share The share
     * @return The share response
     */
    private ResolveShareResponse resolve(ParsedShare share) throws ClientProtocolException, IOException, OXException, JSONException {
        if (AuthenticationMode.ANONYMOUS == share.getAuthentication()) {
            setCredentials(null);
        } else {
            setCredentials(share.getGuestMailAddress(), share.getGuestPassword());
        }
        ResolveShareResponse response = Executor.execute(this, new ResolveShareRequest(share));
        getSession().setId(response.getSessionID());
        return response;
    }

    private DefaultHttpClient getHttpClient() {
        return getSession().getHttpClient();
    }

    private void setCredentials(org.apache.http.auth.Credentials credentials) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (null != credentials) {
            credentialsProvider.setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
        }
        getHttpClient().setCredentialsProvider(credentialsProvider);
    }

    private void setCredentials(String username, String password) {
        setCredentials(new UsernamePasswordCredentials(username, password));
    }

    private static Date getFutureTimestamp() {
        return new Date(System.currentTimeMillis() + 1000000);
    }

}

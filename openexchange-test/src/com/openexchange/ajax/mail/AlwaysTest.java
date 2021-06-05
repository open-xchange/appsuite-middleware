/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * @author marcus
 */
public class AlwaysTest extends AbstractAJAXSession {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlwaysTest.class);

    private AJAXClient client;

    public AlwaysTest() {
        super();
    }

    @Test
    public void testFolderListing() throws Throwable {
        final FolderObject imapRoot = getIMAPRootFolder();
        recListFolder(imapRoot.getFullName(), "");
    }

    public void recListFolder(final String folderId, final String rights) throws Exception {
        LOG.trace("Listing {}", folderId);
        if (rights.length() > 0) {
            listMails(folderId);
        }
        final Map<String, String> subRights = getIMAPRights(client, folderId);
        for (final Entry<String, String> entry : subRights.entrySet()) {
            recListFolder(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @param folder
     * @param max
     * @throws Exception
     */
    private void listMails(final String folderId) throws Exception {
        MailMessage[] mails = mtm.listMails(folderId, MailListField.getAllFields(), MailListField.ID.getField(), Order.DESCENDING, true, Collections.EMPTY_LIST);
        assertFalse(mtm.getLastResponse().hasError());
        for (MailMessage mail : mails) {
            TestMail mailObj = mtm.get(mail.getFolder(), mail.getMailId());
            assertNotNull(mailObj);
            assertFalse(mtm.getLastResponse().hasError());
        }
    }

    public static Map<String, String> getIMAPRights(final AJAXClient client, final String parent) throws IOException, JSONException, OXException {
        final ListResponse listR = client.execute(new ListRequest(EnumAPI.OX_OLD, parent, new int[] { FolderObject.OBJECT_ID, FolderObject.OWN_RIGHTS }, false));
        final Map<String, String> retval = new HashMap<String, String>();
        for (final Object[] row : listR) {
            retval.put(row[0].toString(), row[1].toString());
        }
        return retval;
    }

    public FolderObject getIMAPRootFolder() throws OXException, IOException, JSONException {
        final ListResponse listR = getClient().execute(new ListRequest(EnumAPI.OX_OLD, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID)));
        FolderObject defaultIMAPFolder = null;
        final Iterator<FolderObject> iter = listR.getFolder();
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && fo.getFullName().equals(MailFolder.ROOT_FOLDER_ID)) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertTrue("Can't find IMAP root folder.", defaultIMAPFolder != null && defaultIMAPFolder.hasSubfolders());
        return defaultIMAPFolder;
    }
}

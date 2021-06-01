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

package com.openexchange.carddav.resources;

import static com.openexchange.carddav.Tools.getFoldersHash;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.SyncToken;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.Contact;
import com.openexchange.login.Interface;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link AggregatedCollection} - CardDAV collection aggregating the contents
 * of all visible folders.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AggregatedCollection extends CardDAVCollection {

    private static final String AGGREGATED_DISPLAY_NAME = "All Contacts";

    private final List<UserizedFolder> folders;

    /**
     * Initializes a new {@link AggregatedCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folders The folders aggregated in this collection
     */
    public AggregatedCollection(GroupwareCarddavFactory factory, WebdavPath url, List<UserizedFolder> folders) throws OXException {
        super(factory, url, factory.getState().getDefaultFolder());
        this.folders = folders;
    }

    @Override
    protected List<UserizedFolder> getFolders() {
        return folders;
    }

    @Override
    public String getDisplayName() {
        return AGGREGATED_DISPLAY_NAME;
    }

    @Override
    public String getPushTopic() {
        return "ox:" + Interface.CARDDAV.toString().toLowerCase() + ":contacts" ;
    }

    @Override
    public String getSyncToken() throws WebdavProtocolException {
        Date lastModified = getLastModified();
        if (null == lastModified) {
            return "0";
        }
        String foldersHash = getFoldersHash(getFolders());
        return new SyncToken(lastModified.getTime(), foldersHash, 0).toString();
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(SyncToken syncToken) throws OXException {
        /*
         * re-check hash of aggregated folders to detect changes
         */
        String foldersHash = getFoldersHash(getFolders());
        if (0L < syncToken.getTimestamp() && false == Objects.equals(syncToken.getAdditional(), foldersHash)) {
            String msg = "Mismatching folders hash of aggregated collection (client token: " + syncToken.getAdditional() + ", current: " + foldersHash + ")";
            OXException cause = OXException.general(msg).setCategory(Category.CATEGORY_CONFLICT);
            throw new PreconditionException(cause, DAVProtocol.DAV_NS.getURI(), "valid-sync-token", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        /*
         * get sync status & enrich token with hash for aggregated folders
         */
        SyncStatus<WebdavResource> syncStatus = super.getSyncStatus(syncToken);
        SyncToken nextSyncToken = SyncToken.parse(syncStatus.getToken());
        syncStatus.setToken(new SyncToken(nextSyncToken.getTimestamp(), foldersHash, nextSyncToken.getFlags()).toString());
        return syncStatus;
    }

    @Override
    protected Collection<Contact> getDeletedObjects(Date since) throws OXException {
        Collection<Contact> contacts = super.getDeletedObjects(since);
        for (UserizedFolder folder : factory.getState().getDeletedFolders(since)) {
            contacts.addAll(getDeletedContacts(since, folder.getID()));
        }
        return contacts;
    }

}

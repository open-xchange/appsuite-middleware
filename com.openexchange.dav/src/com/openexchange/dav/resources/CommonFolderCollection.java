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

package com.openexchange.dav.resources;

import java.util.Collection;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.internal.Tools;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;

/**
 * {@link CommonFolderCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class CommonFolderCollection<T extends CommonObject> extends FolderCollection<T> {

    /**
     * Initializes a new {@link CommonFolderCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     */
    protected CommonFolderCollection(DAVFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        super(factory, url, folder);
    }

    @Override
    protected WebdavPath constructPathForChildResource(T object) {
        String fileName = object.getFilename();
        if (null == fileName || 0 == fileName.length()) {
            fileName = object.getUid();
        }
        String fileExtension = getFileExtension().toLowerCase();
        if (false == fileExtension.startsWith(".")) {
            fileExtension = "." + fileExtension;
        }
        return constructPathForChildResource(fileName + fileExtension);
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(Date since) throws OXException {
		SyncStatus<WebdavResource> multistatus = new SyncStatus<WebdavResource>();
//		Date nextSyncToken = new Date(since.getTime());
        boolean initialSync = 0 == since.getTime();
		Date nextSyncToken = Tools.getLatestModified(since, this.folder);
		/*
		 * new and modified objects
		 */
		Collection<T> modifiedObjects = this.getModifiedObjects(since);
		for (T object : modifiedObjects) {
			// add resource to multistatus
			WebdavResource resource = createResource(object, constructPathForChildResource(object));
			int status = null != object.getCreationDate() && object.getCreationDate().after(since) ?
			    HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
			multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
			// remember aggregated last modified for next sync token
			nextSyncToken = Tools.getLatestModified(nextSyncToken, object);
		}
		/*
		 * deleted objects
		 */
	    Collection<T> deletedObjects = this.getDeletedObjects(since);
		for (T object : deletedObjects) {
			// only include objects that are not also modified (due to move operations)
			if (null != object.getUid() && false == contains(modifiedObjects, object.getUid())) {
		        if (false == initialSync) {
	                // add resource to multistatus
    				WebdavResource resource = createResource(object, constructPathForChildResource(object));
    				multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), resource));
		        }
				// remember aggregated last modified for parent folder
				nextSyncToken = Tools.getLatestModified(nextSyncToken, object);
			}
		}
		/*
		 * Return response with new next sync-token in response
		 */
		multistatus.setToken(Long.toString(nextSyncToken.getTime()));
		return multistatus;
	}

    /**
     * Gets all objects that have been created or modified since the supplied time.
     *
     * @param since The (exclusive) minimum modification time to consider
     * @return The objects
     */
    protected abstract Collection<T> getModifiedObjects(Date since) throws OXException;

    /**
     * Gets all objects that have been deleted since the supplied time.
     *
     * @param since The (exclusive) minimum modification time to consider
     * @return The objects
     */
    protected abstract Collection<T> getDeletedObjects(Date since) throws OXException;

	private static <T extends CommonObject> boolean contains(Collection<T> objects, String uid) {
		for (T object : objects) {
			if (uid.equals(object.getUid())) {
				return true;
			}
		}
		return false;
	}

}

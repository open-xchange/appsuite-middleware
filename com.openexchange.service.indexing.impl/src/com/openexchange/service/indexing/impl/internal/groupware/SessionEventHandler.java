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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.indexing.impl.internal.groupware;

import java.io.File;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.infostore.InfostoreFolderJob;
import com.openexchange.service.indexing.impl.infostore.InfostoreJobInfo;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;


/**
 * {@link SessionEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SessionEventHandler implements EventHandler {

    // TODO: move to infostore/server bundle and check if indexing is allowed
    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic) || SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic)) {            
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            IndexAccess<File> infostoreAccess = null;
            try {
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                infostoreAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, session);
                FolderService folderService = Services.getService(FolderService.class);                
                FolderResponse<UserizedFolder[]> folders = folderService.getSubfolders(
                    FolderStorage.REAL_TREE_ID,
                    String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID),
                    true,
                    session,
                    null);
                
                if (folders == null || folders.getResponse().length == 0) {
                    return;
                }
                
                IndexingService indexingService = Services.getService(IndexingService.class);
                for (UserizedFolder folder : folders.getResponse()) {
                    String id = folder.getID();
                    long folderId = Long.parseLong(folder.getID());
                    
                    if (!infostoreAccess.isIndexed(IndexConstants.DEFAULT_ACCOUNT, id)) {
                        JobInfo jobInfo = InfostoreJobInfo.newBuilder(InfostoreFolderJob.class)
                            .contextId(session.getContextId())
                            .userId(session.getUserId())
                            .account(IndexConstants.DEFAULT_ACCOUNT)
                            .folder(folderId)
                            .build();
                        
                        indexingService.scheduleJob(jobInfo, null, -1L, IndexingService.DEFAULT_PRIORITY);
                    }
                }                
            } catch (Exception e) {
                // TODO: handle exception
            } finally {
                if (infostoreAccess != null) {
                    try {
                        indexFacade.releaseIndexAccess(infostoreAccess);
                    } catch (OXException e) {
                        // ignore
                    }
                }
            }
        }        
    }

}

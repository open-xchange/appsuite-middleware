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

import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.index.IndexConstants;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.infostore.InfostoreFolderJob;
import com.openexchange.service.indexing.impl.infostore.InfostoreJobInfo;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link FolderEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderEventHandler implements EventHandler {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(FolderEventHandler.class);
    
    // TODO: this handles only private infostore folders atm
    @Override
    public void handleEvent(Event event) {
        try {
            String topic = event.getTopic();
            if ("com/openexchange/groupware/folder/update".equals(topic)) {
                // TODO: delete and reindex
            } else if ("com/openexchange/groupware/folder/delete".equals(topic)) {
                CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
                FolderObject folder = (FolderObject) commonEvent.getActionObj();
                int module = folder.getModule();
                int userId = commonEvent.getUserId();
                int contextId = commonEvent.getContextId();
                if (module == FolderObject.INFOSTORE) {
                    ContextService contextService = Services.getService(ContextService.class);
                    UserService userService = Services.getService(UserService.class);
                    UserConfigurationService configurationService = Services.getService(UserConfigurationService.class);
                    IndexingService indexingService = Services.getService(IndexingService.class);
                    OCLPermission[] oclPermissions = folder.getNonSystemPermissionsAsArray();
                    if (oclPermissions.length > 1) {
                        /*
                         * This folder is shared in any way
                         */
                        return;
                    }
                    
                    Context context = contextService.getContext(contextId);
                    User user = userService.getUser(userId, context);
                    UserConfiguration userConfiguration = configurationService.getUserConfiguration(userId, context);
                    EffectivePermission userPermission = folder.getEffectiveUserPermission(user.getId(), userConfiguration);
                    if (userPermission.getEntity() == user.getId() && userPermission.canReadAllObjects()) {
                        // The folder is a private folder of this user
                        long folderId = (long) folder.getObjectID();                    
                        JobInfo jobInfo = InfostoreJobInfo.newBuilder(InfostoreFolderJob.class)
                            .contextId(contextId)
                            .userId(userId)
                            .account(IndexConstants.DEFAULT_ACCOUNT)
                            .folder(folderId)
                            .delete()
                            .build();
                        
                        indexingService.scheduleJob(jobInfo, IndexingService.NOW, IndexingService.NO_INTERVAL, IndexingService.DEFAULT_PRIORITY);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not handle folder event.", e);
        }
    }

}

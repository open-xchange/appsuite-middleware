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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.contact.aggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.aggregator.ContactSource.Speed;
import com.openexchange.contact.aggregator.ContactSource.Type;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ContactFolderContactSourceFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactFolderContactSourceFactory implements ContactSourceFactory {
    
    private ConfigViewFactory configs;
    
    public ContactFolderContactSourceFactory(ConfigViewFactory configs) {
        this.configs = configs;
    }

    public ContactFolderContactSourceFactory() {
        super();
    }

    public List<ContactSource> getSources(ServerSession session) throws Exception {
        final User user = session.getUser();
        final UserConfiguration userConfig = session.getUserConfiguration();
        
        ConfigView view = configs.getView(session.getUserId(), session.getContextId());
        
        Set<String> excluded = new HashSet<String>(Arrays.asList(view.opt("com.openexchange.contact.aggregator.folderBlacklist", String.class, "").split("\\s*,\\s*")));

        final Queue<FolderObject> queue = ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
            session.getUserId(),
            user.getGroups(),
            userConfig.getAccessibleModules(),
            FolderObject.CONTACT,
            session.getContext())).asQueue();
        
        List<ContactSource> sources = new ArrayList<ContactSource>(queue.size());
        final ServerUserSetting serverUserSetting = ServerUserSetting.getInstance();

        final Integer ccollectorFolderId = serverUserSetting.getContactCollectionFolder(session.getContextId(), session.getUserId());
        
        for (FolderObject folder : queue) {
            if (excluded.contains(String.valueOf(folder.getObjectID()))) {
                continue;
            }
            EffectivePermission permission = folder.getEffectiveUserPermission(user.getId(), userConfig);
            
            if ((permission.canReadAllObjects() || permission.canReadOwnObjects())) {
                if (ccollectorFolderId == null || folder.getObjectID() != ccollectorFolderId) {
                    Type type = Type.CONFIRMED;
                    if (folder.getType() == FolderObject.PRIVATE) {
                        type = Type.IMPORTANT;
                    }
                    ContactFolderContactSource source = new ContactFolderContactSource(folder, new RdbContactSQLImpl(session), type);
                    if (folder.isDefaultFolder()) {
                        sources.add(0, source);
                    } else {
                        sources.add(source);
                        
                    }
                } else {
                    sources.add(new ContactFolderContactSource(folder, new RdbContactSQLImpl(session), Type.CONTRIBUTOR));
                }
            }
        }
        
        return sources;
    }

    public Speed getSpeed() {
        return ContactSource.Speed.FAST;
    }



}

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

package com.openexchange.contact.aggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailFolderDiscoverer}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailFolderDiscoverer {

    private static final Log LOG = LogFactory.getLog(MailFolderDiscoverer.class);
    
    private final FolderService folderService;

    public MailFolderDiscoverer(FolderService folderService) {
        super();
        this.folderService = folderService;
    }

    public List<String> getMailFolder(ServerSession session) throws Exception {
        ContentType mailType = getMailType();
        if (mailType == null) {
            throw new IllegalStateException("Could not determine contentType for Mail module");
        }
        FolderServiceDecorator decorator = new FolderServiceDecorator().setTimeZone(TimeZone.getTimeZone("UTC")).setAllowedContentTypes(
            Arrays.asList(mailType)).put("mailRootFolders", "true");
        final FolderResponse<UserizedFolder[]> privateResp = folderService.getVisibleFolders(
            FolderStorage.REAL_TREE_ID,
            mailType,
            PrivateType.getInstance(),
            true,
            session,
            decorator);

        UserizedFolder[] response = privateResp.getResponse();
        List<String> folders = new ArrayList<String>();
        for (UserizedFolder userizedFolder : response) {
            if (!isVirtualMailFolder(userizedFolder.getID())) {
                folders.add(userizedFolder.getID());
            }
            if (userizedFolder.isSubscribed()) {
                recurse(userizedFolder, folders, session, decorator);
            }
        }
        return new ArrayList<String>(new HashSet<String>(folders));
    }

    private void recurse(UserizedFolder userizedFolder, List<String> folders, Session session, FolderServiceDecorator decorator) throws Exception {
        try {
            FolderResponse<UserizedFolder[]> subfolders = folderService.getSubfolders(
                FolderStorage.REAL_TREE_ID,
                userizedFolder.getID(),
                true,
                session,
                decorator);
            UserizedFolder[] userizedFolders = subfolders.getResponse();
            for (UserizedFolder folder : userizedFolders) {
                if (!isVirtualMailFolder(folder.getID())) {
                    folders.add(folder.getID());
                }
                if (folder.isSubscribed()) {
                    recurse(folder, folders, session, decorator);
                }
            }
        } catch (Exception x) {
            LOG.error(x.getMessage(), x);
        }
    }

    private boolean isVirtualMailFolder(String id) {
        return !id.contains("/");
    }

    private ContentType getMailType() {
        Map<Integer, ContentType> availableContentTypes = folderService.getAvailableContentTypes();
        return availableContentTypes.get(FolderObject.MAIL);
    }
}

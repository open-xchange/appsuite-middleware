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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal.converters;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.zmal.ACLPermission;
import com.openexchange.zmal.Services;
import com.openexchange.zmal.ZmalException;
import com.openexchange.zmal.config.ZmalConfig;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.zclient.ZFolder;
import com.zimbra.cs.zclient.ZGrant;
import com.zimbra.cs.zclient.ZGrant.GranteeType;
import com.zimbra.cs.zclient.ZMailbox;


/**
 * {@link ZFolderConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ZFolderConverter {

    private final String url;
    private final ZmalConfig config;
    private final Session session;

    /**
     * Initializes a new {@link ZMessageConverter}.
     */
    public ZFolderConverter(final String url, final ZmalConfig config) {
        super();
        this.url = url;
        this.config = config;
        session = config.getSession();
    }

    private ZFolder checkFolderById(final String fullName, final ZMailbox mailbox) throws ServiceException, OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullName)) {
            return mailbox.getUserRoot(); 
        }
        if ("INBOX".equals(fullName)) {
            return mailbox.getInbox();
        }
        final ZFolder folder = mailbox.getFolderById(fullName);
        if (null == folder) {
            throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullName);
        }
        return folder;
    }

    public MailFolder convert(final ZFolder folder, final ZMailbox mailbox) throws OXException {
        if (null == folder) {
            return null;
        }
        try {
            final MailFolder mailFolder = new MailFolder();
            mailFolder.setSupportsUserFlags(false);
            mailFolder.setExists(true);
            mailFolder.setShared(false);
            final String parentId = folder.getParentId();
            if (mailbox.getUserRoot().getId().equals(folder.getId())) {
                // Root
                mailFolder.setRootFolder(true);
                mailFolder.setName(MailFolder.DEFAULT_FOLDER_NAME);
                mailFolder.setFullname(MailFolder.DEFAULT_FOLDER_ID);
                mailFolder.setParentFullname(null);
                mailFolder.setDefaultFolder(false);
                mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                mailFolder.setSubfolders(true);
                mailFolder.setSubscribed(true);
                mailFolder.setSubscribedSubfolders(true);
                mailFolder.setHoldsMessages(false);
                mailFolder.setUnreadMessageCount(-1);
                mailFolder.setMessageCount(-1);
                mailFolder.setDeletedMessageCount(-1);
                mailFolder.setNewMessageCount(-1);
            } else {
                mailFolder.setRootFolder(false);
                mailFolder.setName(folder.getName());
                final String inboxId = mailbox.getInbox().getId();
                if (mailbox.getUserRoot().getId().equals(parentId)) {
                    mailFolder.setParentFullname(MailFolder.DEFAULT_FOLDER_ID);
                } else if (inboxId.equals(parentId)) {
                    mailFolder.setParentFullname("INBOX");
                } else {
                    ZFolder parent = folder.getParent();
                    if (null == parent) {
                        parent = checkFolderById(parentId, mailbox);
                    }
                    mailFolder.setParentFullname(parent.getPath());
                }
                final String id = folder.getId();
                mailFolder.setFullname(folder.getPath());
                if (inboxId.equals(id)) {
                    mailFolder.setDefaultFolder(true);
                    mailFolder.setDefaultFolderType(DefaultFolderType.INBOX);
                    mailFolder.setFullname("INBOX");
                } else if (mailbox.getDrafts().getId().equals(id)) {
                    mailFolder.setDefaultFolder(true);
                    mailFolder.setDefaultFolderType(DefaultFolderType.DRAFTS);
                } else if (mailbox.getSpam().getId().equals(id)) {
                    mailFolder.setDefaultFolder(true);
                    mailFolder.setDefaultFolderType(DefaultFolderType.SPAM);
                } else if (mailbox.getTrash().getId().equals(id)) {
                    mailFolder.setDefaultFolder(true);
                    mailFolder.setDefaultFolderType(DefaultFolderType.TRASH);
                } else {
                    mailFolder.setDefaultFolder(false);
                    mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                }
                mailFolder.setSubscribed(folder.isIMAPSubscribed());
                mailFolder.setHoldsMessages(true);
                if (folder.isNoInferiors()) {
                    mailFolder.setHoldsFolders(false);
                    mailFolder.setSubscribedSubfolders(false);
                    mailFolder.setSubfolders(false);
                } else {
                    mailFolder.setHoldsFolders(true);
                    final List<ZFolder> subFolders = folder.getSubFolders();
                    if (null != subFolders && !subFolders.isEmpty()) {
                        boolean subscribedSubfolders = false;
                        for (final ZFolder zFolder : subFolders) {
                            if (zFolder.isIMAPSubscribed()) {
                                subscribedSubfolders = true;
                                break;
                            }
                        }
                        mailFolder.setSubscribedSubfolders(subscribedSubfolders);
                        mailFolder.setSubfolders(true);
                    } else {
                        mailFolder.setSubscribedSubfolders(false);
                        mailFolder.setSubfolders(false);
                    }
                }
                final int unreadCount = folder.getUnreadCount();
                if (unreadCount >= 0) {
                    mailFolder.setUnreadMessageCount(unreadCount);
                } else {
                    mailFolder.setUnreadMessageCount(-1);
                }
                final int messageCount = folder.getMessageCount();
                if (messageCount >= 0) {
                    mailFolder.setMessageCount(messageCount);
                } else {
                    mailFolder.setMessageCount(-1);
                }
                mailFolder.setDeletedMessageCount(-1);
                mailFolder.setNewMessageCount(-1);
            }
            // Check ACL
            if (useACLs()) {
                // Own permission
                {
                    final DefaultMailPermission ownPermission = new DefaultMailPermission();
                    ownPermission.setAllPermission(
                        OCLPermission.NO_PERMISSIONS,
                        OCLPermission.NO_PERMISSIONS,
                        OCLPermission.NO_PERMISSIONS,
                        OCLPermission.NO_PERMISSIONS);
                    ownPermission.setFolderAdmin(false);
                    final String s = folder.getEffectivePerms();
                    if (!isEmpty(s)) {
                        if (s.indexOf('r') >= 0) {
                            ownPermission.setFolderAdmin(false);
                            ownPermission.setAllObjectPermission(
                                OCLPermission.READ_ALL_OBJECTS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS);
                            ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
                        }
                        if (s.indexOf('i') >= 0) {
                            ownPermission.setFolderAdmin(false);
                            ownPermission.setAllObjectPermission(
                                OCLPermission.READ_ALL_OBJECTS,
                                OCLPermission.WRITE_ALL_OBJECTS,
                                OCLPermission.DELETE_ALL_OBJECTS);
                            ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
                        }
                        if (s.indexOf('c') >= 0) {
                            ownPermission.setFolderAdmin(true);
                            ownPermission.setAllObjectPermission(
                                OCLPermission.READ_ALL_OBJECTS,
                                OCLPermission.WRITE_ALL_OBJECTS,
                                OCLPermission.DELETE_ALL_OBJECTS);
                            ownPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
                        }
                    }
                }
                // All permissions/grants
                final List<ZGrant> grants = folder.getGrants();
                if (null != grants && !grants.isEmpty()) {
                    final List<MailPermission> perms = new ArrayList<MailPermission>(grants.size());
                    final ContextService contextService = Services.getService(ContextService.class);
                    final Context ctx = contextService.getContext(session.getContextId());
                    for (final ZGrant zGrant : grants) {
                        if (!GranteeType.usr.equals(zGrant.getGranteeType())) {
                            continue;
                        }
                        // ACL
                        final ACLPermission permission = new ACLPermission();
                        final ACL acl = new ACL(zGrant.getGranteeName());
                        final String s = zGrant.getPermissions();
                        final Rights rights = new Rights();
                        if (!isEmpty(s)) {
                            if (s.indexOf('r') >= 0) {
                                rights.add(Rights.Right.READ);
                            }
                            if (s.indexOf('w') >= 0) {
                                rights.add(Rights.Right.WRITE);
                            }
                            if (s.indexOf('i') >= 0) {
                                rights.add(Rights.Right.INSERT);
                            }
                            if (s.indexOf('d') >= 0) {
                                rights.add(Rights.Right.DELETE);
                            }
                            if (s.indexOf('a') >= 0) {
                                rights.add(Rights.Right.ADMINISTER);
                            }
                        }
                        permission.parseACL(acl, ctx);
                        perms.add(permission);
                    }
                }
            } else {
                final DefaultMailPermission p = new DefaultMailPermission();
                p.setAllPermission(MailPermission.CREATE_SUB_FOLDERS, MailPermission.READ_ALL_OBJECTS, MailPermission.WRITE_ALL_OBJECTS, MailPermission.DELETE_ALL_OBJECTS);
                p.setEntity(session.getUserId());
                p.setGroupPermission(false);
                mailFolder.setOwnPermission(p);
                mailFolder.addPermission(p);
            }
            return mailFolder;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean useACLs() {
        return false;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}

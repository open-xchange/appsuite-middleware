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

package com.openexchange.mail.json.actions;

import java.util.EnumSet;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractArchiveMailAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractArchiveMailAction extends AbstractMailAction {

    /**
     * Initializes a new {@link AbstractArchiveMailAction}.
     * @param services
     */
    protected AbstractArchiveMailAction(ServiceLookup services) {
        super(services);
    }

    /**
     * Checks the archive full name for given arguments
     *
     * @param mailAccess The connected mail access
     * @param req The associated request
     * @return The archive full name
     * @throws OXException If checking archive full name fails
     */
    protected String checkArchiveFullNameFor(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, MailRequest req, int[] separatorRef) throws OXException {
        final int accountId = mailAccess.getAccountId();
        final ServerSession session = req.getSession();

        MailAccountStorageService service = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class.getName());
        }
        MailAccount mailAccount = service.getMailAccount(accountId, session.getUserId(), session.getContextId());

        // Check archive full name
        char separator;
        String archiveFullName = mailAccount.getArchiveFullname();
        final String parentFullName;
        String archiveName;
        if (isEmpty(archiveFullName)) {
            archiveName = mailAccount.getArchive();
            boolean updateAccount = false;
            if (isEmpty(archiveName)) {
                final User user = session.getUser();
                if (!AJAXRequestDataTools.parseBoolParameter("useDefaultName", req.getRequest(), true)) {
                    final String i18nArchive = StringHelper.valueOf(user.getLocale()).getString(MailStrings.ARCHIVE);
                    throw MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(Category.CATEGORY_USER_INPUT, i18nArchive);
                }
                // Select default name for archive folder
                archiveName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.DEFAULT_ARCHIVE);
                updateAccount = true;
            }
            final String prefix = mailAccess.getFolderStorage().getDefaultFolderPrefix();
            if (isEmpty(prefix)) {
                separator = mailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
                archiveFullName = archiveName;
                parentFullName = MailFolder.DEFAULT_FOLDER_ID;
            } else {
                separator = prefix.charAt(prefix.length() - 1);
                archiveFullName = new StringBuilder(prefix).append(archiveName).toString();
                parentFullName = prefix.substring(0, prefix.length() - 1);
            }
            // Update mail account
            if (updateAccount) {
                final MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                if (null != mass) {
                    final String af = archiveFullName;
                    ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            final MailAccountDescription mad = new MailAccountDescription();
                            mad.setId(accountId);
                            mad.setArchiveFullname(af);
                            mass.updateMailAccount(mad, EnumSet.of(Attribute.ARCHIVE_FULLNAME_LITERAL), session.getUserId(), session.getContextId(), session);
                            return null;
                        }
                    });
                }
            }
        } else {
            separator = mailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
            final int pos = archiveFullName.lastIndexOf(separator);
            if (pos > 0) {
                parentFullName = archiveFullName.substring(0, pos);
                archiveName = archiveFullName.substring(pos + 1);
            } else {
                parentFullName = MailFolder.DEFAULT_FOLDER_ID;
                archiveName = archiveFullName;
            }
        }
        if (!mailAccess.getFolderStorage().exists(archiveFullName)) {
            if (!AJAXRequestDataTools.parseBoolParameter("createIfAbsent", req.getRequest(), true)) {
                throw MailExceptionCode.FOLDER_NOT_FOUND.create(archiveFullName);
            }
            final MailFolderDescription toCreate = new MailFolderDescription();
            toCreate.setAccountId(accountId);
            toCreate.setParentAccountId(accountId);
            toCreate.setParentFullname(parentFullName);
            toCreate.setExists(false);
            toCreate.setFullname(archiveFullName);
            toCreate.setName(archiveName);
            toCreate.setSeparator(separator);
            {
                final DefaultMailPermission mp = new DefaultMailPermission();
                mp.setEntity(session.getUserId());
                final int p = MailPermission.ADMIN_PERMISSION;
                mp.setAllPermission(p, p, p, p);
                mp.setFolderAdmin(true);
                mp.setGroupPermission(false);
                toCreate.addPermission(mp);
            }
            mailAccess.getFolderStorage().createFolder(toCreate);
            CacheFolderStorage.getInstance().removeFromCache(parentFullName, "0", true, session);
        }

        separatorRef[0] = separator;
        return archiveFullName;
    }

}

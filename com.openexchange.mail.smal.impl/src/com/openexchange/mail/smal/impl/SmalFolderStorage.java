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

package com.openexchange.mail.smal.impl;

import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.api.IMailFolderStorageInfoSupport;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailFolderInfo;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.session.Session;

/**
 * {@link SmalFolderStorage} - The SMAL folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalFolderStorage extends AbstractSMALStorage implements IMailFolderStorage, IMailFolderStorageEnhanced2, IMailFolderStorageDelegator, IMailFolderStorageInfoSupport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SmalFolderStorage.class);

    private static final String DEFAULT_FOLDER_ID = MailFolder.DEFAULT_FOLDER_ID;

    /**
     * Initializes a new {@link SmalFolderStorage}.
     *
     * @throws OXException If initialization fails
     */
    public SmalFolderStorage(final Session session, final int accountId, final SmalMailAccess smalMailAccess) throws OXException {
        super(session, accountId, smalMailAccess);
    }

    @Override
    public boolean isInfoSupported() throws OXException {
        final IMailFolderStorage folderStorage = smalMailAccess.getDelegateMailAccess().getFolderStorage();
        return (folderStorage instanceof IMailFolderStorageInfoSupport) && ((IMailFolderStorageInfoSupport) folderStorage).isInfoSupported();
    }

    @Override
    public MailFolderInfo getFolderInfo(final String fullName) throws OXException {
        final IMailFolderStorage folderStorage = smalMailAccess.getDelegateMailAccess().getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageInfoSupport) {
            final IMailFolderStorageInfoSupport infoSupport = ((IMailFolderStorageInfoSupport) folderStorage);
            if (infoSupport.isInfoSupported()) {
                return infoSupport.getFolderInfo(fullName);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public List<MailFolderInfo> getAllFolderInfos(final boolean subscribedOnly) throws OXException {
        return getFolderInfos(null, subscribedOnly);
    }

    @Override
    public List<MailFolderInfo> getFolderInfos(final String optParentFullName, final boolean subscribedOnly) throws OXException {
        final IMailFolderStorage folderStorage = smalMailAccess.getDelegateMailAccess().getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageInfoSupport) {
            final IMailFolderStorageInfoSupport infoSupport = ((IMailFolderStorageInfoSupport) folderStorage);
            if (infoSupport.isInfoSupported()) {
                return infoSupport.getFolderInfos(optParentFullName, subscribedOnly);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public IMailFolderStorage getDelegateFolderStorage() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage();
    }

    @Override
    public boolean exists(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return true;
        }
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().exists(fullName);
    }

    @Override
    public MailFolder getFolder(final String fullName) throws OXException {
        final MailFolder folder = smalMailAccess.getDelegateMailAccess().getFolderStorage().getFolder(fullName);
        return folder;
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getSubfolders(parentFullName, all);
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        smalMailAccess.getDelegateMailAccess().getFolderStorage().checkDefaultFolders();
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        final String fullName = smalMailAccess.getDelegateMailAccess().getFolderStorage().createFolder(toCreate);
        try {
            submitFolderJob(fullName);
        } catch (final OXException e) {
            LOG.warn("Could not schedule folder job for folder {}.", fullName, e);
        }
        return fullName;
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        final String fn = smalMailAccess.getDelegateMailAccess().getFolderStorage().updateFolder(fullName, toUpdate);
        return fn;
    }

    @Override
    public String moveFolder(final String fullName, final String newFullName) throws OXException {
        final String nfn = smalMailAccess.getDelegateMailAccess().getFolderStorage().moveFolder(fullName, newFullName);
        try {
            submitFolderJob(fullName);
            submitFolderJob(nfn);
        } catch (final OXException e) {
            LOG.warn("Could not schedule folder job for folder {}.", fullName, e);
        }

        return nfn;
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        final String retval = smalMailAccess.getDelegateMailAccess().getFolderStorage().deleteFolder(fullName, hardDelete);
        try {
            submitFolderJob(retval);
            if (!hardDelete) {
                submitFolderJob(getTrashFolder());
            }
        } catch (final OXException e) {
            LOG.warn("Could not schedule folder job for folder {}.", retval, e);
        }
        return retval;
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        smalMailAccess.getDelegateMailAccess().getFolderStorage().clearFolder(fullName, hardDelete);
        try {
            submitFolderJob(fullName);
        } catch (final OXException e) {
            LOG.warn("Could not schedule folder job for folder {}.", fullName, e);
        }
    }

    @Override
    public Quota[] getQuotas(final String fullName, final Type[] types) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getQuotas(fullName, types);
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getConfirmedHamFolder();
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getConfirmedSpamFolder();
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getDraftsFolder();
    }

    @Override
    public String getSpamFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getSpamFolder();
    }

    @Override
    public String getSentFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getSentFolder();
    }

    @Override
    public String getTrashFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getTrashFolder();
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getFolder(DEFAULT_FOLDER_ID);
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getDefaultFolderPrefix();
    }

    @Override
    public String renameFolder(final String fullName, final String newName) throws OXException {
        final String nfn = smalMailAccess.getDelegateMailAccess().getFolderStorage().renameFolder(fullName, newName);
        try {
            submitFolderJob(fullName);
            submitFolderJob(nfn);
        } catch (final OXException e) {
            LOG.warn("Could not schedule folder job for folder {}.", fullName, e);
        }

        return nfn;
    }

    @Override
    public String deleteFolder(final String fullName) throws OXException {
        return deleteFolder(fullName, false);
    }

    @Override
    public void clearFolder(final String fullName) throws OXException {
        clearFolder(fullName, false);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getPath2DefaultFolder(fullName);
    }

    @Override
    public Quota getStorageQuota(final String fullName) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getStorageQuota(fullName);
    }

    @Override
    public Quota getMessageQuota(final String fullName) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getFolderStorage().getMessageQuota(fullName);
    }

    @Override
    public void releaseResources() throws OXException {
        smalMailAccess.getDelegateMailAccess().getFolderStorage().releaseResources();
    }

    @Override
    public int getUnreadCounter(final String fullName) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess = smalMailAccess.getDelegateMailAccess();
        final IMailFolderStorage folderStorage = delegateMailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            return ((IMailFolderStorageEnhanced) delegateMailAccess.getFolderStorage()).getUnreadCounter(ensureFullName(fullName));
        }
        return delegateMailAccess.getMessageStorage().getUnreadMessages(
            ensureFullName(fullName),
            MailSortField.RECEIVED_DATE,
            OrderDirection.DESC,
            FIELDS_ID,
            -1).length;
    }

    @Override
    public void expungeFolder(final String fullName) throws OXException {
        expungeFolder(fullName, false);
    }

    @Override
    public void expungeFolder(final String fullName, final boolean hardDelete) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess = smalMailAccess.getDelegateMailAccess();
        final IMailFolderStorage folderStorage = delegateMailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            ((IMailFolderStorageEnhanced) folderStorage).expungeFolder(ensureFullName(fullName), hardDelete);
            return;
        }
        final IMailMessageStorage messageStorage = delegateMailAccess.getMessageStorage();
        final MailMessage[] messages = messageStorage.searchMessages(
            ensureFullName(fullName),
            IndexRange.NULL,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            new FlagTerm(MailMessage.FLAG_DELETED, true),
            FIELDS_ID);
        final List<String> mailIds = new ArrayList<String>(messages.length);
        for (int i = 0; i < messages.length; i++) {
            final MailMessage mailMessage = messages[i];
            if (null != mailMessage) {
                mailIds.add(mailMessage.getMailId());
            }
        }
        if (hardDelete) {
            messageStorage.deleteMessages(fullName, mailIds.toArray(new String[0]), true);
        } else {
            messageStorage.moveMessages(fullName, folderStorage.getTrashFolder(), mailIds.toArray(new String[0]), true);
        }
    }

    private static String ensureFullName(final String fullName) {
        return prepareMailFolderParam(fullName).getFullname();
    }

    @Override
    public int getNewCounter(final String fullName) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess = smalMailAccess.getDelegateMailAccess();
        final IMailFolderStorage folderStorage = delegateMailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            return ((IMailFolderStorageEnhanced) folderStorage).getNewCounter(ensureFullName(fullName));
        }
        final MailMessage[] messages =
            delegateMailAccess.getMessageStorage().searchMessages(
                ensureFullName(fullName),
                IndexRange.NULL,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                null,
                FIELDS_FLAGS);
        int count = 0;
        for (final MailMessage mailMessage : messages) {
            if (mailMessage.isRecent()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getTotalCounter(final String fullName) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess = smalMailAccess.getDelegateMailAccess();
        final IMailFolderStorage folderStorage = delegateMailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            return ((IMailFolderStorageEnhanced) folderStorage).getTotalCounter(ensureFullName(fullName));
        }
        return delegateMailAccess.getMessageStorage().searchMessages(
            ensureFullName(fullName),
            IndexRange.NULL,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            null,
            FIELDS_ID).length;
    }

    @Override
    public int[] getTotalAndUnreadCounter(String fullName) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess = smalMailAccess.getDelegateMailAccess();
        final IMailFolderStorage folderStorage = delegateMailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced2) {
            return ((IMailFolderStorageEnhanced2) folderStorage).getTotalAndUnreadCounter(ensureFullName(fullName));
        }
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            final IMailFolderStorageEnhanced storageEnhanced = (IMailFolderStorageEnhanced) folderStorage;
            final String ensuredFullName = ensureFullName(fullName);
            final int total = storageEnhanced.getTotalCounter(ensuredFullName);
            final int unread = storageEnhanced.getUnreadCounter(ensuredFullName);
            return new int[] { total, unread };
        }

        final int total = delegateMailAccess.getMessageStorage().searchMessages(
            ensureFullName(fullName),
            IndexRange.NULL,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            null,
            FIELDS_ID).length;

        final int unread = delegateMailAccess.getMessageStorage().getUnreadMessages(
            ensureFullName(fullName),
            MailSortField.RECEIVED_DATE,
            OrderDirection.DESC,
            FIELDS_ID,
            -1).length;

        return new int[] { total, unread };
    }

}

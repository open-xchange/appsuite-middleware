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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.mail;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.api2.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * {@link MailFolderStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderStorage implements FolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailFolderStorage.class);

    private final String treeId;

    /**
     * Initializes a new {@link MailFolderStorage}.
     * 
     * @param treeId The tree identifier
     */
    public MailFolderStorage(final String treeId) {
        super();
        this.treeId = treeId;
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) params.getParameter(
                MailFolderType.getInstance(),
                MailStorageParameterConstants.PARAM_MAIL_ACCESS);
            if (null != mailServletInterface) {
                mailServletInterface.close(true);
            }
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub

    }

    public void deleteFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailStorageParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(
                    MailException.Code.MISSING_PARAM,
                    MailStorageParameterConstants.PARAM_MAIL_ACCESS));
            }

            mailServletInterface.deleteFolder(folderId);
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public Folder getDefaultFolder(final int entity, final StorageParameters storageParameters) throws FolderException {
        // TODO Which default folder? Trash, Sent, ...
        return null;
    }

    public Folder getFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailStorageParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(
                    MailException.Code.MISSING_PARAM,
                    MailStorageParameterConstants.PARAM_MAIL_ACCESS));
            }

            final MailFolder mailFolder = mailServletInterface.getFolder(folderId, true);
            final FolderImpl retval = new FolderImpl(mailFolder, mailServletInterface.getAccountID());
            retval.setTreeID(treeId);

            // TODO: Fill subfolder IDs? Or leave to null to force FolderStorage.getSubfolders()?
            final SearchIterator<MailFolder> iter = mailServletInterface.getChildFolders(MailFolderUtility.prepareFullname(
                mailServletInterface.getAccountID(),
                mailFolder.getFullname()), true);
            try {
                final String[] subfolderIds = new String[iter.size()];
                for (int i = 0; i < subfolderIds.length; i++) {
                    subfolderIds[i] = MailFolderUtility.prepareFullname(mailServletInterface.getAccountID(), iter.next().getFullname());
                }
            } catch (final SearchIteratorException e) {
                throw new FolderException(e);
            } catch (final OXException e) {
                throw new FolderException(e);
            } finally {
                try {
                    iter.close();
                } catch (final SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            return retval;
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public FolderType getFolderType() {
        return MailFolderType.getInstance();
    }

    public SortableId[] getSubfolders(final String parentId, final StorageParameters storageParameters) throws FolderException {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) storageParameters.getParameter(
                MailFolderType.getInstance(),
                MailStorageParameterConstants.PARAM_MAIL_ACCESS);

            if (null == mailServletInterface) {
                throw new FolderException(new MailException(
                    MailException.Code.MISSING_PARAM,
                    MailStorageParameterConstants.PARAM_MAIL_ACCESS));
            }

            final SearchIterator<MailFolder> iter = mailServletInterface.getChildFolders(parentId, true);
            try {
                final int size = iter.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = size; i > 0; i--) {
                    list.add(new FullnameId(MailFolderUtility.prepareFullname(
                        mailServletInterface.getAccountID(),
                        iter.next().getFullname()), i));
                }
                return list.toArray(new SortableId[list.size()]);
            } catch (final SearchIteratorException e) {
                throw new FolderException(e);
            } catch (final OXException e) {
                throw new FolderException(e);
            } finally {
                try {
                    iter.close();
                } catch (final SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void rollback(final StorageParameters params) {
        try {
            final MailServletInterface mailServletInterface = (MailServletInterface) params.getParameter(
                MailFolderType.getInstance(),
                MailStorageParameterConstants.PARAM_MAIL_ACCESS);
            if (null != mailServletInterface) {
                mailServletInterface.close(true);
            }
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public StorageParameters startTransaction(final StorageParameters parameters) throws FolderException {
        try {
            parameters.putParameter(
                MailFolderType.getInstance(),
                MailStorageParameterConstants.PARAM_MAIL_ACCESS,
                MailServletInterface.getInstance(parameters.getSession()));
            return parameters;
        } catch (final MailException e) {
            throw new FolderException(e);
        }
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub

    }

}

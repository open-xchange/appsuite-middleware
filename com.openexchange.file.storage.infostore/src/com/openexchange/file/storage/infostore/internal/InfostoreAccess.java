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

package com.openexchange.file.storage.infostore.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.java.Strings;


/**
 * Encapsulates common methods needed by classes that delegate calls to {@link InfostoreFacade}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class InfostoreAccess {

    protected static final InfostoreFacade VIRTUAL_INFOSTORE = new VirtualFolderInfostoreFacade();
    protected static final Set<Long> VIRTUAL_FOLDERS;
    static {
        final Set<Long> set = new HashSet<Long>(4);
        set.add(Long.valueOf(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID));
        set.add(Long.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID));
        set.add(Long.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID));
        VIRTUAL_FOLDERS = Collections.unmodifiableSet(set);
    }

    protected final InfostoreFacade infostore;

    protected InfostoreAccess(InfostoreFacade infostore) {
        super();
        this.infostore = infostore;
    }

    protected InfostoreFacade getInfostore(final String folderId) throws OXException {
        if (Strings.isNotEmpty(folderId)) {
            try {
                if (VIRTUAL_FOLDERS.contains(Long.valueOf(folderId))) {
                    return VIRTUAL_INFOSTORE;
                }
            } catch (NumberFormatException e) {
                throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(e, folderId);
            }
        }        
        return infostore;
    }

    protected static int ID(final String id) {
        return Integer.parseInt(id);
    }

    protected static long FOLDERID(final String folderId) {
        return Long.parseLong(folderId);
    }

    protected static int VERSION(final String version) {
        int iVersion = InfostoreFacade.CURRENT_VERSION;
        if (version != FileStorageFileAccess.CURRENT_VERSION) {
            iVersion = Integer.parseInt(version);
        }

        return iVersion;
    }

}

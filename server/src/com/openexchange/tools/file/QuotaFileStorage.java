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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.tools.file;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.file.external.FileStorageException;
import com.openexchange.tools.file.external.QuotaFileStorageStarter;

public class QuotaFileStorage extends FileStorage {

    private static com.openexchange.tools.file.external.QuotaFileStorage qfs;

    public QuotaFileStorage(final URI uri, final Context ctx, final QuotaFileStorageStarter qfss) throws com.openexchange.tools.file.FileStorageException {
        super(uri);
        try {
            qfs = qfss.getQuotaFileStorage(ctx, uri);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    public long getUsage() throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.getUsage();
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    public void recalculateUsage() throws com.openexchange.tools.file.FileStorageException {
        try {
            qfs.recalculateUsage();
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    public long getQuota() throws FileStorageException {
        return qfs.getQuota();
    }

    @Override
    public boolean deleteFile(final String identifier) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.deleteFile(identifier);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public Set<String> deleteFiles(final String[] identifiers) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.deleteFiles(identifiers);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public InputStream getFile(final String name) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.getFile(name);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public SortedSet<String> getFileList() throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.getFileList();
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public long getFileSize(final String name) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.getFileSize(name);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public String getMimeType(final String name) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.getMimeType(name);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public void recreateStateFile() throws com.openexchange.tools.file.FileStorageException {
        try {
            qfs.recreateStateFile();
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public void remove() throws com.openexchange.tools.file.FileStorageException {
        try {
            qfs.remove();
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public String saveNewFile(final InputStream file) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.saveNewFile(file);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }

    @Override
    public String saveNewFile(final InputStream file, final long sizeHint) throws com.openexchange.tools.file.FileStorageException {
        try {
            return qfs.saveNewFile(file);
        } catch (final FileStorageException e) {
            throw new com.openexchange.tools.file.FileStorageException(e);
        }
    }
}

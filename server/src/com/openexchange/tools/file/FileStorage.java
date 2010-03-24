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
import com.openexchange.tools.file.external.QuotaFileStorageStarter;

public class FileStorage {

    public static QuotaFileStorageStarter qfss;

    private static com.openexchange.tools.file.external.FileStorage fs;

    public static com.openexchange.tools.file.external.FileStorageStarter fss;

    protected FileStorage(final URI uri) throws FileStorageException {
        try {
            fs = fss.getFileStorage(uri);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public static final FileStorage getInstance(final URI uri) throws FileStorageException {
        FileStorage retval = null;
        if (fss != null) {
            retval = new com.openexchange.tools.file.FileStorage(uri);
        } else {
            throw new FileStorageException(FileStorageException.Code.INSTANTIATIONERROR);
        }
        return retval;
    }

    public static final FileStorage getInstance(final URI uri, final Context ctx) throws FileStorageException {
        FileStorage retval = null;
        if (qfss != null) {
            retval = new com.openexchange.tools.file.QuotaFileStorage(uri, ctx, qfss);
        } else {
            throw new FileStorageException(FileStorageException.Code.INSTANTIATIONERROR);
        }
        return retval;
    }

    public boolean deleteFile(final String identifier) throws FileStorageException {
        try {
            return fs.deleteFile(identifier);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public Set<String> deleteFiles(final String[] identifiers) throws FileStorageException {
        try {
            return fs.deleteFiles(identifiers);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public InputStream getFile(final String name) throws FileStorageException {
        try {
            return fs.getFile(name);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public SortedSet<String> getFileList() throws FileStorageException {
        try {
            return fs.getFileList();
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public long getFileSize(final String name) throws FileStorageException {
        try {
            return fs.getFileSize(name);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public String getMimeType(final String name) throws FileStorageException {
        try {
            return fs.getMimeType(name);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public void recreateStateFile() throws FileStorageException {
        try {
            fs.recreateStateFile();
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }

    }

    public void remove() throws FileStorageException {
        try {
            fs.remove();
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public String saveNewFile(final InputStream file) throws FileStorageException {
        try {
            return fs.saveNewFile(file);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public String saveNewFile(final InputStream file, final long sizeHint) throws FileStorageException {
        try {
            return fs.saveNewFile(file);
        } catch (final com.openexchange.tools.file.external.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public void close() {
        fs = null;
    }

}

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

package com.openexchange.tools.file;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link QuotaFileStorage} - The quota-aware file storage delegating to new <code>com.openexchange.filestore</code> API.
 *
 * @deprecated Please use new <code>com.openexchange.filestore</code> API
 */
@Deprecated
public final class QuotaFileStorage extends FileStorage {

    private static volatile QuotaFileStorageService qfss;

    /**
     * Gets the <tt>QuotaFileStorage</tt> instance for specified URI and context.
     *
     * @param uri The file storage's URI
     * @param ctx The associated context
     * @return The appropriate <tt>QuotaFileStorage</tt> instance
     * @throws OXException If a <tt>QuotaFileStorage</tt> instance cannot be returned
     */
    public static final QuotaFileStorage getInstance(final URI uri, final Context ctx) throws OXException {
        QuotaFileStorageService factory = qfss;
        if (factory == null) {
            throw FileStorageCodes.INSTANTIATIONERROR.create("No quota file storage starter registered.");
        }
        return new com.openexchange.tools.file.QuotaFileStorage(uri, ctx, factory);
    }

    /**
     * Statically sets the <tt>QuotaFileStorageFactory</tt> instance.
     *
     * @param qfss The factory instance to set
     */
    public static void setQuotaFileStorageStarter(final QuotaFileStorageService qfss) {
        QuotaFileStorage.qfss = qfss;
    }

    // ----------------------------------------------------------------------------------------------------------- //

    private com.openexchange.filestore.QuotaFileStorage delegateQuotaFileStorage;

    private QuotaFileStorage(URI uri, Context ctx, QuotaFileStorageService qfss) throws OXException {
        super();
        delegateQuotaFileStorage = qfss.getQuotaFileStorage(ctx.getContextId(), Info.general());
    }

    private com.openexchange.filestore.QuotaFileStorage getDelegateQuotaFileStorage() {
        // Helper method to ensure QuotaFileStorage instance is not null
        com.openexchange.filestore.QuotaFileStorage tmp = this.delegateQuotaFileStorage;
        if (null == tmp) {
            throw new IllegalStateException("QuotaFileStorage has already been closed.");
        }
        return tmp;
    }

    public long getUsage() throws OXException {
        return getDelegateQuotaFileStorage().getUsage();
    }

    public void recalculateUsage() throws OXException {
        getDelegateQuotaFileStorage().recalculateUsage();
    }

    public void recalculateUsage(Set<String> filesToIgnore) throws OXException {
        getDelegateQuotaFileStorage().recalculateUsage(filesToIgnore);
    }

    public long getQuota() throws OXException {
        return getDelegateQuotaFileStorage().getQuota();
    }

    @Override
    public boolean deleteFile(final String identifier) throws OXException {
        return getDelegateQuotaFileStorage().deleteFile(identifier);
    }

    @Override
    public Set<String> deleteFiles(final String[] identifiers) throws OXException {
        if (null == identifiers || 0 == identifiers.length) {
            return Collections.emptySet();
        }
        return getDelegateQuotaFileStorage().deleteFiles(identifiers);
    }

    @Override
    public InputStream getFile(final String name) throws OXException {
        return getDelegateQuotaFileStorage().getFile(name);
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        return getDelegateQuotaFileStorage().getFileList();
    }

    @Override
    public long getFileSize(final String name) throws OXException {
        return getDelegateQuotaFileStorage().getFileSize(name);
    }

    @Override
    public String getMimeType(final String name) throws OXException {
        return getDelegateQuotaFileStorage().getMimeType(name);
    }

    @Override
    public void recreateStateFile() throws OXException {
        getDelegateQuotaFileStorage().recreateStateFile();
    }

    @Override
    public void remove() throws OXException {
        getDelegateQuotaFileStorage().remove();
    }

    @Override
    public String saveNewFile(final InputStream file) throws OXException {
        return getDelegateQuotaFileStorage().saveNewFile(file);
    }

    public String saveNewFile(final InputStream file, final long sizeHint) throws OXException {
        return getDelegateQuotaFileStorage().saveNewFile(file, sizeHint);
    }

    public long appendToFile(InputStream file, String name, long offset, long sizeHint) throws OXException {
        return getDelegateQuotaFileStorage().appendToFile(file, name, offset, sizeHint);
    }

    @Override
    public void close() {
        delegateQuotaFileStorage = null;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        return getDelegateQuotaFileStorage().appendToFile(file, name, offset);
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        getDelegateQuotaFileStorage().setFileLength(length, name);
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        return getDelegateQuotaFileStorage().getFile(name, offset, length);
    }

}

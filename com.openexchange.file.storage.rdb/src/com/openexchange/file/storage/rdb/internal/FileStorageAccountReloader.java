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

package com.openexchange.file.storage.rdb.internal;

import java.util.Map;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.caching.dynamic.Refresher;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.ServiceAware;

/**
 * {@link FileStorageAccountReloader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class FileStorageAccountReloader extends Refresher<FileStorageAccount> implements FileStorageAccount, ServiceAware {

    private static final long serialVersionUID = -522777266183406469L;

    /**
     * Cached delegate.
     */
    private FileStorageAccount delegate;

    /**
     * Initializes a new {@link FileStorageAccountReloader}.
     *
     * @throws OXException If initial load of the object fails.
     */
    public FileStorageAccountReloader(final OXObjectFactory<FileStorageAccount> factory, final String regionName) throws OXException {
        super(factory, regionName, true);
        delegate = refresh();
    }

    /**
     * @throws RuntimeException if refreshing fails.
     */
    private void updateDelegate() throws RuntimeException {
        try {
            delegate = refresh();
        } catch (final OXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        updateDelegate();
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        updateDelegate();
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return "FileStorageAccountReloader: " + delegate.toString();
    }

    @Override
    public Map<String, Object> getConfiguration() {
        updateDelegate();
        return delegate.getConfiguration();
    }

    @Override
    public String getDisplayName() {
        updateDelegate();
        return delegate.getDisplayName();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public FileStorageService getFileStorageService() {
        updateDelegate();
        return delegate.getFileStorageService();
    }

    @Override
    public String getServiceId() {
        updateDelegate();
        if (delegate instanceof ServiceAware) {
            return ((ServiceAware) delegate).getServiceId();
        }
        final FileStorageService service = delegate.getFileStorageService();
        return null == service ? null : service.getId();
    }

}

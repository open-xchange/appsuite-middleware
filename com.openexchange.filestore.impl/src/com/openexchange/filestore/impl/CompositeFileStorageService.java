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


package com.openexchange.filestore.impl;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;

/**
 * {@link CompositeFileStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeFileStorageService implements FileStorageService, ServiceTrackerCustomizer<FileStorageProvider, FileStorageProvider> {

    /** The list of known providers */
    private final List<FileStorageProvider> providers = new CopyOnWriteArrayList<FileStorageProvider>();

    /** The bundle context */
    private final BundleContext bundleContext;

    /**
     * Initializes a new {@link CompositeFileStorageService}.
     */
    public CompositeFileStorageService(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
    }

    @Override
    public FileStorage getFileStorage(URI uri) throws OXException {
        if (null == uri) {
            return null;
        }

        URI fsUri = FileStorages.ensureScheme(uri);
        FileStorageProvider candidate = null;
        for (FileStorageProvider fac : providers) {
            if (fac.supports(fsUri) && (null == candidate || fac.getRanking() > candidate.getRanking())) {
                candidate = fac;
            }
        }
        if (null != candidate && candidate.getRanking() >= DEFAULT_RANKING) {
            return new CloseableTrackingFileStorage(candidate.getFileStorage(fsUri));
        }

        /*
         * Fall back to default implementation
         */

        return new CloseableTrackingFileStorage(getInternalFileStorage(fsUri));
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        if (null == uri) {
            return null;
        }

        try {
            LocalFileStorage standardFS = new LocalFileStorage(uri);
            HashingFileStorage hashedFS = new HashingFileStorage(new File(new File(uri), "hashed"));
            return new CompositingFileStorage(standardFS, "hashed", Collections.<String, FileStorage> singletonMap("hashed", hashedFS));
        } catch (IllegalArgumentException e) {
            throw OXException.general("Cannot create file storage for URI: \"" + uri + "\". That URI does not hold the preconditions to be absolute, hierarchical with a scheme equal to \"file\", a non-empty path component, and undefined authority, query, and fragment components.", e);
        }
    }

    @Override
    public boolean supports(URI uri) throws OXException {
        return true;
    }

    @Override
    public int getRanking() {
        return Integer.MAX_VALUE;
    }

    // ---------------------------------------- ServiceTracker methods --------------------------------------------------

    @Override
    public FileStorageProvider addingService(ServiceReference<FileStorageProvider> reference) {
        FileStorageProvider provider = bundleContext.getService(reference);
        synchronized (this) {
            List<FileStorageProvider> providers = this.providers;
            if (!providers.contains(provider)) {
                providers.add(provider);
                return provider;
            }
        }

        bundleContext.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FileStorageProvider> reference, FileStorageProvider provider) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<FileStorageProvider> reference, FileStorageProvider provider) {
        boolean contained = providers.remove(provider);
        if (contained) {
            bundleContext.ungetService(reference);
        }
    }

}

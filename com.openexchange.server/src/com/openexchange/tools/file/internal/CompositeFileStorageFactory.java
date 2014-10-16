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


package com.openexchange.tools.file.internal;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.server.osgi.ServerActivator;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageFactory;
import com.openexchange.tools.file.external.FileStorageFactoryCandidate;

/**
 * {@link CompositeFileStorageFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeFileStorageFactory implements FileStorageFactory, ServiceTrackerCustomizer<FileStorageFactoryCandidate, FileStorageFactoryCandidate> {

    /** The list of known factory candidates */
    protected static final List<FileStorageFactoryCandidate> FACTORY_CANDIDATES = new CopyOnWriteArrayList<FileStorageFactoryCandidate>();

    /**
     * Initializes a new {@link CompositeFileStorageFactory}.
     */
    public CompositeFileStorageFactory() {
        super();
    }

    @Override
    public FileStorage getFileStorage(URI uri) throws OXException {
        if (null== uri) {
            return null;
        }

        FileStorageFactoryCandidate candidate = null;
        for (final FileStorageFactoryCandidate fac : FACTORY_CANDIDATES) {
            if (fac.supports(uri) && (null == candidate || fac.getRanking() > candidate.getRanking())) {
                candidate = fac;
            }
        }
        if (null != candidate && candidate.getRanking() >= DEFAULT_RANKING) {
            return candidate.getFileStorage(uri);
        }

        /*
         * Fall back to default implementation
         */

        return getInternalFileStorage(uri);
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        if (null== uri) {
            return null;
        }

        try {
            final LocalFileStorage standardFS = new LocalFileStorage(uri);
            final HashingFileStorage hashedFS = new HashingFileStorage(new File(new File(uri), "hashed"));
            final CompositingFileStorage cStorage = new CompositingFileStorage();

            cStorage.addStore(standardFS);
            cStorage.addStore("hashed", hashedFS);
            cStorage.setSavePrefix("hashed");

            return cStorage;
        } catch (final IllegalArgumentException e) {
            throw OXException.general("Cannot create file storage for URI: \"" + uri + "\". Wrong or missing FileStorage bundle for scheme " + uri.getScheme() + "?", e);
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

    @Override
    public FileStorageFactoryCandidate addingService(ServiceReference<FileStorageFactoryCandidate> reference) {
        final BundleContext context = ServerActivator.getContext();
        final FileStorageFactoryCandidate candidate = context.getService(reference);
        synchronized (this) {
            List<FileStorageFactoryCandidate> candidates = FACTORY_CANDIDATES;
            if (!candidates.contains(candidate)) {
                candidates.add(candidate);
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FileStorageFactoryCandidate> reference, FileStorageFactoryCandidate candidate) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<FileStorageFactoryCandidate> reference, FileStorageFactoryCandidate candidate) {
        boolean contained = FACTORY_CANDIDATES.remove(candidate);
        if (contained) {
            final BundleContext context = ServerActivator.getContext();
            if (null != context) {
                context.ungetService(reference);
            }
        }
    }

}

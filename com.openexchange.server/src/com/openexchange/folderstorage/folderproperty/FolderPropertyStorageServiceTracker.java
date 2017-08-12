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

package com.openexchange.folderstorage.folderproperty;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.java.Strings;

/**
 * {@link FolderPropertyStorageServiceTracker} - A {@link ServiceTrackerCustomizer} for the {@link FolderPropertyStorage} to track
 * real folder storages; meaning those storages which indicate to handle the {@link FolderStorage#REAL_TREE_ID} tree identifier.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class FolderPropertyStorageServiceTracker implements ServiceTrackerCustomizer<FolderStorage, FolderStorage> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderPropertyStorageServiceTracker.class);

    private final static String TREE_IDENTIFIER = "tree";

    private final BundleContext context;

    /**
     * Initializes a new {@link FolderPropertyStorageServiceTracker}.
     * 
     * @param {@link BundleContext} to get the Services from
     */
    public FolderPropertyStorageServiceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public FolderStorage addingService(ServiceReference<FolderStorage> reference) {
        try {
            final FolderStorage storage = context.getService(reference);
            final String treeId = String.valueOf(reference.getProperty(TREE_IDENTIFIER));
            if (Strings.isEmpty(treeId)) {
                LOG.error("Missing tree identifier property \"tree\" for {}", storage.getClass().getName());
            } else {
                if (FolderPropertyStorageRegistry.getInstance().addFolderStorage(treeId, storage)) {
                    return storage;
                }
            }
        } catch (Exception e) {
            LOG.debug("Could not register FileStorage for PropertyFolderStorage.", e);
        }
        // Fall back
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FolderStorage> reference, FolderStorage service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<FolderStorage> reference, FolderStorage service) {
        if (null != service) {
            try {
                final String treeId = String.valueOf(reference.getProperty(TREE_IDENTIFIER));
                if (Strings.isEmpty(treeId)) {
                    LOG.error("Missing tree identifier property \"tree\" for {}", service.getClass().getName());
                } else {
                    FolderPropertyStorageRegistry.getInstance().removeFolderStorage(treeId, service);
                }
            } catch (Exception e) {
                LOG.debug("Could not remove FolderStorage from registry", e);
            }
            context.ungetService(reference);
        }
    }
}

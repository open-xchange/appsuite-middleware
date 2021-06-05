/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.folderstorage.osgi;

import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.google.common.collect.ImmutableSet;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderModifier;
import com.openexchange.folderstorage.FolderStorageFolderModifier;

/**
 * {@link FolderModifierTracker} - The tracker for folder modifiers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderModifierTracker implements ServiceTrackerCustomizer<FolderModifier,FolderModifier> {

    private final BundleContext context;
    private final FolderStorageFolderModifier folderStorage;
    private final Set<ContentType> supportedTypes;

    /**
     * Initializes a new {@link FolderModifierTracker}.
     */
    public FolderModifierTracker(FolderStorageFolderModifier folderStorage, BundleContext context) {
        super();
        this.folderStorage = folderStorage;
        this.context = context;
        supportedTypes = ImmutableSet.copyOf(folderStorage.getSupportedContentTypes());
    }

    @Override
    public FolderModifier addingService(ServiceReference<FolderModifier> reference) {
        FolderModifier modifier = context.getService(reference);
        if (supportedTypes.contains(modifier.getContentType())) {
            folderStorage.addFolderModifier(modifier);
            return modifier;
        }

        // Nothing to track, return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FolderModifier> reference, FolderModifier modifier) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<FolderModifier> reference, FolderModifier modifier) {
        if (null != modifier) {
            try {
                if (modifier.getContentType().equals(folderStorage.getDefaultContentType())) {
                    folderStorage.removeFolderModifier(modifier);
                }
            } finally {
                context.ungetService(reference);
            }
        }
    }

}

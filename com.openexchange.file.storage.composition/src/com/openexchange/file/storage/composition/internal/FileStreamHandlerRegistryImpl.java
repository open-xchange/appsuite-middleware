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

package com.openexchange.file.storage.composition.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.FileStreamHandler;

/**
 * {@link FileStreamHandlerRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStreamHandlerRegistryImpl extends ServiceTracker<FileStreamHandler, FileStreamHandler> implements FileStreamHandlerRegistry {

    private final ConcurrentMap<Class<? extends FileStreamHandler>, FileStreamHandler> registry;
    private final Comparator<FileStreamHandler> comparator;

    /**
     * Initializes a new {@link FileStreamHandlerRegistryImpl}.
     *
     * @param context The bundle context
     */
    public FileStreamHandlerRegistryImpl(final BundleContext context) {
        super(context, FileStreamHandler.class, null);
        registry = new ConcurrentHashMap<Class<? extends FileStreamHandler>, FileStreamHandler>(8, 0.9f, 1);
        comparator = new Comparator<FileStreamHandler>() {

            @Override
            public int compare(final FileStreamHandler o1, final FileStreamHandler o2) {
                final int r1 = o1 == null ? 0 : o1.getRanking();
                final int r2 = o2 == null ? 0 : o2.getRanking();
                return (r1 < r2 ? 1 : (r1 == r2 ? 0 : -1));
            }
        };
    }

    @Override
    public FileStreamHandler addingService(final ServiceReference<FileStreamHandler> reference) {
        final FileStreamHandler streamHandler = super.addingService(reference);
        if (null == registry.putIfAbsent(streamHandler.getClass(), streamHandler)) {
            return streamHandler;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<FileStreamHandler> reference, final FileStreamHandler streamHandler) {
        if (null != streamHandler) {
            registry.remove(streamHandler.getClass());
            super.removedService(reference, streamHandler);
        }
    }

    @Override
    public Collection<FileStreamHandler> getHandlers() {
        final List<FileStreamHandler> ret = new ArrayList<FileStreamHandler>(registry.values());
        Collections.sort(ret, comparator);
        return ret;
    }

}

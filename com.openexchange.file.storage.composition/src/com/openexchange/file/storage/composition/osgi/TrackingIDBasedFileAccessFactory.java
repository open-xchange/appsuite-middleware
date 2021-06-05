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

package com.openexchange.file.storage.composition.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.file.storage.composition.DelegatingIDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedAdministrativeFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.internal.CompositingIDBasedAdministrativeFileAccess;
import com.openexchange.file.storage.composition.internal.CompositingIDBasedFileAccess;
import com.openexchange.marker.OXThreadMarkers;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link TrackingIDBasedFileAccessFactory} - A file access factory, which tracks possible delegating file access factories that possibly
 * influence file retrieval/storing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class TrackingIDBasedFileAccessFactory extends RankingAwareNearRegistryServiceTracker<DelegatingIDBasedFileAccessFactory> implements IDBasedFileAccessFactory {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link TrackingIDBasedFileAccessFactory}.
     */
    public TrackingIDBasedFileAccessFactory(ServiceLookup services, BundleContext context) {
        super(context, DelegatingIDBasedFileAccessFactory.class);
        this.services = services;
    }

    @Override
    public IDBasedFileAccess createAccess(Session session) {
        DelegatingIDBasedFileAccessFactory df = getHighestRanked();
        return null == df ? newFileAccess(session) : df.createAccess(newFileAccess(session), session);
    }

    private CompositingIDBasedFileAccess newFileAccess(Session session) {
        CompositingIDBasedFileAccess fileAccess = new CompositingIDBasedFileAccess(session, services);
        OXThreadMarkers.rememberCloseable(fileAccess);
        return fileAccess;
    }

    @Override
    public IDBasedAdministrativeFileAccess createAccess(int contextId) {
        return new CompositingIDBasedAdministrativeFileAccess(contextId, services);
    }

}

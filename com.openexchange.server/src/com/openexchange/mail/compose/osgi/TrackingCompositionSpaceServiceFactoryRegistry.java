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

package com.openexchange.mail.compose.osgi;

import java.util.Iterator;
import java.util.List;
import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.CompositionSpaceServiceFactoryRegistry;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link TrackingCompositionSpaceServiceFactoryRegistry} - Tracking registry for composition space service factories.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class TrackingCompositionSpaceServiceFactoryRegistry extends RankingAwareNearRegistryServiceTracker<CompositionSpaceServiceFactory> implements CompositionSpaceServiceFactoryRegistry {

    /**
     * Initializes a new {@link TrackingCompositionSpaceServiceFactoryRegistry}.
     */
    public TrackingCompositionSpaceServiceFactoryRegistry(BundleContext context) {
        super(context, CompositionSpaceServiceFactory.class);
    }

    @Override
    public CompositionSpaceServiceFactory getHighestRankedFactoryFor(Session session) throws OXException {
        for (CompositionSpaceServiceFactory compositionSpaceServiceFactory : this) {
            if (compositionSpaceServiceFactory.isEnabled(session)) {
                return compositionSpaceServiceFactory;
            }
        }
        throw ServiceExceptionCode.absentService(CompositionSpaceService.class);
    }

    @Override
    public List<CompositionSpaceServiceFactory> getFactoriesFor(Session session) throws OXException {
        List<CompositionSpaceServiceFactory> services = getServiceList();
        for (Iterator<CompositionSpaceServiceFactory> it = services.iterator(); it.hasNext();) {
            CompositionSpaceServiceFactory compositionSpaceServiceFactory = it.next();
            if (!compositionSpaceServiceFactory.isEnabled(session)) {
                it.remove();
            }
        }
        return services;
    }

    @Override
    public CompositionSpaceServiceFactory getFactoryFor(String serviceId, Session session) throws OXException {
        if (serviceId == null) {
            throw new IllegalArgumentException("Service identifier must not be null.");
        }
        List<CompositionSpaceServiceFactory> services = getServiceList();
        for (CompositionSpaceServiceFactory compositionSpaceServiceFactory : services) {
            if (serviceId.equals(compositionSpaceServiceFactory.getServiceId()) && compositionSpaceServiceFactory.isEnabled(session)) {
                return compositionSpaceServiceFactory;
            }
        }
        throw ServiceExceptionCode.absentService(CompositionSpaceService.class);
    }

}

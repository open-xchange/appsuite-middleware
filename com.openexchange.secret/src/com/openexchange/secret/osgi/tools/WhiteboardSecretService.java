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

package com.openexchange.secret.osgi.tools;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.secret.RankingAwareSecretService;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;

/**
 * {@link WhiteboardSecretService} - Whiteboard pattern for {@link SecretService}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WhiteboardSecretService implements RankingAwareSecretService {

    private final ServiceTracker<SecretService, SecretService> tracker;

    /**
     * Initializes a new {@link WhiteboardSecretService}.
     *
     * @param context The bundle context
     */
    public WhiteboardSecretService(final BundleContext context) {
        tracker = new ServiceTracker<SecretService, SecretService>(context, SecretService.class, null);
    }

    /**
     * Gets the ranking of the currently applicable {@link SecretService} reference.
     *
     * @return The currently applicable service ranking
     */
    @Override
    public int getRanking() {
        final ServiceReference<SecretService> reference = tracker.getServiceReference();
        if (null == reference) {
            return Integer.MIN_VALUE;
        }
        final Object ranking = reference.getProperty(org.osgi.framework.Constants.SERVICE_RANKING);
        return null == ranking ? Integer.MIN_VALUE : ((ranking instanceof Integer) ? ((Integer) ranking).intValue() : Integer.parseInt(ranking.toString().trim()));
    }

    @Override
    public String getSecret(final Session session) {
        /*-
         * If multiple services are being tracked, the service with the highest
         * ranking (as specified in its {@code service.ranking} property) is
         * returned. If there is a tie in ranking, the service with the lowest
         * service ID (as specified in its {@code service.id} property); that
         * is, the service that was registered first is returned. This is the same
         * algorithm used by {@code BundleContext.getServiceReference}.
         */
        final SecretService secretService = tracker.getService();
        if (secretService == null) {
            return null;
        }
        return secretService.getSecret(session);
    }

    /**
     * Opens this {@link WhiteboardSecretService} and starts tracking {@link SecretService}.
     */
    public void open() {
        tracker.open();
    }

    /**
     * Closes this {@link WhiteboardSecretService}.
     */
    public void close() {
        tracker.close();
    }

}

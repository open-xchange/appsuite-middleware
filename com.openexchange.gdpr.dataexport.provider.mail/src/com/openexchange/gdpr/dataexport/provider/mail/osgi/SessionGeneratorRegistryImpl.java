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

package com.openexchange.gdpr.dataexport.provider.mail.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.provider.mail.generator.SessionGenerator;
import com.openexchange.gdpr.dataexport.provider.mail.internal.SessionGeneratorRegistry;
import com.openexchange.java.Strings;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;

/**
 * {@link SessionGeneratorRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SessionGeneratorRegistryImpl extends RankingAwareNearRegistryServiceTracker<SessionGenerator> implements SessionGeneratorRegistry {

    /**
     * Initializes a new {@link SessionGeneratorRegistryImpl}.
     */
    public SessionGeneratorRegistryImpl(BundleContext context) {
        super(context, SessionGenerator.class);
    }

    @Override
    public SessionGenerator getGenerator(Session session) throws OXException {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }
        for (SessionGenerator generator : this) {
            if (generator.isApplicable(session)) {
                return generator;
            }
        }
        throw OXException.general("No suitable generator");
    }

    @Override
    public SessionGenerator getGeneratorById(String generatorId) throws OXException {
        if (Strings.isEmpty(generatorId)) {
            throw new IllegalArgumentException("Generator identifier must not be null or empty");
        }
        for (SessionGenerator generator : this) {
            if (generatorId.equals(generator.getId())) {
                return generator;
            }
        }
        throw OXException.general("No such generator: " + generatorId);
    }
}

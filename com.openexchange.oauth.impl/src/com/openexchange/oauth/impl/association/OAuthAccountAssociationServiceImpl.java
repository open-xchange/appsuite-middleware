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

package com.openexchange.oauth.impl.association;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.OAuthAccountAssociationService;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;

/**
 * {@link OAuthAccountAssociationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OAuthAccountAssociationServiceImpl extends RankingAwareNearRegistryServiceTracker<OAuthAccountAssociationProvider> implements OAuthAccountAssociationService {

    /**
     * Initializes a new {@link OAuthAccountAssociationServiceImpl}.
     */
    public OAuthAccountAssociationServiceImpl(BundleContext context) {
        super(context, OAuthAccountAssociationProvider.class);
    }

    @Override
    public Collection<OAuthAccountAssociation> getAssociationsFor(int accountId, Session session) throws OXException {
        Set<OAuthAccountAssociation> associations = new LinkedHashSet<>();
        for (OAuthAccountAssociationProvider provider : this) {
            associations.addAll(provider.getAssociationsFor(accountId, session));
        }
        return associations;
    }

}

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

package com.openexchange.sessionstorage.hazelcast;

import java.util.Map.Entry;
import com.hazelcast.query.Predicate;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;

/**
 * {@link AltIdPredicate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AltIdPredicate implements Predicate<String, PortableSession> {

    private static final long serialVersionUID = -3741029445819911943L;

    private String altId;

    /**
     * Initializes a new {@link AltIdPredicate}.
     *
     * @param altId The alternative ID to match
     */
    public AltIdPredicate(String altId) {
        super();
        this.altId = altId;
    }

    /**
     * Initializes a new {@link AltIdPredicate}.
     */
    public AltIdPredicate() {
        super();
    }

    @Override
    public boolean apply(Entry<String, PortableSession> mapEntry) {
        return null != mapEntry && null != mapEntry.getValue() && null != altId &&
            altId.equals(mapEntry.getValue().getParameter(Session.PARAM_ALTERNATIVE_ID));
    }

}

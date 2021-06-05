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

package com.openexchange.rest.client.session.pair;

import java.io.Serializable;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.exception.RESTExceptionCodes;

/**
 * {@link AbstractTokenPair}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractTokenPair implements Serializable {

    private static final long serialVersionUID = 6343407953200747797L;

    public final String key;

    public final String secret;

    /**
     * Initializes a new {@link AbstractTokenPair}.
     * 
     * @throws OXException
     */
    public AbstractTokenPair(final String key, final String secret) throws OXException {
        super();
        if (key == null) {
            throw RESTExceptionCodes.KEY_NULL.create();
        }
        if (key.contains("|")) {
            throw RESTExceptionCodes.ILLEGAL_CHARACTER.create("|", key);
        }
        if (secret == null) {
            throw RESTExceptionCodes.SECRET_NULL.create();
        }
        this.key = key;
        this.secret = secret;
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ (secret.hashCode() << 1);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractTokenPair && equals((AbstractTokenPair) o);
    }

    public boolean equals(AbstractTokenPair o) {
        return key.equals(o.key) && secret.equals(o.secret);
    }

    @Override
    public String toString() {
        return "{key=\"" + key + "\", secret=\"" + secret + "\"}";
    }
}

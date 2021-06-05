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

package com.openexchange.tokenlogin;

import java.util.Collections;
import java.util.Map;

/**
 * {@link DefaultTokenLoginSecret} - The default implementation for {@link TokenLoginSecret}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultTokenLoginSecret implements TokenLoginSecret {

    private String secret;
    private Map<String, Object> parameters;

    /**
     * Initializes a new {@link DefaultTokenLoginSecret}.
     */
    public DefaultTokenLoginSecret() {
        super();
    }

    @Override
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret
     *
     * @param secret The secret to set
     * @return This instance with new argument applied
     */
    public DefaultTokenLoginSecret setSecret(final String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public Map<String, Object> getParameters() {
        return null == parameters ? Collections.<String, Object> emptyMap() : parameters;
    }

    /**
     * Sets the parameters
     *
     * @param parameters The parameters to set
     * @return This instance with new argument applied
     */
    public DefaultTokenLoginSecret setParameters(final Map<String, Object> parameters) {
        this.parameters = Collections.unmodifiableMap(parameters);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("DefaultTokenLoginSecret [");
        if (secret != null) {
            builder.append("secret=").append(secret).append(", ");
        }
        if (parameters != null) {
            builder.append("parameters=").append(parameters);
        }
        builder.append("]");
        return builder.toString();
    }

}

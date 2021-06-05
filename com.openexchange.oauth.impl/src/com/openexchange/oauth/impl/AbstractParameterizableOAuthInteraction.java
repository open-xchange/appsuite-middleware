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

package com.openexchange.oauth.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.Parameterizable;


/**
 * {@link AbstractParameterizableOAuthInteraction} - Enhances {@link OAuthInteraction} by carrying parameters.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractParameterizableOAuthInteraction implements Parameterizable, OAuthInteraction {

    /** The parameters map */
    protected final ConcurrentMap<String, Object> parameters;

    /**
     * Initializes a new {@link AbstractParameterizableOAuthInteraction}.
     */
    protected AbstractParameterizableOAuthInteraction() {
        super();
        parameters = new ConcurrentHashMap<String, Object>(4, 0.9f, 1);
    }

    @Override
    public Set<String> getParamterNames() {
        return new LinkedHashSet<String>(parameters.keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getParameter(String name) {
        return (V) parameters.get(name);
    }

    @Override
    public void putParameter(String name, Object value) {
        if (null == value) {
            parameters.remove(name);
        } else {
            parameters.put(name, value);
        }
    }

}

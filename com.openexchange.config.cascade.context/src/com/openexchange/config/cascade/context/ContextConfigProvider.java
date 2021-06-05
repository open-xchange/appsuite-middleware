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

package com.openexchange.config.cascade.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ContextConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContextConfigProvider extends AbstractContextBasedConfigProvider {

    static final String DYNAMIC_ATTR_PREFIX = "config/";

    /**
     * Initializes a new {@link ContextConfigProvider}.
     *
     * @param services The service look-up
     */
    public ContextConfigProvider(ServiceLookup services) {
        super(services);
    }

    @Override
    public String getScope() {
    	return ConfigViewScope.CONTEXT.getScopeName();
    }

    @Override
    protected BasicProperty get(String propertyName, Context ctx, int user) throws OXException {
        return new BasicPropertyImpl(propertyName, ctx, services);
    }

    @Override
    protected Collection<String> getAllPropertyNamesFor(Context ctx, int userId) {
        Map<String, List<String>> attributes = ctx.getAttributes();
        Set<String> allNames = new HashSet<String>();

        String prefix = DYNAMIC_ATTR_PREFIX;
        int snip1 = prefix.length();
        for (String name : attributes.keySet()) {
            if (name.startsWith(prefix)) {
                allNames.add(name.substring(snip1));
            }
        }

        return allNames;
    }

}

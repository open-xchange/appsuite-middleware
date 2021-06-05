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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.ajax.requesthandler.AJAXActionCustomizer;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.AJAXResultDecoratorRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DecoratingAJAXActionCustomizer} - The {@link AJAXActionCustomizer customizer} applying {@link AJAXResultDecorator decorators}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DecoratingAJAXActionCustomizer implements AJAXActionCustomizer {

    /**
     * The reference for {@link AJAXResultDecoratorRegistry registry}.
     */
    static final AtomicReference<AJAXResultDecoratorRegistry> REGISTRY_REF = new AtomicReference<AJAXResultDecoratorRegistry>();

    private static final DecoratingAJAXActionCustomizer INSTANCE = new DecoratingAJAXActionCustomizer();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DecoratingAJAXActionCustomizer getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link DecoratingAJAXActionCustomizer}.
     */
    private DecoratingAJAXActionCustomizer() {
        super();
    }

    @Override
    public AJAXRequestData incoming(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        // Return unchanged
        return requestData;
    }

    @Override
    public AJAXRequestResult outgoing(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
        AJAXResultDecoratorRegistry registry = REGISTRY_REF.get();
        if (null == registry) {
            return result;
        }

        Collection<String> decoratorIds = requestData.getDecoratorIds();
        if (null == decoratorIds || decoratorIds.isEmpty()) {
            return result;
        }

        for (String decoratorId : decoratorIds) {
            AJAXResultDecorator decorator = registry.getDecorator(decoratorId);
            if (null != decorator && decorator.getFormat().equals(result.getFormat())) {
                decorator.decorate(requestData, result, session);
            }
        }
        return result;
    }

}

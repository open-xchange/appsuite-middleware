/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

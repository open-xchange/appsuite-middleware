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

package com.openexchange.sso.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link SSOActionFactory} - The action factory for single sign-on bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SSOActionFactory implements AJAXActionServiceFactory {

    private static final SSOActionFactory SINGLETON = new SSOActionFactory();

    /**
     * Gets the action factory instance.
     *
     * @return The action factory instance
     */
    public static final SSOActionFactory getInstance() {
        return SINGLETON;
    }

    /*-
     * ----------------------------------------- Member section -----------------------------------------
     */

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link SSOActionFactory}.
     */
    private SSOActionFactory() {
        super();
        actions = initActions();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        final AJAXActionService retval = actions.get(action);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        return retval;
    }

    private static Map<String, AJAXActionService> initActions() {
        final Map<String, AJAXActionService> tmp = new HashMap<String, AJAXActionService>(4);
        tmp.put(GetAction.ACTION, new GetAction());
        return Collections.unmodifiableMap(tmp);
    }

}

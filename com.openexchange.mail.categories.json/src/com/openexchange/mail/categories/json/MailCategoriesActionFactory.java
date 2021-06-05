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

package com.openexchange.mail.categories.json;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailCategoriesActionFactory}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesActionFactory implements AJAXActionServiceFactory {

    private static final AtomicReference<MailCategoriesActionFactory> INSTANCE_REFERENCE = new AtomicReference<MailCategoriesActionFactory>();

    public static MailCategoriesActionFactory initMailCategoriesActionFactory(final ServiceLookup services) {
        try {
        MailCategoriesActionFactory actionFactory = new MailCategoriesActionFactory(services);
        INSTANCE_REFERENCE.set(actionFactory);
        return actionFactory;
        } catch (Throwable t) {
            throw t;
        }
    }

    private final Map<String, AbstractCategoriesAction> actions;

    /**
     * Initializes a new {@link MailCategoriesActionFactory}.
     *
     * @param services The service look-up
     */
    private MailCategoriesActionFactory(final ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AbstractCategoriesAction> actions = ImmutableMap.builder();
        actions.put("unread", new UnreadAction(services));
        actions.put("train", new TrainAction(services));
        actions.put("move", new MoveAction(services));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) {
        return actions.get(action);
    }

}

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

package com.openexchange.tasks.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.actions.AllAction;
import com.openexchange.tasks.json.actions.ConfirmAction;
import com.openexchange.tasks.json.actions.CopyAction;
import com.openexchange.tasks.json.actions.DeleteAction;
import com.openexchange.tasks.json.actions.GetAction;
import com.openexchange.tasks.json.actions.ListAction;
import com.openexchange.tasks.json.actions.NewAction;
import com.openexchange.tasks.json.actions.SearchAction;
import com.openexchange.tasks.json.actions.TaskAction;
import com.openexchange.tasks.json.actions.UpdateAction;
import com.openexchange.tasks.json.actions.UpdatesAction;


/**
 * {@link TaskActionFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@SuppressWarnings("deprecation")
@OAuthModule
public class TaskActionFactory implements AJAXActionServiceFactory {

    public static final String MODULE = "tasks";

    @Deprecated
    public static final String OAUTH_READ_SCOPE = "read_tasks";

    @Deprecated
    public static final String OAUTH_WRITE_SCOPE = "write_tasks";

    private final Map<String, TaskAction> actions;

    /**
     * Initializes a new {@link TaskActionFactory}.
     */
    public TaskActionFactory(final ServiceLookup serviceLookup) {
        super();
        ImmutableMap.Builder<String, TaskAction> actions = ImmutableMap.builder();
        actions.put("all", new AllAction(serviceLookup));
        actions.put("confirm", new ConfirmAction(serviceLookup));
        actions.put("copy", new CopyAction(serviceLookup));
        actions.put("delete", new DeleteAction(serviceLookup));
        actions.put("get", new GetAction(serviceLookup));
        actions.put("list", new ListAction(serviceLookup));
        actions.put("new", new NewAction(serviceLookup));
        actions.put("search", new SearchAction(serviceLookup));
        actions.put("update", new UpdateAction(serviceLookup));
        actions.put("updates", new UpdatesAction(serviceLookup));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}

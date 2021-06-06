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

package com.openexchange.subscribe.json.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.json.SubscriptionSourceJSONWriter;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ListSourcesAction extends AbstractSubscribeSourcesAction {

    /**
     * Initializes a new {@link ListSourcesAction}.
     */
    public ListSourcesAction(ServiceLookup services) {
        super(services);
    }

    private static final String[] FIELDS = new String[] { "id", "displayName", "icon", "module", "formDescription" };

    private static final Set<String> IGNOREES = ImmutableSet.of("com.openexchange.subscribe.crawler.gmx");

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException {
        int module = getModule(subscribeRequest.getRequestData().getModule());
        // Retrieve subscription sources
        ServerSession session = subscribeRequest.getServerSession();
        List<SubscriptionSource> sources = new ArrayList<SubscriptionSource>(getAvailableSources(session).getSources(module));
        for (Iterator<SubscriptionSource> iterator = sources.iterator(); iterator.hasNext();) {
            final SubscriptionSource subscriptionSource = iterator.next();
            if (IGNOREES.contains(subscriptionSource.getId())) {
                iterator.remove();
            } else {
                SubscribeService subscribeService = subscriptionSource.getSubscribeService();
                if (false == subscribeService.isEnabled(session) || false == subscribeService.isCreateModifyEnabled()) {
                    iterator.remove();
                }
            }
        }
        // Generate appropriate JSON
        JSONArray json = new SubscriptionSourceJSONWriter(createTranslator(session)).writeJSONArray(sources, FIELDS);
        return new AJAXRequestResult(json, "json");
    }

    protected int getModule(String moduleAsString) {
        if (moduleAsString == null) {
            return -1;
        }
        if (moduleAsString.equals("contacts")) {
            return FolderObject.CONTACT;
        } else if (moduleAsString.equals("calendar")) {
            return FolderObject.CALENDAR;
        } else if (moduleAsString.equals("tasks")) {
            return FolderObject.TASK;
        } else if (moduleAsString.equals("infostore")) {
            return FolderObject.INFOSTORE;
        }
        return -1;
    }
}

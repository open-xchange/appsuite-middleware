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

package com.openexchange.chronos.itip.json;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.chronos.itip.ITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.json.actions.ActionPerformerAction;
import com.openexchange.chronos.itip.json.actions.AnalyzeAction;
import com.openexchange.chronos.itip.json.actions.ShowMailAction;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;


/**
 * 
 * {@link ITipActionFactory}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipActionFactory implements AJAXActionServiceFactory {

    public static ITipActionFactory INSTANCE = null;

    private final Map<String, AJAXActionService> actions = new HashMap<String, AJAXActionService>();

    public ITipActionFactory(final ServiceLookup services, RankingAwareNearRegistryServiceTracker<ITipAnalyzerService> analyzerListing, RankingAwareNearRegistryServiceTracker<ITipActionPerformerFactoryService> factoryListing) throws OXException {
        super();
        actions.put("analyze",  new AnalyzeAction(services, analyzerListing));
        actions.put("showMail", new ShowMailAction(services));
        final ActionPerformerAction dingeMacherAction = new ActionPerformerAction(services, analyzerListing, factoryListing);
        for (final String actionName : dingeMacherAction.getActionNames()) {
            actions.put(actionName, dingeMacherAction);
        }
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}

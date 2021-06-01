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

package com.openexchange.mail.filter.json.v2.actions;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.filter.json.v2.json.RuleParser;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MailFilterActionFactory}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class MailFilterActionFactory implements AJAXActionServiceFactory{

    public static final MailFilterActionFactory newInstance(RuleParser ruleParser, ServiceLookup services) {
        return new MailFilterActionFactory(ruleParser, services);
    }

    // -----------------------------------------------------------------------------------------------------

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link AdvertisementActionFactory}.
     */
    private MailFilterActionFactory(RuleParser ruleParser, ServiceLookup services) {
        super();
        actions = initActions(ruleParser, services);
    }

    private Map<String, AJAXActionService> initActions(RuleParser ruleParser, ServiceLookup services) {
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        tmp.put(NewMailFilterAction.ACTION.getAjaxName(), new NewMailFilterAction(ruleParser, services));
        tmp.put(UpdateMailFilterAction.ACTION.getAjaxName(), new UpdateMailFilterAction(ruleParser, services));
        tmp.put(ListMailFilterAction.ACTION.getAjaxName(), new ListMailFilterAction(ruleParser, services));
        tmp.put(ReorderMailFilterAction.ACTION.getAjaxName(), new ReorderMailFilterAction(services));
        tmp.put(DeleteMailFilterAction.ACTION.getAjaxName(), new DeleteMailFilterAction(services));

        tmp.put(ConfigMailFilterAction.ACTION.getAjaxName(), new ConfigMailFilterAction(services));

        tmp.put(GetScriptMailFilterAction.ACTION.getAjaxName(), new GetScriptMailFilterAction(services));
        tmp.put(DeleteScriptMailFilterAction.ACTION.getAjaxName(), new DeleteScriptMailFilterAction(services));
        tmp.put(ApplyMailFilterAction.ACTION.getAjaxName(), new ApplyMailFilterAction(ruleParser, services));

        return tmp.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        final AJAXActionService retval = actions.get(action);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
        }
        return retval;
    }
}

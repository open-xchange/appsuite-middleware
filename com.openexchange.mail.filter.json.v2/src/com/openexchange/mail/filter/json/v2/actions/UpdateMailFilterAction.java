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

import static com.openexchange.java.Autoboxing.I;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.filter.json.v2.Action;
import com.openexchange.mail.filter.json.v2.json.RuleParser;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link UpdateMailFilterAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class UpdateMailFilterAction extends AbstractMailFilterAction {

    public static final Action ACTION = Action.UPDATE;

    private final RuleParser ruleParser;

    /**
     * Initializes a new {@link UpdateMailFilterAction}.
     */
    public UpdateMailFilterAction(RuleParser ruleParser, ServiceLookup services) {
        super(services);
        this.ruleParser = ruleParser;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        final Credentials credentials = getCredentials(session, request);
        final MailFilterService mailFilterService = services.getService(MailFilterService.class);
        try {
            final JSONObject json = getJSONBody(request.getData());
            final int uid = getUniqueId(json).intValue();
            final Rule rule = mailFilterService.getFilterRule(credentials, uid);
            if (rule == null) {
                throw MailFilterExceptionCode.NO_SUCH_ID.create(I(uid), credentials.getRightUsername(), credentials.getContextString());
            }
            ruleParser.parse(rule, json, ServerSessionAdapter.valueOf(request.getSession()));
            mailFilterService.updateFilterRule(credentials, rule, uid);
            return new AJAXRequestResult();
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        } catch (SieveException e) {
            throw MailFilterExceptionCode.handleSieveException(e);
        }
    }

}

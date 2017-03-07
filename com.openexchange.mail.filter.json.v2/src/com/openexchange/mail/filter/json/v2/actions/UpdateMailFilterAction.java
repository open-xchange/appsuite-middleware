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

package com.openexchange.mail.filter.json.v2.actions;

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
        final Credentials credentials = new Credentials(session);
        final MailFilterService mailFilterService = services.getService(MailFilterService.class);
        try {
            final JSONObject json = getJSONBody(request.getData());
            final Integer uid = getUniqueId(json);
            final Rule rule = mailFilterService.getFilterRule(credentials, uid);
            if (rule == null) {
                throw MailFilterExceptionCode.NO_SUCH_ID.create(uid, credentials.getRightUsername(), credentials.getContextString());
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

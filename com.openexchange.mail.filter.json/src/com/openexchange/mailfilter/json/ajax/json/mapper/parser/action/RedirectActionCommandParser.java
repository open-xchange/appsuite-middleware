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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.AddressException;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.json.fields.GeneralField;
import com.openexchange.mailfilter.json.ajax.json.fields.RedirectActionField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ArgumentUtil;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.CommandParserJSONUtil;
import com.openexchange.mailfilter.json.osgi.Services;

/**
 * {@link RedirectActionCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RedirectActionCommandParser implements CommandParser<ActionCommand> {

    /**
     * Initialises a new {@link RedirectActionCommandParser}.
     */
    public RedirectActionCommandParser() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.mapper.parser.ActionCommandParser#parse(org.json.JSONObject)
     */
    @Override
    public ActionCommand parse(JSONObject jsonObject) throws JSONException, SieveException, OXException {
        String stringParam = CommandParserJSONUtil.getString(jsonObject, RedirectActionField.to.name(), Commands.REDIRECT.getCommandName());
        // Check for valid email address here:
        try {
            new QuotedInternetAddress(stringParam, true);
        } catch (final AddressException e) {
            throw MailFilterExceptionCode.INVALID_REDIRECT_ADDRESS.create(e, stringParam);
        }
        // And finally check of that forward address is allowed
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        final Filter filter;
        if (null != service && (null != (filter = service.getFilterFromProperty("com.openexchange.mail.filter.redirectWhitelist"))) && !filter.accepts(stringParam)) {
            throw MailFilterExceptionCode.REJECTED_REDIRECT_ADDRESS.create(stringParam);
        }

        ArrayList<Object> args = new ArrayList<Object>();
        Boolean keep = jsonObject.optBoolean(RedirectActionField.keep.name());
        if (keep) {
            args.add(ArgumentUtil.createTagArgument("copy"));
        }
        args.add(Collections.singletonList(stringParam));

        return new ActionCommand(Commands.REDIRECT, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.json.ajax.json.mapper.parser.ActionCommandParser#parse(org.json.JSONObject, com.openexchange.jsieve.commands.ActionCommand)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void parse(JSONObject jsonObject, ActionCommand actionCommand) throws JSONException, OXException {
        ArrayList<Object> arguments = actionCommand.getArguments();

        jsonObject.put(GeneralField.id.name(), Commands.REDIRECT.getJsonName());
        if (actionCommand.getTagArguments().isEmpty()) {
            jsonObject.put(RedirectActionField.to.name(), ((List<String>) arguments.get(0)).get(0));
        } else {
            jsonObject.put(RedirectActionField.keep.name(), true);
            jsonObject.put(RedirectActionField.to.name(), ((List<String>) arguments.get(1)).get(0));
        }
    }
}

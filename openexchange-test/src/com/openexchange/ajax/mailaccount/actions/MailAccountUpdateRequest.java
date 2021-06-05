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

package com.openexchange.ajax.mailaccount.actions;

import java.util.EnumSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.json.fields.GetSwitch;

/**
 * {@link MailAccountUpdateRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MailAccountUpdateRequest implements AJAXRequest<MailAccountUpdateResponse> {

    private final MailAccountDescription account;
    private final Set<Attribute> attributes;
    private final boolean failOnError;

    public MailAccountUpdateRequest(final MailAccountDescription account, final Set<Attribute> attributes, final boolean failOnError) {
        this.account = account;
        this.attributes = attributes;
        this.failOnError = failOnError;
    }

    public MailAccountUpdateRequest(final MailAccountDescription account, final boolean failOnError) {
        this(account, EnumSet.allOf(Attribute.class), failOnError);
    }

    public MailAccountUpdateRequest(final MailAccountDescription account, final Set<Attribute> attributes) {
        this(account, attributes, true);
    }

    public MailAccountUpdateRequest(final MailAccountDescription account) {
        this(account, true);
    }

    @Override
    public Object getBody() throws JSONException {
        try {
            final JSONObject incrementalUpdate = new JSONObject();
            final GetSwitch getter = new GetSwitch(account);
            for (final Attribute attribute : attributes) {
                incrementalUpdate.put(attribute.getName(), attribute.doSwitch(getter));
            }
            if (!attributes.contains(Attribute.ID_LITERAL)) {
                incrementalUpdate.put(Attribute.ID_LITERAL.getName(), account.getId());
            }

            return incrementalUpdate;
        } catch (OXException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[] { new Parameter("action", "update")
        };
    }

    @Override
    public AbstractAJAXParser<MailAccountUpdateResponse> getParser() {
        return new MailAccountUpdateParser(failOnError);
    }

    @Override
    public String getServletPath() {
        return "/ajax/account";
    }

}

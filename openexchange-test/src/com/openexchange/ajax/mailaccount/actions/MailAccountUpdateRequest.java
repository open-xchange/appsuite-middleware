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
public class MailAccountUpdateRequest implements AJAXRequest<MailAccountUpdateResponse>{

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
            for(final Attribute attribute : attributes) {
                incrementalUpdate.put(attribute.getName(), attribute.doSwitch(getter));
            }
            if(! attributes.contains(Attribute.ID_LITERAL)) {
                incrementalUpdate.put(Attribute.ID_LITERAL.getName(), account.getId());
            }

            return incrementalUpdate;
        } catch (final OXException e) {
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
        return new Parameter[]{
            new Parameter("action", "update")
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

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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonDeleteParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;
import com.openexchange.share.recipient.AnonymousRecipient;

/**
 * {@link UpdateRecipientRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateRecipientRequest implements AJAXRequest<AbstractAJAXResponse>{

    private final boolean failOnError;
    private final AnonymousRecipient recipient;
    private final int entity;

    /**
     * Initializes a new {@link UpdateRecipientRequest}.
     *
     * @param entity The guest entity to update with the recipient definition
     * @param recipient The recipient to update
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     */
    public UpdateRecipientRequest(int entity, AnonymousRecipient recipient, boolean failOnError) {
        super();
        this.entity = entity;
        this.failOnError = failOnError;
        this.recipient = recipient;
    }

    /**
     * Initializes a new {@link UpdateRecipientRequest}.
     *
     * @param entity The guest entity to update with the recipient definition
     * @param recipient The recipient to update
     */
    public UpdateRecipientRequest(int entity, AnonymousRecipient recipient) {
        this(entity, recipient, true);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public String getServletPath() {
        return "/ajax/share/management";
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        return new Params(
            AJAXServlet.PARAMETER_ACTION, "updateRecipient",
            "entity", String.valueOf(entity)
        ).toArray();
    }

    @Override
    public CommonDeleteParser getParser() {
        return new CommonDeleteParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", recipient.getType().toString().toLowerCase());
        jsonObject.put("password", recipient.getPassword());
        jsonObject.put("bits", recipient.getBits());
        return jsonObject;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[0];
    }

}

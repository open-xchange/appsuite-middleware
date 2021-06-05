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

import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link MailAccountGetRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailAccountGetRequest implements AJAXRequest<MailAccountGetResponse> {

    private final String id;
    private final boolean failOnError;

    public MailAccountGetRequest(int id) {
        this(id, true);
    }

    public MailAccountGetRequest(int id, boolean failOnError) {
        this.id = String.valueOf(id);
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.GET;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[] { new Parameter("id", id), new Parameter("action", "get")
        };
    }

    @Override
    public AbstractAJAXParser<MailAccountGetResponse> getParser() {
        return new MailAccountGetParser(failOnError);
    }

    @Override
    public String getServletPath() {
        return "/ajax/account";
    }

}

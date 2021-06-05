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

import org.json.JSONArray;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link MailAccountDeleteRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MailAccountDeleteRequest implements AJAXRequest<MailAccountDeleteResponse> {

    private final JSONArray ids = new JSONArray();
    private final boolean failOnError;

    public MailAccountDeleteRequest(boolean failOnError, int... ids) {
        this.failOnError = failOnError;
        for (int id : ids) {
            this.ids.put(id);
        }
    }

    public MailAccountDeleteRequest(int... ids) {
        this(true, ids);
    }

    @Override
    public Object getBody() {
        return ids;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[] { new Parameter("action", "delete")
        };
    }

    @Override
    public AbstractAJAXParser<MailAccountDeleteResponse> getParser() {
        return new MailAccountDeleteResponseParser(failOnError);
    }

    @Override
    public String getServletPath() {
        return "/ajax/account";
    }

}

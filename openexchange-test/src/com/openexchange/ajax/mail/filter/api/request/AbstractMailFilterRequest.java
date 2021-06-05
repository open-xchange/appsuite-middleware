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

package com.openexchange.ajax.mail.filter.api.request;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.mail.filter.api.conversion.writer.MailFilterWriter;
import com.openexchange.ajax.mail.filter.api.dao.Rule;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public abstract class AbstractMailFilterRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the calendar AJAX interface.
     */
    public static final String URL = "/ajax/mailfilter";

    protected boolean failOnError = true;

    protected AbstractMailFilterRequest() {
        super();
    }

    @Override
    public String getServletPath() {
        return URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    protected JSONObject convert(final Rule rule) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        final MailFilterWriter mailFilterWriter = new MailFilterWriter();
        mailFilterWriter.writeMailFilter(rule, jsonObj);
        return jsonObj;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
}

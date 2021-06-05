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

import java.util.LinkedList;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.json.parser.DefaultMailAccountParser;

/**
 * {@link MailAccountGetParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MailAccountGetParser extends AbstractAJAXParser<MailAccountGetResponse> {

    protected MailAccountGetParser(final boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected MailAccountGetResponse createResponse(final Response response) throws JSONException {
        final MailAccountGetResponse resp = new MailAccountGetResponse(response);
        final MailAccountDescription account = new MailAccountDescription();
        try {
            DefaultMailAccountParser.getInstance().parse(account, (JSONObject) response.getData(), new LinkedList<OXException>());
        } catch (OXException e) {
            throw new JSONException(e);
        }
        resp.setDescription(account);
        return resp;
    }

}

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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.mailaccount.MailAccountDescription;

/**
 * {@link MailAccountListParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MailAccountListParser extends AbstractAJAXParser<MailAccountListResponse> {

    private final int[] cols;

    protected MailAccountListParser(boolean failOnError, int[] cols) {
        super(failOnError);
        this.cols = cols;
    }

    @Override
    protected MailAccountListResponse createResponse(Response response) throws JSONException {
        MailAccountListResponse resp = new MailAccountListResponse(response);
        JSONArray arrayOfArrays = (JSONArray) resp.getData();
        List<MailAccountDescription> accounts = ParserTools.parseList(arrayOfArrays, cols);

        resp.setDescriptions(accounts);

        return resp;
    }

}

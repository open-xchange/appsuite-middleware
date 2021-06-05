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

package com.openexchange.ajax.mail.actions;

import org.json.JSONException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ForwardRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ForwardRequest extends ReplyRequest {

    public ForwardRequest(String folderID, String mailID) {
        super(folderID, mailID);
    }

    public ForwardRequest(String[] folderAndId) {
        this(folderAndId[0], folderAndId[1]);
    }

    @Override
    public String getAction() {
        return Mail.ACTION_FORWARD;
    }

    @Override
    public AbstractAJAXParser<? extends ReplyResponse> getParser() {
        return new AbstractAJAXParser<ForwardResponse>(failOnError) {

            @Override
            protected ForwardResponse createResponse(final Response response) throws JSONException {
                return new ForwardResponse(response);
            }
        };
    }

}

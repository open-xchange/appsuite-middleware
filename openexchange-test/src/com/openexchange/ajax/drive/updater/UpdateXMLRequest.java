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

package com.openexchange.ajax.drive.updater;

import java.io.IOException;
import org.json.JSONException;

/**
 * {@link UpdateXMLRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdateXMLRequest extends AbstractUpdaterRequest<UpdateXMLResponse> {

    /**
     * Initializes a new {@link UpdateXMLRequest}.
     * 
     * @param servletPath
     */
    public UpdateXMLRequest() {
        super("/ajax/drive/client/windows/v1/update.xml");
    }

    /**
     * @see com.openexchange.ajax.framework.AJAXRequest#getMethod()
     */
    @Override
    public Method getMethod() {
        return Method.GET;
    }

    /**
     * @see com.openexchange.ajax.framework.AJAXRequest#getParser()
     */
    @Override
    public UpdateXMLParser getParser() {
        return new UpdateXMLParser(true);
    }

    /**
     * @see com.openexchange.ajax.framework.AJAXRequest#getBody()
     */
    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}

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

package com.openexchange.ajax.attach.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.parser.AttachmentParser;
import com.openexchange.groupware.attach.AttachmentMetadata;

/**
 * {@link GetParser}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class GetParser extends AbstractAJAXParser<GetResponse> {

    protected GetParser(boolean failOnError) {
        super(failOnError);
    }

    //    {"data":{"creation_date":1264674332832,"atta
    //        ched":8937,"rtf_flag":false,"file_size":4,"file_mimetype":"text/plain","file_id":"00/0b/52","comment":"","filename":"test.txt","folder":31,"id":905,"module":4,"created_by":4},
    //        "timestamp":1264670732832}
    @Override
    protected GetResponse createResponse(Response response) throws JSONException {
        GetResponse retval = new GetResponse(response);
        AttachmentMetadata attachment = new AttachmentParser().getAttachmentMetadata(((JSONObject) response.getData()));
        retval.setAttachment(attachment);
        return retval;
    }

}

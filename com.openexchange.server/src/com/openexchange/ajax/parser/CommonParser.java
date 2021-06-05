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

package com.openexchange.ajax.parser;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CommonObject;

/**
 * CommonParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class CommonParser extends FolderChildParser {

    protected CommonParser() {
        super();
    }

    protected CommonParser(final TimeZone timeZone) {
        super(timeZone);
    }

    protected CommonParser(final boolean parseAll, final TimeZone timeZone) {
        super(parseAll, timeZone);
    }

    protected void parseElementCommon(final CommonObject obj, final JSONObject json) throws JSONException, OXException {
        if (json.has(CommonFields.CATEGORIES)) {
            obj.setCategories(parseString(json, CommonFields.CATEGORIES));
        }
        if (json.has(CommonFields.COLORLABEL)) {
            obj.setLabel(parseInt(json, CommonFields.COLORLABEL));
        }
        if (json.has(CommonFields.PRIVATE_FLAG)) {
            obj.setPrivateFlag(parseBoolean(json, CommonFields.PRIVATE_FLAG));
        }
        if (parseAll && json.has(CommonFields.NUMBER_OF_ATTACHMENTS)) {
            obj.setNumberOfAttachments(parseInt(json, CommonFields.NUMBER_OF_ATTACHMENTS));
        }
        if (parseAll && json.has(CommonFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC)) {
            obj.setLastModifiedOfNewestAttachment(parseDate(json, CommonFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC));
        }
        parseElementFolderChildObject(obj, json);
    }
}

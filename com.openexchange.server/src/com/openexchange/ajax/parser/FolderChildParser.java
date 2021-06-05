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
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderChildObject;

/**
 * FolderChildParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class FolderChildParser extends DataParser {

    protected FolderChildParser() {
        super();
    }

    protected FolderChildParser(final TimeZone timeZone) {
        super(timeZone);
    }

    protected FolderChildParser(final boolean parseAll, final TimeZone timeZone) {
        super(parseAll, timeZone);
    }

    protected void parseElementFolderChildObject(final FolderChildObject folderchildobject, final JSONObject jsonobject) throws JSONException, OXException {
		if (jsonobject.has(FolderChildFields.FOLDER_ID)) {
			folderchildobject.setParentFolderID(parseInt(jsonobject, FolderChildFields.FOLDER_ID));
		}

		parseElementDataObject(folderchildobject, jsonobject);
	}
}





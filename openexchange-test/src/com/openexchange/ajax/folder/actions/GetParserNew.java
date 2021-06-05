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

package com.openexchange.ajax.folder.actions;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.parser.FolderParser;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.java.util.TimeZones;

/**
 * {@link GetParserNew}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GetParserNew extends AbstractAJAXParser<GetResponseNew> implements ContentTypeDiscoveryService {

    private static final Map<String, ContentType> TYPES = new HashMap<String, ContentType>();

    static {
        TYPES.put(CalendarContentType.getInstance().toString(), CalendarContentType.getInstance());
        TYPES.put(ContactsContentType.getInstance().toString(), ContactsContentType.getInstance());
        TYPES.put(FileStorageContentType.getInstance().toString(), FileStorageContentType.getInstance());
        TYPES.put(InfostoreContentType.getInstance().toString(), InfostoreContentType.getInstance());
        TYPES.put(MailContentType.getInstance().toString(), MailContentType.getInstance());
        TYPES.put(TaskContentType.getInstance().toString(), TaskContentType.getInstance());
    }

    /**
     * Initializes a new {@link GetParserNew}.
     * 
     * @param failOnError
     */
    protected GetParserNew(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected GetResponseNew createResponse(Response response) throws JSONException {
        JSONObject data;
        Folder folder;
        if (response.hasError()) {
            data = null;
            folder = null;
        } else {
            data = (JSONObject) response.getData();
            try {
                folder = new FolderParser(this).parseFolder(data, TimeZones.UTC);
            } catch (OXException e) {
                throw new IllegalStateException("Could not parse folder response.", e);
            }
        }

        return new GetResponseNew(response, data, folder);
    }

    @Override
    public ContentType getByString(String contentTypeString) {
        return TYPES.get(contentTypeString);
    }

    @Override
    public ContentType getByModule(int module) {
        for (ContentType type : TYPES.values()) {
            if (type.getModule() == module) {
                return type;
            }
        }
        return null;
    }
}

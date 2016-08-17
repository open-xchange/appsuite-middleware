/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.folderstorage.database.contentType.ContactContentType;
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
        TYPES.put(ContactContentType.getInstance().toString(), ContactContentType.getInstance());
        TYPES.put(FileStorageContentType.getInstance().toString(), FileStorageContentType.getInstance());
        TYPES.put(InfostoreContentType.getInstance().toString(), InfostoreContentType.getInstance());
        TYPES.put(MailContentType.getInstance().toString(), MailContentType.getInstance());
        TYPES.put(TaskContentType.getInstance().toString(), TaskContentType.getInstance());
    }

    /**
     * Initializes a new {@link GetParserNew}.
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

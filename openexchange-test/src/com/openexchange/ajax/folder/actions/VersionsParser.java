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
 *    trademarks of the OX Software GmbH. group of companies.
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

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileFieldHandler;
import com.openexchange.file.storage.json.FileMetadataFieldParser;
import com.openexchange.file.storage.json.JsonFieldHandler;
import com.openexchange.file.storage.meta.FileFieldSet;

/**
 * {@link VersionsParser}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class VersionsParser extends AbstractAJAXParser<VersionsResponse> {

    private static final String DATA = "data";
    private final int[] columns;

    protected VersionsParser(boolean failOnError, int[] columns) {
        super(failOnError);
        Arrays.sort(columns);
        this.columns = columns;
    }

    @Override
    protected VersionsResponse createResponse(Response response) throws JSONException {
        FileFieldSet fileFieldSet = new FileFieldSet();

        VersionsResponse versionsResponse = new VersionsResponse(response);
        JSONObject json = ResponseWriter.getJSON(response);
        if (json.has(DATA)) {
            JSONArray versions = (JSONArray) json.get(DATA);
            for (int i = 0; i < versions.length(); i++) {
                DefaultFile metadata = new DefaultFile();
                
                int columncount = 0;
                for (int column : columns) {
                    Field field = Field.get(column);
                    if (null != field) {
                        Object orig = ((JSONArray)versions.get(i)).get(columncount);
                        Object converted;
                        try {
                            converted = FileMetadataFieldParser.convert(field, orig);
                            field.doSwitch(fileFieldSet, metadata, converted);
                        } catch (OXException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        columncount++;
                    }
                }
                
                versionsResponse.addVersion(metadata);
            }
        }
        return versionsResponse;
    }
    
    private static FileFieldHandler getJsonHandler(final File file, final JsonFieldHandler fieldHandler) {
        return new AbstractFileFieldHandler() {
            @Override
            public Object handle(Field field, Object... args) {
                JSONObject jsonObject = get(0, JSONObject.class, args);
                try {
                    jsonObject.put(field.getName(), fieldHandler.handle(field, file));
                } catch (JSONException e) {
                    org.slf4j.LoggerFactory.getLogger(VersionsParser.class);
                    }
                return jsonObject;
            }
        };
    }
}

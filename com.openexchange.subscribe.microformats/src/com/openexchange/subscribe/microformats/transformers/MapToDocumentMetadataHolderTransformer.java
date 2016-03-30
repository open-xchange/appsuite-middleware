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

package com.openexchange.subscribe.microformats.transformers;

import static com.openexchange.groupware.infostore.utils.Metadata.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.subscribe.helpers.DocumentMetadataHolder;


/**
 * {@link MapToDocumentMetadataHolderTransformer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MapToDocumentMetadataHolderTransformer implements MapToObjectTransformer {
    
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd H:m:s.S z");
    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final Map<String, Metadata> mapping = new HashMap<String, Metadata>() {{
        put("ox_lastModified", LAST_MODIFIED_LITERAL);
        put("ox_creationDate", CREATION_DATE_LITERAL);
        put("ox_title", TITLE_LITERAL);
        put("ox_version", VERSION_LITERAL);
        put("ox_fileName", FILENAME_LITERAL);
        put("ox_fileSize", FILE_SIZE_LITERAL);
        put("ox_MIMEType", FILE_MIMETYPE_LITERAL);
        put("ox_comment", DESCRIPTION_LITERAL);
        put("ox_url", URL_LITERAL);
        put("ox_tags", CATEGORIES_LITERAL);
        put("ox_versionComment", VERSION_COMMENT_LITERAL);
    }};


    @Override
    public List<? extends Object> transform(List<Map<String, String>> list) throws OXException {
        List<DocumentMetadataHolder> documents = new ArrayList<DocumentMetadataHolder>(list.size());

        for(Map<String, String> attributes : list) {
            DocumentMetadataImpl metadata = new DocumentMetadataImpl();
            SetSwitch setter = new SetSwitch(metadata);
            for(Map.Entry<String, String> entry : attributes.entrySet()) {
                Metadata field = mapping.get(entry.getKey());
                if(field != null) {
                    Object value = transform(field, entry.getValue());
                    if(value != null) {
                        setter.setValue(value);
                        field.doSwitch(setter);
                    }
                }
            }

            String fileURL = attributes.get("ox_file");

            documents.add(new DocumentMetadataHolder(fileURL, metadata));
        }

        return documents;
    }


    private Object transform(Metadata field, String value) {
        try {
            switch(field.getId()) {
            default : return value;
            case CREATION_DATE : case LAST_MODIFIED:
                synchronized (TIME_FORMAT) {
                    return TIME_FORMAT.parse(value);
                }
            case VERSION: return Integer.valueOf(value);
            case FILE_SIZE:
                return Long.valueOf(value.replaceAll("[,.]", ""));
            }
        } catch (ParseException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }


}

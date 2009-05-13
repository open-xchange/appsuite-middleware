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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.json;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.json.FormContentWriter;
import com.openexchange.datatypes.genericonf.json.ValueWriterSwitch;
import com.openexchange.publish.Publication;

import static com.openexchange.publish.json.FieldNames.*;


/**
 * {@link PublicationWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationWriter {

   
    private static final FormContentWriter formWriter = new FormContentWriter();
    private static final ValueWriterSwitch valueWrite = new ValueWriterSwitch();

    public JSONObject write(Publication publication) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(ID, publication.getId());
        object.put(ENTITY_ID, publication.getEntityId());
        object.put(ENTITY_MODULE, publication.getModule());
        object.put(URL, publication.getUrl());
        String targetId = publication.getTarget().getId();
        object.put(TARGET, targetId);
        object.put(targetId, formWriter.write(publication.getTarget().getFormDescription(), publication.getConfiguration()));
        return object;
    }

    public JSONArray writeArray(Publication publication, String[] basicCols, Map<String, String[]> specialCols, List<String> specialsList, DynamicFormDescription form) {
        JSONArray array = new JSONArray();
        writeBasicCols(array, publication, basicCols);
        for(String identifier : specialsList) {
            writeSpecialCols(array, publication, specialCols.get(identifier), identifier, form);
        }
        return array;
    }

    private void writeSpecialCols(JSONArray array, Publication publication, String[] strings, String externalId, DynamicFormDescription form) {
        boolean writeNulls = !publication.getTarget().getId().equals(externalId);
        Map<String, Object> configuration  = publication.getConfiguration();
        for(String col : strings) {
            if(writeNulls) {
                array.put(JSONObject.NULL);
            } else {
                Object value = configuration.get(col);
                FormElement field = form.getField(col);
                value = field.doSwitch(valueWrite, value);
                array.put(value);
            }
        }
    }

    private void writeBasicCols(JSONArray array,Publication publication, String[] basicCols) {
        for(String basicCol : basicCols) {
            if(ID.equals(basicCol)) {
                array.put(publication.getId());
            } else if ( ENTITY_ID.equals(basicCol)) {
                array.put(publication.getEntityId());
            } else if ( ENTITY_MODULE.equals(basicCol)) {
                array.put(publication.getModule());
            } else if ( URL.equals(basicCol) ) {
                array.put(publication.getUrl());
            } else if ( TARGET.equals(basicCol) ) {
                array.put(publication.getTarget().getId());
            }
        }
    }

}

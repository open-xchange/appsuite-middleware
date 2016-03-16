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

package com.openexchange.subscribe.json;

import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.JSONEXCEPTION;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_FIELD;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_FORM_FIELD;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.i18n.Translator;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SubscriptionSourceJSONWriter implements SubscriptionSourceJSONWriterInterface {

    public static final int CLASS_ID = 2;

    private final Translator translator;

    private final FormDescriptionWriter formWriter;

    public SubscriptionSourceJSONWriter(final Translator translator) {
        super();
        this.translator = translator;
        formWriter = new FormDescriptionWriter(translator);

    }

    @Override
    public JSONObject writeJSON(final SubscriptionSource source) throws OXException {
        validate(source);
        JSONObject retval = null;
        try {
            retval = parse(source);
        } catch (final JSONException e) {
            throw JSONEXCEPTION.create(e);
        }
        return retval;
    }

    @Override
    public JSONArray writeJSONArray(final List<SubscriptionSource> sourceList, final String[] fields) throws OXException {
        final JSONArray retval = new JSONArray();
        for (final SubscriptionSource source : sourceList) {
            final JSONArray row = new JSONArray();
            for(final String field : fields) {
                if(ID.equals(field)) {
                    row.put(source.getId());
                } else if (DISPLAY_NAME.equals(field)) {
                    String displayName = source.getDisplayName();
                    if (source.isLocalizableDisplayName() && translator != null) {
                        displayName = translator.translate(displayName);
                    }

                    row.put(displayName);
                } else if (ICON.equals(field)) {
                    row.put(source.getIcon());
                } else if (FORM_DESCRIPTION.equals(field)) {
                    try {
                        row.put(formWriter.write(source.getFormDescription()));
                    } catch (final JSONException e) {
                        throw JSONEXCEPTION.create(e);
                    }
                } else if (MODULE.equals(field)) {
                    row.put(getModuleAsString(source));
                } else {
                    throw SubscriptionJSONErrorMessages.UNKNOWN_COLUMN.create(field);
                }
            }
            retval.put(row);
        }
        return retval;
    }

    private JSONObject parse(final SubscriptionSource source) throws JSONException {
        final JSONObject retval = new JSONObject();

        retval.put(ID, source.getId());
        retval.put(DISPLAY_NAME, source.getDisplayName());
        retval.put(FORM_DESCRIPTION,  formWriter.write(source.getFormDescription()));
        if (source.getIcon() != null) {
            retval.put(ICON, source.getIcon());
        }
        retval.put(MODULE, getModuleAsString(source));

        return retval;
    }

    private String getModuleAsString(final SubscriptionSource source) {
        final int module = source.getFolderModule();
        switch(module) {
        case FolderObject.CONTACT : return "contacts";
        case FolderObject.CALENDAR : return "calendar";
        case FolderObject.TASK : return "tasks";
        case FolderObject.INFOSTORE: return "infostore";
        default : return null;
        }
    }


    private void validate(final SubscriptionSource source) throws OXException {
        final List<String> missingFields = new ArrayList<String>();
        if (source.getId() == null) {
            missingFields.add(ID);
        }
        if (source.getDisplayName() == null) {
            missingFields.add(DISPLAY_NAME);
        }
        if (source.getFormDescription() == null) {
            missingFields.add(FORM_DESCRIPTION);
        }
        if (missingFields.size() > 0) {
            throw MISSING_FIELD.create(buildStringList(missingFields,", "));
        }

        for (final Iterator<FormElement> iter = source.getFormDescription().iterator(); iter.hasNext();) {
            final FormElement element = iter.next();
            final List<String> missingFormFields = new ArrayList<String>();
            if (element.getName() == null) {
                missingFormFields.add(NAME);
            }
            if (element.getDisplayName() == null) {
                missingFormFields.add(DISPLAY_NAME);
            }
            if (element.getWidget() == null) {
                missingFormFields.add(WIDGET);
            }
            // TODO: check for mandatory field "mandatory"
            if (missingFormFields.size() > 0) {
                throw MISSING_FORM_FIELD.create(buildStringList(missingFormFields, ", "));
            }
        }
    }

    private String buildStringList(final List<String> strings, final String delimiter) {
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<String> iter = strings.iterator(); iter.hasNext();) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}

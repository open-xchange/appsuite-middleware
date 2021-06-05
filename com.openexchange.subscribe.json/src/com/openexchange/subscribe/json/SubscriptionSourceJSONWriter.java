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
        } catch (JSONException e) {
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
                if (ID.equals(field)) {
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
                    } catch (JSONException e) {
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
        switch (module) {
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

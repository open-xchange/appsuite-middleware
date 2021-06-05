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

package com.openexchange.datatypes.genericonf.json;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.FormElement.Widget;
import com.openexchange.i18n.Translator;

/**
 * {@link FormDescriptionWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FormDescriptionWriter {

    public static final String WIDGET = "widget";

    public static final String NAME = "name";

    public static final String DISPLAY_NAME = "displayName";

    private static final String MANDATORY = "mandatory";

    private static final String OPTIONS = "options";

    private static final String DEFAULT_VALUE = "defaultValue";

    private static final ValueWriterSwitch valueWrite = new ValueWriterSwitch();

    private final Translator translator;

    public FormDescriptionWriter(Translator translator) {
        super();
        this.translator = null == translator ? Translator.EMPTY : translator;
    }

    public FormDescriptionWriter() {
        this(Translator.EMPTY);
    }

    public JSONArray write(DynamicFormDescription form) throws JSONException {
        JSONArray formDescriptionArray = new JSONArray();
        for (FormElement formElement : form) {
            JSONObject formElementObject = write(formElement);
            formDescriptionArray.put(formElementObject);
        }
        return formDescriptionArray;
    }

    public JSONObject write(FormElement formElement) throws JSONException {
        JSONObject object = new JSONObject();
        if (formElement.getWidget() != Widget.CUSTOM) {
            object.put(WIDGET, formElement.getWidget().getKeyword());
        } else {
            object.put(WIDGET, formElement.getCustomWidget());
        }

        Map<String, String> options = formElement.getOptions();
        if (options != null && !options.isEmpty()) {
            JSONObject jsonOptions = new JSONObject();
            for (Map.Entry<String, String> entry : options.entrySet())  {
                jsonOptions.put(entry.getKey(),  entry.getValue());
            }
            object.put(OPTIONS, jsonOptions);
        }

        object.put(NAME, formElement.getName());
        object.put(DISPLAY_NAME, translator.translate(formElement.getDisplayName()));
        object.put(MANDATORY, formElement.isMandatory());
        if (null != formElement.getDefaultValue()) {
            object.put(DEFAULT_VALUE, formElement.getWidget().doSwitch(valueWrite, formElement.getDefaultValue()));
        }
        return object;
    }
}

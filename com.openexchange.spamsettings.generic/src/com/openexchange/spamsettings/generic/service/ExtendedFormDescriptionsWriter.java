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


package com.openexchange.spamsettings.generic.service;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.FormElement.Widget;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.datatypes.genericonf.json.ValueWriterSwitch;
import com.openexchange.i18n.Translator;


public class ExtendedFormDescriptionsWriter extends FormDescriptionWriter {

    private static final String OPTIONS = "options";

    private static final String MANDATORY = "mandatory";

    private static final String DEFAULT_VALUE = "defaultValue";

    private static final ValueWriterSwitch valueWrite = new ValueWriterSwitch();

    private final Translator translator;

    public ExtendedFormDescriptionsWriter(final Translator translator) {
        super(translator);
        this.translator = translator;
    }

    @Override
    public JSONArray write(final DynamicFormDescription form) throws JSONException {
        final JSONArray formDescriptionArray = new JSONArray();
        for (final FormElement formElement : form) {
            final JSONObject formElementObject;
            if (formElement instanceof ExtendedFormElement) {
                formElementObject = write((ExtendedFormElement)formElement);
            } else {
                formElementObject = write(formElement);
            }
            formDescriptionArray.put(formElementObject);
        }
        return formDescriptionArray;
    }

    public JSONObject write(final ExtendedFormElement formElement) throws JSONException {
        final JSONObject object = new JSONObject();
        if (formElement.getWidget() != Widget.CUSTOM) {
            object.put(WIDGET, formElement.getWidget().getKeyword());
        } else {
            object.put(WIDGET, formElement.getCustomWidget());
        }

        final Map<Integer, String> options = formElement.getOptionsNew();
        if(options != null && !options.isEmpty()) {
            final JSONObject jsonOptions = new JSONObject();

            final JSONArray keys = new JSONArray();
            final JSONArray values = new JSONArray();

            for (final Map.Entry<Integer, String> entry : options.entrySet())  {
                keys.put(entry.getKey().intValue());
                values.put(entry.getValue());
            }
            jsonOptions.putOpt("keys", keys);
            jsonOptions.put("values", values);
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

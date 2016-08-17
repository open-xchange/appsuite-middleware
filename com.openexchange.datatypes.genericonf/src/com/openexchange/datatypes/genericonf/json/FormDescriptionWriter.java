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
        if(options != null && !options.isEmpty()) {
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

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

package com.openexchange.subscribe.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.subscribe.FormElement;
import com.openexchange.subscribe.SubscriptionFormDescription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SubscriptionSourceParser {

    public static final String ID = "id";

    public static final String DISPLAY_NAME = "displayName";

    public static final String ICON = "icon";

    public static final String FORM_DESCRIPTION = "formDescription";

    public static final String NAME = "name";

    public static final String WIDGET = "widget";

    public static final String MANDATORY = "mandatory";

    public static final String DEFAULT = "default";

    public JSONObject parseSubscriptionSource(SubscriptionSource source) throws ParseException {
        validate(source);
        JSONObject retval = null;
        try {
            retval = parse(source);
        } catch (JSONException e) {
            throw new ParseException(e);
        }
        return retval;
    }

    public JSONArray parseSubscriptionSources(List<SubscriptionSource> sourceList) throws ParseException {
        JSONArray retval = new JSONArray();
        for (Iterator<SubscriptionSource> iter = sourceList.iterator(); iter.hasNext();) {
            retval.put(parseSubscriptionSource(iter.next()));
        }
        return retval;
    }
    
    private JSONObject parse(SubscriptionSource source) throws JSONException {
        JSONObject retval = new JSONObject();

        retval.put(ID, source.getId());
        retval.put(DISPLAY_NAME, source.getDisplayName());
        retval.put(FORM_DESCRIPTION, parseFormElements(source.getFormDescription()));
        if (source.getIcon() != null) {
            retval.put(ICON, source.getIcon());
        }

        return retval;
    }

    private JSONArray parseFormElements(SubscriptionFormDescription formDescription) throws JSONException {
        JSONArray retval = new JSONArray();
        for (Iterator<FormElement> iter = formDescription.iterator(); iter.hasNext();) {
            FormElement element = iter.next();
            JSONObject jsonElement = new JSONObject();
            jsonElement.put(NAME, element.getName());
            jsonElement.put(DISPLAY_NAME, element.getDisplayName());
            jsonElement.put(WIDGET, element.getWidget().getKeyword());
            jsonElement.put(MANDATORY, element.isMandatory());
            if (element.getDefaultValue() != null) {
                jsonElement.put(DEFAULT, element.getDefaultValue());
            }
            retval.put(jsonElement);
        }
        return retval;
    }

    private void validate(SubscriptionSource source) throws ParseException {
        List<String> missingFields = new ArrayList<String>();
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
            throw new ParseException("Missing field(s): " + buildStringList(missingFields, ", "));
        }

        for (Iterator<FormElement> iter = source.getFormDescription().iterator(); iter.hasNext();) {
            FormElement element = iter.next();
            List<String> missingFormFields = new ArrayList<String>();
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
                throw new ParseException("Missing form field(s): " + buildStringList(missingFormFields, ", "));
            }
        }
    }

    private String buildStringList(List<String> strings, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iter = strings.iterator(); iter.hasNext();) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}

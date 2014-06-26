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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.json.convert;

import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.mobilenotifier.NotifyTemplate;

/**
 * {@link NotifyTemplateWriter} - Converts a notification template to a JSON structure.
 * 
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class NotifyTemplateWriter {

    private NotifyTemplateWriter() {
        super();
    }

    /**
     * Writes the JSON structure of notify templates
     * 
     * @param template The notify template
     * @return The JSON structure
     * @throws JSONException
     */
    public static JSONObject write(final NotifyTemplate template) throws JSONException {
        final JSONObject attributes = new JSONObject();
        attributes.put(MobileNotifyField.TITLE, template.getTitle());
        attributes.put(MobileNotifyField.TEMPLATE, template.getHtmlTemplate());
        attributes.put(MobileNotifyField.SLOW, template.isSlow());
        attributes.put(MobileNotifyField.INDEX, template.getIndex());

        // writes additional attributes
        final Iterator<Entry<String, Object>> iter = template.getAttributes().entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<String, Object> additionalAttribute = iter.next();
            attributes.put(additionalAttribute.getKey(), additionalAttribute.getValue());
        }
        return attributes;
    }
}

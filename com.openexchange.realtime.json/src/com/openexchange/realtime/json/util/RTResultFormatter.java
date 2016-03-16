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

package com.openexchange.realtime.json.util;

import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.realtime.json.actions.RTAction;


/**
 * {@link RTResultFormatter} Format a Realtime JSON Result.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTResultFormatter {

    @SuppressWarnings("unchecked")
    public static String format(Map<String, Object> resultMap) {
        StringBuilder formatter = new StringBuilder(1024);

        formatter.append("Result: {").append("\n");

        List<Long> acknowledgements = (List<Long>) resultMap.get(RTAction.ACKS);
        formatter.append("\tacks:").append("\n");
        if(acknowledgements == null) {
            formatter.append("\t\t[]").append("\n");
        } else {
            formatter.append("\t\t").append(acknowledgements.toString()).append("\n");
        }

        JSONObject error = (JSONObject) resultMap.get(RTAction.ERROR);
        formatter.append("\terror:").append("\n");
        if(error == null) {
            formatter.append("\t\t").append("none").append("\n");
        } else {
            formatter.append("\t\t").append(shortenOutput(error.toString())).append("\n");
        }

        JSONObject result = (JSONObject) resultMap.get(RTAction.RESULT);
        formatter.append("\tresult:").append("\n");
        if(result == null) {
            formatter.append("\t\t").append("none").append("\n");
        } else {
            formatter.append("\t\t").append(shortenOutput(result.toString())).append("\n");
        }

        List<JSONObject> stanzas = (List<JSONObject>) resultMap.get(RTAction.STANZAS);
        formatter.append("\tstanzas:").append("\n");
        if(stanzas == null || stanzas.isEmpty()) {
            formatter.append("\t\t{}").append("\n");
        } else {
            for (JSONObject stanza : stanzas) {
                formatter.append("\t\t").append(shortenOutput(stanza.toString())).append("\n");
            }
        }

        formatter.append("}").append("\n");

        return formatter.toString();
    }

    /**
     * Shorten given String to 500 characters and add '...' as ellipsis.
     * 
     * @param input the input string
     * @return the maybe shortened input string
     */
    public static String shortenOutput(String input) {
        if (input.length() > 500) {
            return input.substring(0, 500) + "...";
        } else {
            return input;
        }
    }
}

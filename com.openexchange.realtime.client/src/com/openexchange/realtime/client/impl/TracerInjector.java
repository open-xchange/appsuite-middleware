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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.client.impl;

import java.util.UUID;
import org.apache.commons.lang.Validate;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.realtime.client.impl.config.ConfigurationProvider;


/**
 * {@link TracerInjector}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class TracerInjector {

    /**
     * Decides based on the structure of the message and client configuration if it can and should be extended with a tracer and adds a
     * generated UUID to the message.
     *
     * @param json the message to extend with a tracer key
     * @return the extended message
     * @throws JSONException
     */
    public static JSONObject injectTracer(JSONObject json) {
        return injectTracer(json, null);
    }

    /**
     * Decides based on the structure of the message and client configuration if it can and should be extended with a tracer and adds either
     * the given tracer or a generated UUID to the message.
     *
     * @param json the message to extend with a tracer key
     * @param tracer An optional tracer that identifies the message on it's way through the server backend
     * @return the extended message
     * @throws JSONException
     */
    public static JSONObject injectTracer(JSONObject json, String tracer) {
        if(!ConfigurationProvider.getInstance().isTraceEnabled()) {
            return json;
        }
        Validate.notNull(json, "json must not be null");
        if(shouldInject(json)) {
            if(tracer == null) {
                tracer = UUID.randomUUID().toString();
            }
            try {
                json.put("tracer", tracer);
            } catch (JSONException e) {
                //can't happen
            }
        }
        return json;
    }

    private static boolean shouldInject(JSONObject json) {
        if(isPing(json) || isAck(json)) {
            return false;
        }
        return true;
    }

    /*
     * {"type": "ping", "commit": true }
     */
    private static boolean isPing(JSONObject json) {
        if(json.has("type") && json.optString("type").equalsIgnoreCase("ping")) {
            return true;
        }
        return false;
    }

    /*
     * {"type":"ack","seq":["4"]}
     */
    private static boolean isAck(JSONObject json) {
        if(json.has("type") && json.optString("type").equalsIgnoreCase("ack")) {
            return true;
        }
        return false;
    }
}

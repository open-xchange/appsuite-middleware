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

package com.openexchange.mail.smal.impl.adapter.elasticsearch;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link Mapping} - See <a href="http://www.elasticsearch.org/guide/reference/mapping/core-types.html">ElasticSearch Core Types</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Mapping {

    /**
     * Initializes a new {@link Mapping}.
     */
    private Mapping() {
        super();
    }

    /**
     * The JSON mail mappings.
     */
    public static final String JSON_MAPPINGS;

    static {
        try {
            final JSONObject properties = new JSONObject();
            properties.put(Constants.FIELD_TIMESTAMP, new JSONObject("{ \"type\": \"long\", \"index\": \"not_analyzed\", \"null_value\": 0 }"));
            properties.put(Constants.FIELD_UUID, new JSONObject("{ \"type\": \"string\", \"index\": \"not_analyzed\" }"));
            properties.put(Constants.FIELD_USER_ID, new JSONObject("{ \"type\": \"long\", \"index\": \"not_analyzed\", \"null_value\": 0 }"));
            properties.put(Constants.FIELD_ACCOUNT_ID, new JSONObject("{ \"type\": \"integer\", \"index\": \"not_analyzed\", \"null_value\": 0 }"));
            properties.put(Constants.FIELD_FULL_NAME, new JSONObject("{ \"type\": \"string\", \"index\": \"not_analyzed\" }"));
            properties.put(Constants.FIELD_ID, new JSONObject("{ \"type\": \"string\", \"index\": \"not_analyzed\" }"));
            /*
             * Body content
             */
            properties.put(Constants.FIELD_BODY, new JSONObject("{ \"type\": \"string\", \"store\": \"no\" }"));
            /*
             * Subject
             */
            properties.put(Constants.FIELD_SUBJECT, new JSONObject("{ \"type\": \"string\", \"store\": \"yes\" }"));
            /*
             * Address field
             */
            properties.put(Constants.FIELD_FROM, new JSONObject("{ \"type\": \"string\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_TO, new JSONObject("{ \"type\": \"string\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_CC, new JSONObject("{ \"type\": \"string\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_BCC, new JSONObject("{ \"type\": \"string\", \"store\": \"yes\" }"));
            /*
             * Flag fields
             */
            properties.put(Constants.FIELD_FLAG_ANSWERED, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_DELETED, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_DRAFT, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_FLAGGED, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_RECENT, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_SEEN, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_USER, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_SPAM, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_FORWARDED, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            properties.put(Constants.FIELD_FLAG_READ_ACK, new JSONObject("{ \"type\": \"boolean\", \"store\": \"yes\" }"));
            /*
             * Date fields
             */
            properties.put(Constants.FIELD_RECEIVED_DATE, new JSONObject("{ \"type\": \"long\", \"store\": \"yes\", \"null_value\": 0 }"));
            properties.put(Constants.FIELD_SENT_DATE, new JSONObject("{ \"type\": \"long\", \"store\": \"yes\", \"null_value\": 0 }"));
            /*
             * Size
             */
            properties.put(Constants.FIELD_SIZE, new JSONObject("{ \"type\": \"long\", \"store\": \"yes\", \"null_value\": 0 }"));
            /*
             * Put mapping
             */
            final JSONObject container = new JSONObject();
            container.put("properties", properties);
            final JSONObject mapping = new JSONObject();
            mapping.put(Constants.INDEX_TYPE, container);
            JSON_MAPPINGS = mapping.toString(2);
        } catch (final JSONException e) {
            throw new Error("Initialization failed", e);
        }
    }

}

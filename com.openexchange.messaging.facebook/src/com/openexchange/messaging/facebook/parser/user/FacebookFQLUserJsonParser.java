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

package com.openexchange.messaging.facebook.parser.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.facebook.utility.FacebookUser;

/**
 * {@link FacebookFQLUserJsonParser} - Parses a given Facebook user element.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookFQLUserJsonParser {

    private interface ItemHandler {

        void handleItem(JSONObject userInformation, FacebookUser user) throws OXException, JSONException;
    }

    private static final Map<String, ItemHandler> ITEM_HANDLERS;

    static {
        {
            final Map<String, ItemHandler> m = new HashMap<String, ItemHandler>();

            m.put("uid", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject userInformation, final FacebookUser user) throws OXException {
                    user.setUid(userInformation.optLong("uid", -1L));
                }
            });

            m.put("name", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject userInformation, final FacebookUser user) throws OXException {
                    user.setName(userInformation.optString("name", null));
                }
            });

            m.put("pic_small", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject userInformation, final FacebookUser user) throws OXException {
                    user.setPicSmall(userInformation.optString("pic_small", null));
                }
            });

            /*
             * TODO: Add other useful item handler
             */

            ITEM_HANDLERS = Collections.unmodifiableMap(m);
        }
    }

    /**
     * Initializes a new {@link FacebookFQLUserJsonParser}.
     */
    private FacebookFQLUserJsonParser() {
        super();
    }

    /**
     * Parses given Facebook user element into a user.
     *
     * @param userElement The Facebook user element
     * @return The resulting user
     * @throws OXException If parsing fails
     */
    public static FacebookUser parseUserJsonElement(final JSONObject userElement) throws OXException {
        if (null == userElement || 0 == userElement.length()) {
            return null;
        }
        try {
            final FacebookUser user = new FacebookUser();
            /*
             * Iterate child nodes
             */
            for (final String name : userElement.keySet()) {
                final ItemHandler itemHandler = ITEM_HANDLERS.get(name);
                if (null == itemHandler) {
                    Logger logger = org.slf4j.LoggerFactory.getLogger(FacebookFQLUserJsonParser.class);
                    logger.warn("Un-handled item: {}", name);
                } else {
                    itemHandler.handleItem(userElement, user);
                }
            }
            /*
             * Return
             */
            return user.isEmpty() ? null : user;
        } catch (final JSONException e) {
            throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

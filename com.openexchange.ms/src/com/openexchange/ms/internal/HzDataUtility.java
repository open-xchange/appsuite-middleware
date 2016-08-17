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

package com.openexchange.ms.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link HzDataUtility} - A utility class for Hazelcast-based messaging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzDataUtility {

    /**
     * Initializes a new {@link HzDataUtility}.
     */
    private HzDataUtility() {
        super();
    }

    // ------------------------------------- DELAY STUFF -------------------------------------------- //

    /**
     * The delay for pooled messages.
     */
    public static final long DELAY_MSEC = 5000L;

    /**
     * The frequency to check for delayed pooled messages.
     */
    public static final int DELAY_FREQUENCY = 3000;

    // ------------------------------------- CHUNK STUFF -------------------------------------------- //

    /**
     * The chunk size of a multiple message.
     */
    public static final int CHUNK_SIZE = 10;

    /**
     * The threshold when to switch to a multiple message.
     */
    public static final int CHUNK_THRESHOLD = 2;

    // ------------------------------------- MESSAGE DATA ------------------------------------------- //

    /**
     * The property name for the identifier of the sender that transmitted message data.
     */
    public static final String MESSAGE_DATA_SENDER_ID = "__senderId".intern();

    /**
     * The property name for transmitted message data object.
     */
    public static final String MESSAGE_DATA_OBJECT = "__object".intern();

    /**
     * The property to mark as a multiple transport.
     */
    public static final String MULTIPLE_MARKER = "__multiple".intern();

    /**
     * The property prefix on a multiple transport.
     */
    public static final String MULTIPLE_PREFIX = "__map".intern();

    /**
     * Generates message data for given arguments.
     *
     * @param e The message data object; POJOs preferred
     * @param senderId The sender identifier
     * @return The message data container
     */
    public static <E> Map<String, Object> generateMapFor(final E e, final String senderId) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>(4);
        if (null != e) {
            map.put(MESSAGE_DATA_OBJECT, e);
        }
        if (null != senderId) {
            map.put(MESSAGE_DATA_SENDER_ID, senderId);
        }
        return map;
    }

    // ------------------------------------- OTHER STIFF ------------------------------------------- //

}

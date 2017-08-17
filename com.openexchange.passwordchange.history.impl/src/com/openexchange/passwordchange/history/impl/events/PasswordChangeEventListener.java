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

package com.openexchange.passwordchange.history.impl.events;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.session.Session;

/**
 * {@link PasswordChangeEventListener} Listens to password change event created in {@link com.openexchange.passwordchange.BasicPasswordChangeService#perform(com.openexchange.passwordchange.PasswordChangeEvent)} (in propagate)
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeEventListener implements EventHandler {

    /** The topic the event listens to */
    private static final String TOPIC = "com/openexchange/passwordchange";

    private final PasswordChangeRecorderRegistryService registry;

    /**
     * Initializes a new {@link PasswordChangeEventListener}.
     *
     * @param registry The {@link PasswordChangeRecorderRegistryService} to get the {@link PasswordChangeRecorder} from
     */
    public PasswordChangeEventListener(PasswordChangeRecorderRegistryService registry) {
        super();
        this.registry = registry;
    }

    /**
     * Gets the topic of interest.
     *
     * @return The topic
     */
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public void handleEvent(Event event) {
        if (false == TOPIC.equals(event.getTopic())) {
            return;
        }

        // Read user/context identifier
        int contextId = (int) event.getProperty("com.openexchange.passwordchange.contextId");
        int userId = (int) event.getProperty("com.openexchange.passwordchange.userId");
        String ipAddress = String.valueOf(event.getProperty(("com.openexchange.passwordchange.ipAddress")));
        Session session = (Session) event.getProperty("com.openexchange.passwordchange.session");

        PasswordChangeHelper.recordChangeSafe(contextId, userId, ipAddress, session.getClient(), registry);
    }
}

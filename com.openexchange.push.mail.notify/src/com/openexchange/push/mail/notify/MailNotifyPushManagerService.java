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

package com.openexchange.push.mail.notify;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.PushUser;
import com.openexchange.session.Session;

/**
 * {@link MailNotifyPushManagerService} - The {@link PushManagerService} for primary mail account.
 */
public final class MailNotifyPushManagerService implements PushManagerService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailNotifyPushManagerService.class);

    private final String name;
    private final MailNotifyPushListenerRegistry registry;

    /**
     * Initializes a new {@link MailNotifyPushManagerService}.
     */
    public MailNotifyPushManagerService(MailNotifyPushListenerRegistry registry) {
        super();
        name = "Mail Push Manager";
        this.registry = registry;
    }

    private boolean hasPermanentPush(int userId, int contextId) {
        try {
            PushListenerService pushListenerService = Services.getService(PushListenerService.class, true);
            return pushListenerService.hasRegistration(new PushUser(userId, contextId));
        } catch (Exception e) {
            LOGGER.warn("Failed to check for push registration for user {} in context {}", I(userId), I(contextId), e);
            return false;
        }
    }

    private Session generateSessionFor(int userId, int contextId) throws OXException {
        PushListenerService pushListenerService = Services.getService(PushListenerService.class, true);
        return pushListenerService.generateSessionFor(new PushUser(userId, contextId));
    }

    private Session generateSessionFor(PushUser pushUser) throws OXException {
        PushListenerService pushListenerService = Services.getService(PushListenerService.class, true);
        return pushListenerService.generateSessionFor(pushUser);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startListener(final Session session) throws OXException {
        final MailNotifyPushListener pushListener = MailNotifyPushListener.newInstance(session);
        if (registry.addPushListener(session.getContextId(), session.getUserId(), pushListener)) {
            pushListener.open();
            return pushListener;
        }
        return null;
    }

    @Override
    public boolean stopListener(final Session session) throws OXException {
        return registry.removePushListener(session.getContextId(), session.getUserId());
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return name;
    }

}

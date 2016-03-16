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

package com.openexchange.event.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.java.Streams;

/**
 * {@link EventConfigImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class EventConfigImpl extends AbstractConfigWrapper implements EventConfig {

    private boolean isEventQueueEnabled;

    private int eventQueueDelay = 60000;

    //private boolean isInit;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventConfigImpl.class);

    public EventConfigImpl() {

    }

    public EventConfigImpl(final File propfile) {
        /*-
         * This if statement always yields false
         *
        if (isInit) {
            return;
        }
        */

        if (propfile == null) {
            LOG.error("missing propfile");
            return;
        }
        Properties prop = null;

        try {
            LOG.debug("try to load propfile: {}", propfile);

            prop = new Properties();
            final java.io.InputStream in = new FileInputStream(propfile);
            try {
                prop.load(in);
            } finally {
                Streams.close(in);
            }
        } catch (final FileNotFoundException exc) {
            LOG.error("Cannot find propfile: {}", propfile, exc);
        } catch (final IOException exc) {
            LOG.error("Cannot read propfile: {}", propfile, exc);
        }

        isEventQueueEnabled = parseProperty(prop, "com.openexchange.event.isEventQueueEnabled", false);
        LOG.debug("Event property: com.openexchange.event.isEventQueueEnabled={}", isEventQueueEnabled);

        eventQueueDelay = parseProperty(prop, "com.openexchange.event.eventQueueDelay", eventQueueDelay);
        LOG.debug("Event property: com.openexchange.event.eventQueueDelay={}", eventQueueDelay);

        /*-
         * Field "isInit" is never used
         *
        isInit = true;
        */
    }

    @Override
    public boolean isEventQueueEnabled() {
        return isEventQueueEnabled;
    }

    @Override
    public void setEventQueueEnabled(final boolean isEventQueueEnabled) {
        this.isEventQueueEnabled = isEventQueueEnabled;
    }

    @Override
    public int getEventQueueDelay() {
        return eventQueueDelay;
    }

    @Override
    public void setEventQueueDelay(final int eventQueueDelay) {
        this.eventQueueDelay = eventQueueDelay;
    }
}

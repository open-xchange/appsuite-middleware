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

package com.openexchange.push;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.ConfigurationException.Code;
import com.openexchange.event.EventQueue;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.push.udp.PushConfigInterface;
import com.openexchange.push.udp.PushConfigInterfaceImpl;
import com.openexchange.push.udp.PushHandler;
import com.openexchange.push.udp.PushMulticastRequestTimer;
import com.openexchange.push.udp.PushMulticastSocket;
import com.openexchange.push.udp.PushOutputQueue;
import com.openexchange.push.udp.PushSocket;
import com.openexchange.server.impl.Initialization;

/**
 * Initializes the event system.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class PushInit implements Initialization {

    /**
     * Singleton.
     */
    private static final PushInit SINGLETON = new PushInit();

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(PushInit.class);

    private static final SystemConfig.Property KEY = SystemConfig.Property.PUSH;

    private PushMulticastRequestTimer requestTimer;

    private PushMulticastSocket multicast;

    private PushOutputQueue output;

    private PushSocket input;

    private PushHandler pushHandler;

    private PushConfigInterface config;

    /**
     * Prevent instantiation.
     */
    private PushInit() {
        super();
    }

    /**
     * @return the singleton instance.
     */
    public static PushInit getInstance() {
        return SINGLETON;
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws ConfigurationException {
        if (null != config) {
            LOG.error("Duplicate push initialization.");
            return;
        }
        final String fileName = SystemConfig.getProperty(KEY);
        if (null == fileName) {
            throw new ConfigurationException(Code.PROPERTY_MISSING, KEY
                .getPropertyName());
        }
        config = new PushConfigInterfaceImpl(fileName);
        input = new PushSocket(config);
        output = new PushOutputQueue(config);

        if (config.isPushEnabled()) {
            pushHandler = new PushHandler();
            EventQueue.addAppointmentEvent(pushHandler);
            EventQueue.addTaskEvent(pushHandler);
            EventQueue.addContactEvent(pushHandler);
            EventQueue.addFolderEvent(pushHandler);
        }

        multicast = new PushMulticastSocket(config);
        requestTimer = new PushMulticastRequestTimer(config);
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        if (null == requestTimer) {
            LOG.error("Duplicate push component shutdown.");
            return;
        }
        requestTimer.cancel();
        requestTimer = null;
        multicast.close();
        multicast = null;
        if (config.isPushEnabled()) {
            EventQueue.removeAppointmentEvent(pushHandler);
            EventQueue.removeTaskEvent(pushHandler);
            EventQueue.removeContactEvent(pushHandler);
            EventQueue.removeFolderEvent(pushHandler);
            pushHandler = null;
        }
        output.close();
        output = null;
        input.close();
        input = null;
    }
}

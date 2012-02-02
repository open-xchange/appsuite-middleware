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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.osgi;

import java.util.Dictionary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * {@link ConditionalRegistration}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ConditionalRegistration {
    protected BundleContext context;
    protected String serviceName;
    protected Object service;
    protected Dictionary dictionary;
    protected ServiceRegistration registration;
    private boolean running;

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ConditionalRegistration.class));

    public ConditionalRegistration(final BundleContext context, final String serviceName, final Object service, final Dictionary dict) {
        this.context = context;
        this.serviceName = serviceName;
        this.service = service;
        dictionary = dict;
    }

    public void check() {
        if(!running) {
            return;
        }
        if(!registered() && mustRegister()) {
            register();
        } else if (registered() && ! mustRegister()) {
            unregister();
        }
    }

    private synchronized void unregister() {
        if(registration != null) {
            LOG.info("Unregistering "+service+" as "+serviceName+". ");
            registration.unregister();
            registration = null;
        }
    }


    private synchronized void register() {
        if(registration == null && service != null && running) {
            registration = context.registerService(serviceName, service, dictionary);
            LOG.info("Registering "+service+" as "+serviceName);
        }
    }

    protected boolean mustRegister() {
        return true;
    }

    private synchronized boolean registered() {
        return registration != null;
    }

    public void start() {
        running = true;
        check();
    }

    public void close() {
        running = false;
        unregister();
    }

}

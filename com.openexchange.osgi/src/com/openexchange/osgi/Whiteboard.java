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

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.tools.global.OXCloseable;


/**
 * {@link Whiteboard}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Whiteboard implements OXCloseable {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Whiteboard.class));

    private final List<OXCloseable> closeables = new LinkedList<OXCloseable>();

    private final BundleContext context;

    private final DynamicWhiteboardFactory factory;

    public Whiteboard(final BundleContext context) {
        this.context = context;
        factory = new DynamicWhiteboardFactory(context);
        closeables.add(factory);
    }

    public <T> T getService(final Class<T> klass) {
        return factory.createWhiteboardService(context, klass, closeables, null);
    }

    public <T> T getService(final Class<T> klass, final DynamicServiceStateListener listener) {
        return factory.createWhiteboardService(context, klass, closeables, listener);
    }

    public boolean isActive(final Object o) {
        return factory.isActive(o);
    }

    @Override
    public void close() throws OXException {
        for(final OXCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (final OXException x) {
                LOG.error(x);
            }
        }
    }
}

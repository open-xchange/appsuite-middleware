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

package com.openexchange.ajp13.timertask;

import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajp13.AJPv13Config;

/**
 * {@link AJPv13JSessionIDCleaner} - A {@link TimerTask timer task} to clean exceeded <i>JSESSIONID</i>s. The time-to-live is taken from
 * {@link AJPv13Config#getJSessionIDTTL()}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13JSessionIDCleaner implements Runnable {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13JSessionIDCleaner.class);

    private final ConcurrentMap<String, Long> jsessionids;

    /**
     * Initializes a new {@link AJPv13JSessionIDCleaner}
     * 
     * @param jsessionids The concurrent map containing known <i>JSESSIONID</i>s
     */
    public AJPv13JSessionIDCleaner(final ConcurrentMap<String, Long> jsessionids) {
        super();
        this.jsessionids = jsessionids;
    }

    public void run() {
        try {
            if (AJPv13Config.getJSessionIDTTL() <= 0) {
                /*
                 * Infinite TTL
                 */
                return;
            }
            for (final Iterator<Map.Entry<String, Long>> iterator = jsessionids.entrySet().iterator(); iterator.hasNext();) {
                final Map.Entry<String, Long> entry = iterator.next();
                if ((System.currentTimeMillis() - entry.getValue().longValue()) > AJPv13Config.getJSessionIDTTL()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new StringBuilder("Removing JSESSIONID ").append(entry.getKey()));
                    }
                    iterator.remove();
                }
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

}

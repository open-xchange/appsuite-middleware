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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mailfilter.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mailfilter.MailFilterInterceptor;
import com.openexchange.mailfilter.MailFilterInterceptorRegistry;

/**
 * {@link MailFilterInterceptorRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterInterceptorRegistryImpl implements MailFilterInterceptorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFilterInterceptorRegistryImpl.class);

    private final Set<MailFilterInterceptor> interceptors;

    /**
     * {@link MailFilterInterceptorComparator}
     */
    private static class MailFilterInterceptorComparator implements Comparator<MailFilterInterceptor> {

        /**
         * Initializes a new {@link MailFilterInterceptorComparator}.
         */
        MailFilterInterceptorComparator() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(MailFilterInterceptor o1, MailFilterInterceptor o2) {
            return o1.getRank() > o2.getRank() ? -1 : 1;
        }
    }

    /**
     * Initialises a new {@link MailFilterInterceptorRegistryImpl}.
     */
    public MailFilterInterceptorRegistryImpl() {
        super();
        interceptors = new TreeSet<>(new MailFilterInterceptorComparator());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mailfilter.MailFilterInterceptorRegistry#register(com.openexchange.mailfilter.MailFilterInterceptor, int)
     */
    @Override
    public void register(MailFilterInterceptor interceptor) {
        boolean registered = interceptors.add(interceptor);
        LOGGER.debug("MailFilterInterceptor '{}' with rank {} was {} registered.", interceptor.getClass().getSimpleName(), interceptor.getRank(), registered ? "sucessfully" : "not");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mailfilter.MailFilterInterceptorRegistry#execute(java.util.List)
     */
    @Override
    public void executeBefore(int userId, int contextId, List<Rule> rules) throws OXException {
        LOGGER.debug("Executing pre-processing mail filter interceptors...");
        for (MailFilterInterceptor interceptor : interceptors) {
            LOGGER.debug("Executing pre-processing mail filter interceptor {} with rank {}", interceptor.getClass().getSimpleName(), interceptor.getRank());
            interceptor.before(userId, contextId, rules);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mailfilter.MailFilterInterceptorRegistry#executeAfter(java.util.List)
     */
    @Override
    public void executeAfter(int userId, int contextId, List<Rule> rules) throws OXException {
        LOGGER.debug("Executing post-processing mail filter interceptors...");
        for (MailFilterInterceptor interceptor : interceptors) {
            LOGGER.debug("Executing post-processing mail filter interceptor {} with rank {}", interceptor.getClass().getSimpleName(), interceptor.getRank());
            interceptor.after(userId, contextId, rules);
        }
    }
}

/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailfilter.internal;

import static com.openexchange.java.Autoboxing.I;
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

    @Override
    public void register(MailFilterInterceptor interceptor) {
        boolean registered = interceptors.add(interceptor);
        LOGGER.debug("MailFilterInterceptor '{}' with rank {} was {} registered.", interceptor.getClass().getSimpleName(), I(interceptor.getRank()), registered ? "sucessfully" : "not");
    }

    @Override
    public void executeBefore(int userId, int contextId, List<Rule> rules) throws OXException {
        LOGGER.debug("Executing pre-processing mail filter interceptors...");
        for (MailFilterInterceptor interceptor : interceptors) {
            LOGGER.debug("Executing pre-processing mail filter interceptor {} with rank {}", interceptor.getClass().getSimpleName(), I(interceptor.getRank()));
            interceptor.before(userId, contextId, rules);
        }
    }

    @Override
    public void executeAfter(int userId, int contextId, List<Rule> rules) throws OXException {
        LOGGER.debug("Executing post-processing mail filter interceptors...");
        for (MailFilterInterceptor interceptor : interceptors) {
            LOGGER.debug("Executing post-processing mail filter interceptor {} with rank {}", interceptor.getClass().getSimpleName(), I(interceptor.getRank()));
            interceptor.after(userId, contextId, rules);
        }
    }
}

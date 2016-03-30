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

package com.openexchange.mail.osgi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.mail.api.MailProvider;

/**
 * {@link MailProviderProxyGenerator} - Generates proxy objects for mail provider which delegate method invocations to the service obtained
 * from a bundle context
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailProviderProxyGenerator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailProviderProxyGenerator.class);

    /**
     * TODO: Does not work since {@link MailProvider} is not an interface
     * <p>
     * Create a new proxy object for mail provider which delegates method invocations to the service obtained from bundle context
     *
     * @param mailProviderServiceReference The service reference of a mail provider
     * @param context The bundle context (needed to get/unget the service)
     * @return A new proxy object for mail provider
     */
    public static MailProvider newMailProviderProxy(final ServiceReference mailProviderServiceReference, final BundleContext context) {
        try {
            return (MailProvider) java.lang.reflect.Proxy.newProxyInstance(
                MailProvider.class.getClassLoader(),
                new Class<?>[] { MailProvider.class },
                new MailProviderInvocationHandler(mailProviderServiceReference, context));
        } catch (final ClassCastException e) {
            LOG.error("", e);
            return null;
        }
    }

    /**
     * Initializes a new {@link MailProviderProxyGenerator}
     */
    private MailProviderProxyGenerator() {
        super();
    }

    private static final class MailProviderInvocationHandler implements java.lang.reflect.InvocationHandler {

        private final BundleContext context;

        private final ServiceReference mailProviderServiceReference;

        /**
         * Initializes a new {@link MailProviderProxyGenerator}
         */
        private MailProviderInvocationHandler(final ServiceReference mailProviderServiceReference, final BundleContext context) {
            super();
            this.mailProviderServiceReference = mailProviderServiceReference;
            this.context = context;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            Object result;
            try {
                final MailProvider provider = (MailProvider) context.getService(mailProviderServiceReference);
                try {
                    result = method.invoke(provider, args);
                } finally {
                    context.ungetService(mailProviderServiceReference);
                }
            } catch (final InvocationTargetException e) {
                throw e.getTargetException();
            } catch (final Exception e) {
                throw new RuntimeException("unexpected invocation exception: " + e.getMessage(), e);
            }
            return result;
        }
    }

}

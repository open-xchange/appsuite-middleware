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

package com.ctc.wstx.osgi.delegate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * {@link WstxDelegateBundleActivator} - This class is responsible for registering OSGi service(s) that Woodstox package provides.
 * <p>
 * Currently it means registering all providers that are needed to instantiate input, output and validation schema factories; these are
 * needed since JDK service-introspection (which is the standard Stax instance instantiation mechanism) does not work with OSGi.
 * <p>
 * All resources obtained from <a href="http://wiki.fasterxml.com/WoodstoxDownload">Woodstox Download</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class WstxDelegateBundleActivator implements BundleActivator {

    private final com.ctc.wstx.osgi.WstxBundleActivator delegate;

    /**
     * Initializes a new {@link WstxDelegateBundleActivator}.
     */
    public WstxDelegateBundleActivator() {
        super();
        delegate = new com.ctc.wstx.osgi.WstxBundleActivator();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WstxDelegateBundleActivator.class);
        logger.info("Starting bundle: com.ctc.wstx...");
        try {
            delegate.start(context);
            logger.info("Bundle successfully started: com.ctc.wstx");
        } catch (final Exception e) {
            logger.error("Error while starting bundle: com.ctc.wstx", e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WstxDelegateBundleActivator.class);
        logger.info("Stopping bundle: com.ctc.wstx...");
        try {
            delegate.stop(context);
            logger.info("Bundle successfully stopped: com.ctc.wstx");
        } catch (final Exception e) {
            logger.error("Error while stopping bundle: com.ctc.wstx", e);
            throw e;
        }
    }

}

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

package com.openexchange.imap.acl;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;

/**
 * {@link ACLExtensionInit} - Initialization for ACL extension.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ACLExtensionInit implements Initialization {

    private static final ACLExtensionInit instance = new ACLExtensionInit();

    /**
     * Gets the initialization for ACL extension.
     *
     * @return The initialization for ACL extension.
     */
    public static ACLExtensionInit getInstance() {
        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    // private Class<? extends ACLExtension> implementingClass;

    private final AtomicBoolean started;

    /**
     * Initializes a new {@link ACLExtensionInit}.
     */
    private ACLExtensionInit() {
        super();
        started = new AtomicBoolean();
    }

    @Override
    public void start() throws OXException {
        if (started.get()) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ACLExtensionInit.class);
            logger.warn("{} already started", ACLExtensionInit.class.getName());
            return;
        }
        ACLExtensionFactory.createInstance();
        // ACLExtensionAutoDetector.initACLExtensionMappings();
        // TODO: Uncomment subsequent code to enable configured instance rather than auto-detection
        /*-
         *
        synchronized(this) {
            if (null == implementingClass) {
                final String classNameProp = IMAPConfig.getEntity2AclImpl();
                if ((null == classNameProp) || (classNameProp.length() == 0)) {
                    throw new Entity2ACLException(Entity2ACLException.Code.MISSING_SETTING,
                            "com.openexchange.imap.User2ACLImpl");
                }
                if ("auto".equalsIgnoreCase(classNameProp)) {
                    // Try to detect dependent on IMAP server greeting
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Auto-Detection for IMAP server implementation");
                    }
                    implementingClass = null;
                    return;
                }
                final String className = IMAPServer.getIMAPServerImpl(classNameProp);
                implementingClass = className == null ? Class.forName(classNameProp).asSubclass(Entity2ACL.class)
                        : Class.forName(className).asSubclass(Entity2ACL.class);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Used IMAP server implementation: {}", implementingClass.getName());
                }
                ACLExtensionFactory.getInstance().setACLExtensionInstance(implementingClass.newInstance());
            }
        } catch (final ClassNotFoundException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.CLASS_NOT_FOUND, e, EMPTY_ARGS);
        } catch (final InstantiationException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
        } catch (final IllegalAccessException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
        }
         */
    }

    @Override
    public void stop() throws OXException {
        if (started.get()) {
            ACLExtensionFactory.getInstance().resetACLExtensionFactory();
            // ACLExtensionAutoDetector.resetACLExtensionMappings();
            ACLExtensionFactory.releaseInstance();
        }
    }

}

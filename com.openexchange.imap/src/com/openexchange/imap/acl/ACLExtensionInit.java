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
        } catch (ClassNotFoundException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.CLASS_NOT_FOUND, e, EMPTY_ARGS);
        } catch (InstantiationException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.INSTANTIATION_FAILED, e, EMPTY_ARGS);
        } catch (IllegalAccessException e) {
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

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

package com.openexchange.imap.entity2acl;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.server.Initialization;

/**
 * {@link Entity2ACLInit}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Entity2ACLInit implements Initialization {

    private static final Entity2ACLInit instance = new Entity2ACLInit();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Entity2ACLInit.class);

    /**
     * @return The singleton instance of {@link Entity2ACLInit}
     */
    public static Entity2ACLInit getInstance() {
        return instance;
    }

    private volatile Class<? extends Entity2ACL> implementingClass;

    private final AtomicBoolean started;

    /**
     * No instantiation
     */
    private Entity2ACLInit() {
        super();
        started = new AtomicBoolean();
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("{} already started", Entity2ACLInit.class.getName());
            return;
        }
        Entity2ACLAutoDetector.initEntity2ACLMappings();
        try {
            if (null == implementingClass) {
                final String classNameProp = IMAPProperties.getInstance().getEntity2AclImpl();
                if ((null == classNameProp) || (classNameProp.length() == 0)) {
                    throw Entity2ACLExceptionCode.MISSING_SETTING.create("com.openexchange.imap.User2ACLImpl");
                }
                if ("auto".equalsIgnoreCase(classNameProp)) {
                    LOG.info("Auto-Detection for IMAP server implementation");
                    implementingClass = null;
                    return;
                }
                final Entity2ACL className = IMAPServer.getIMAPServerImpl(classNameProp);
                if (null != className) {
                    Entity2ACL.setInstance(className);
                } else {
                    implementingClass = Class.forName(classNameProp).asSubclass(Entity2ACL.class);
                    LOG.info("Used IMAP server implementation: {}", implementingClass.getName());
                    Entity2ACL.setInstance(implementingClass.newInstance());
                }
            }
        } catch (ClassNotFoundException e) {
            throw Entity2ACLExceptionCode.CLASS_NOT_FOUND.create(e, new Object[0]);
        } catch (InstantiationException e) {
            throw Entity2ACLExceptionCode.INSTANTIATION_FAILED.create(e, new Object[0]);
        } catch (IllegalAccessException e) {
            throw Entity2ACLExceptionCode.INSTANTIATION_FAILED.create(e, new Object[0]);
        }
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error(Entity2ACLInit.class.getName() + " cannot be stopped since it has not been started before");
            return;
        }
        implementingClass = null;
        Entity2ACL.resetEntity2ACL();
        Entity2ACLAutoDetector.resetEntity2ACLMappings();
    }

}

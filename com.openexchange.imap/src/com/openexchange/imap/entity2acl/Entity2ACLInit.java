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
        } catch (final ClassNotFoundException e) {
            throw Entity2ACLExceptionCode.CLASS_NOT_FOUND.create(e, new Object[0]);
        } catch (final InstantiationException e) {
            throw Entity2ACLExceptionCode.INSTANTIATION_FAILED.create(e, new Object[0]);
        } catch (final IllegalAccessException e) {
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

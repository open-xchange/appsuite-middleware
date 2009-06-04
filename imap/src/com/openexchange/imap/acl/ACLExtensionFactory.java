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

package com.openexchange.imap.acl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.config.IMAPConfig;

/**
 * {@link ACLExtensionFactory} - Factory for ACL extension.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ACLExtensionFactory {

    private static ACLExtensionFactory instance;

    static void createInstance() {
        instance = new ACLExtensionFactory();
    }

    static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the factory instance.
     * 
     * @return The factory instance.
     */
    public static ACLExtensionFactory getInstance() {
        return instance;
    }

    private final AtomicBoolean instantiated;

    private ACLExtension configured;

    /**
     * Initializes a new {@link ACLExtensionFactory}.
     */
    private ACLExtensionFactory() {
        super();
        instantiated = new AtomicBoolean();
    }

    /**
     * Gets the appropriate ACL extension for the IMAP server denoted by specified IMAP configuration.
     * 
     * @param imapConfig The IMAP configuration providing needed access data.
     * @return The appropriate ACL extension
     * @throws IMAPException If an I/O error occurs
     */
    public ACLExtension getACLExtension(final IMAPConfig imapConfig) throws IMAPException {
        if (!instantiated.get()) {
            try {
                return ACLExtensionAutoDetector.getACLExtension(imapConfig);
            } catch (final IOException e) {
                throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getMessage());
            }
        }
        return configured;
    }

    /**
     * Resets this factory instance.
     */
    void resetACLExtensionFactory() {
        configured = null;
        instantiated.set(false);
    }

    /**
     * Only invoked if auto-detection is turned off.
     * 
     * @param singleton The singleton instance of {@link ACLExtension}
     */
    void setACLExtensionInstance(final ACLExtension singleton) {
        configured = singleton;
        instantiated.set(true);
    }
}

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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.compose;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.mail.MailPath;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpace} - Represents a composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CompositionSpace {

    private static final String PARAM_REGISTRY = "__comp.registry";

    /**
     * Gets the composition space registry
     *
     * @param session The associated session
     * @return The composition space registry
     */
    static CompositionSpaceRegistry getRegistry(Session session) {
        CompositionSpaceRegistry registry = (CompositionSpaceRegistry) session.getParameter(PARAM_REGISTRY);
        if (null == registry) {
            CompositionSpaceRegistry newRegistry = new CompositionSpaceRegistry();
            registry = (CompositionSpaceRegistry) ((PutIfAbsent) session).setParameterIfAbsent(PARAM_REGISTRY, newRegistry);
            if (null == registry) {
                registry = newRegistry;
            }
        }
        return registry;
    }

    /**
     * Gets the composition space for given identifier
     *
     * @param csid The composition space identifier
     * @param session The associated session
     * @return The composition space
     */
    public static CompositionSpace getCompositionSpace(String csid, Session session) {
        return getRegistry(session).getCompositionSpace(csid);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------
    // //

    private final String id;
    private volatile MailPath replyFor;
    private volatile MailPath forwardFor;
    private final Queue<MailPath> cleanUps;

    /**
     * Initializes a new {@link CompositionSpace}.
     */
    CompositionSpace(String id) {
        super();
        this.id = id;
        cleanUps = new ConcurrentLinkedQueue<MailPath>();
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the <code>replyFor</code> reference
     *
     * @return The <code>replyFor</code> reference
     */
    public MailPath getReplyFor() {
        return replyFor;
    }

    /**
     * Sets the <code>replyFor</code> reference
     *
     * @param replyFor The <code>replyFor</code> reference to set
     */
    public void setReplyFor(MailPath replyFor) {
        this.replyFor = replyFor;
    }

    /**
     * Gets the <code>forwardFor</code> reference
     *
     * @return The <code>forwardFor</code> reference
     */
    public MailPath getForwardFor() {
        return forwardFor;
    }

    /**
     * Sets the <code>forwardFor</code> reference
     *
     * @param forwardFor The <code>forwardFor</code> reference to set
     */
    public void setForwardFor(MailPath forwardFor) {
        this.forwardFor = forwardFor;
    }

    /**
     * Adds given mail path to clean-ups.
     *
     * @param mailPath The mail path to add
     */
    public void addCleanUp(MailPath mailPath) {
        cleanUps.offer(mailPath);
    }

    /**
     * Gets the clean-ups
     *
     * @return The clean-ups
     */
    public Queue<MailPath> getCleanUps() {
        return cleanUps;
    }

}

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

package com.openexchange.realtime;

import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;


/**
 * A {@link ComponentHandle} is a recipient and handler of messages directed at a certain component. Component Handles need not be thread-safe.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * 
 */
public @NotThreadSafe interface ComponentHandle {
    
    /**
     * Process the stanza that was received for this handle.
     */
    void process(Stanza stanza);

    /**
     * Should this stanza be handled in the global thread. This should generally return false. Have this return
     * true if the resource directory state might be changed by this method call
     */
    boolean shouldBeDoneInGlobalThread(Stanza stanza);
    
    /**
     * Get the id used to direct messages at this {@link ComponentHandle}
     * @return the id used to direct messages at this {@link ComponentHandle}
     */
    ID getId();

    /**
     * Dispose this Componenthandle. Stanzas that are currently being delivered to this ComponentHandle will either be silently discarded or
     * cause an Exception due to the missing ComponentHandle depending on the position of the {@link Stanza} in the backend stack.
     */
    void dispose();

}

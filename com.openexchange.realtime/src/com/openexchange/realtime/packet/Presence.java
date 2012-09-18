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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.packet;

/**
 * {@link Presence} - Exchanges presence information.
 * A Presence Stanza is a broadcast from a single entity X to a set of entities subscribed to this specific entity X.
 * Being a broadcast this stanza normally doesn't specify a recipient via the <code>to</code> field.
 * Nevertheless clients are able to send directed Presence Stanzas to single recipients by specifying a <code>to</code>.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */

public class Presence extends Stanza {

    /**
     * {@link Type} - Specifies the Presence Type
     * <ol>
     * <li>unavailable: entity is no longer available for communication</li>
     * <li>subscribe: sender wants to subscribe to recipient's presence</li>
     * <li>subscribed: sender allowed recipient to subscribe to their presence</li>
     * <li>unsubscribe: sender wants to unsubscribe from recipient's presence</li>
     * <li>unsubscribed: <code>subscribe</code> request has been denied or an existing subscription has been cancelled</li>
     * <li>error: if an error occurred during processing or delivery of the last stanza</li>
     * <li>none: is used for the initial presence message of a client to signal its availability for communications.</li>
     * <li>pending:</li>
     * </ol>
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public enum Type {
        UNAVAILABLE, SUBSCRIBE, SUBSCRIBED, UNSUBSCRIBE, UNSUBSCRIBED, ERROR, NONE, PENDING;
    }

    /**
     * Optional attribute. The default of none means the client is available.
     */
    private Type type = Type.NONE;

    /**
     * Gets the type of Presence Stanza
     * 
     * @return The state
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the Presence Stanza
     * 
     * @param type The state to set
     */
    public void setType(Type type) {
        this.type = type;
    }

}

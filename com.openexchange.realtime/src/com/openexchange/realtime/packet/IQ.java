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

package com.openexchange.realtime.packet;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.payload.PayloadTree;

/**
 * {@link IQ} - Used for command exchanges.
 * <p>
 * Example for registering a new account:
 *
 * <pre>
 *    C: &lt;iq type='get' id='reg1'&gt;
 *           &lt;query xmlns='jabber:iq:register'/&gt;
 *       &lt;/iq>
 *    S: &lt;iq type='result' id='reg1'&gt;
 *           &lt;query xmlns='jabber:iq:register'&gt;
 *               &lt;username/&gt;
 *               &lt;password/&gt;
 *           &lt;/query>
 *       &lt;/iq>
 *    C: &lt;iq type='set' id='reg2'&gt;
 *           &lt;query xmlns='jabber:iq:register'&gt;
 *               &lt;username&gt;hax0r&lt;/username&gt;
 *               &lt;password&gt;god&lt;/password&gt;
 *           &lt;/query&gt;
 *       &lt;/iq&gt;
 * </pre>
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IQ extends Stanza {

    private static final long serialVersionUID = 7524944340523962661L;

    /**
     * Describes a command's type.
     */
    public static enum Type {
        GET, SET, RESULT, ERROR
    }

    /**
     * Obligatory type specifier for IQ Stanzas
     */
    private Type type;

    /**
     * Initializes a new {@link IQ}.
     */
    public IQ() {
        super();
    }

    /**
     * Gets the type.
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type
     */
    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public void initializeDefaults() throws OXException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Stanza newInstance() {
        return new IQ();
    }

    @Override
    public void addPayload(PayloadTree tree) {
        addPayloadToMap(tree, payloads);
    }

}

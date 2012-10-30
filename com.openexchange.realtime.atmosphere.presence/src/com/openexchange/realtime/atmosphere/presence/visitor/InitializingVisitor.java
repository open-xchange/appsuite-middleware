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

package com.openexchange.realtime.atmosphere.presence.visitor;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link InitializingVisitor} - Visit the Stanza's default payloads and initialize its fields based on the found payloads.
 * 
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class InitializingVisitor implements PresencePayloadVisitor {

    private Presence presence;

    /**
     * Initializes a new {@link InitializingVisitor}.
     * 
     * @param presence The Presence Stanza to initialize by visiting its payloads.
     */
    public InitializingVisitor(Presence presence) {
        this.presence = presence;
    }
    
    /**
     * Visit the Stanza's default payloads and initialize its fields based on the found payloads. 
     */
    @Override
    public Presence doVisit() {
        Collection<PayloadTree> defaultPayloads = presence.getDefaultPayloads();
        for (PayloadTree payloadTree : defaultPayloads) {
            payloadTree.accept(this);
        }
        return presence;
    }

    @Override
    public void visit(PayloadElement element, Object data) {
        // Only needed vor the Visitor interface hierarchy
    }

    public void visit(PayloadElement element, PresenceState data) {
        presence.setState(data);
    }

    public void visit(PayloadElement element, String data) {
        if (Presence.MESSAGE_PATH.equals(element.getElementPath())) {
            presence.setMessage(data);
        }
    }

    public void visit(PayloadElement element, Byte data) {
        presence.setPriority(data);
    }

    public void visit(PayloadElement element, OXException data) {
        presence.setError(data);
    }
}

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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;


/**
 * {@link StanzaMatcher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class StanzaMatcher extends BaseMatcher<Stanza> {

    private ID from;
    private ID to;
    private String namespace;
    private String name;
    private Object payload;
    private String selector;
    
    /**
     * 
     * @param from sender
     * @param to recipient
     * @param namespace namespace
     * @param name name
     * @param payload payload
     * @return new StanzaMatcher
     */
    public static StanzaMatcher isStanza(ID from, ID to, String namespace, String name, Object payload) {
        return new StanzaMatcher(from, to, namespace, name, null, payload);
    }
    

    public static StanzaMatcher isStanza(ID from, ID to, String namespace, String name, String selector, Object payload) {
        return new StanzaMatcher(from, to, namespace, name, selector, payload);
    }
    
    public StanzaMatcher(ID from, ID to, String namespace, String name, String selector, Object payload) {
        this.from = from;
        this.to = to;
        this.namespace = namespace;
        this.name = name;
        this.payload = payload;
        this.selector = selector;
    }

    @Override
    public boolean matches(Object arg0) {
        if (! (arg0 instanceof Stanza)) {
            return false;
        }
        
        Stanza s = (Stanza) arg0;
        if (selector != null) {
            if (!s.getSelector().equals(selector)) {
                return false;
            }
        }
        if (payload instanceof Matcher) {
            Matcher payloadMatcher = (Matcher) payload;
            if (!payloadMatcher.matches(s.getPayloadTrees())) {
                return false;
            }
        } else {
            if (!payload.equals(s.getPayload().getData())) {
                return false;
            }
        }
        return s.getFrom().equals(from) &&
            s.getTo().equals(to) &&
            s.getPayload().getNamespace().equals(namespace) &&
            s.getPayload().getElementName().equals(name);
           
    }

    @Override
    public void describeTo(Description arg0) {
        
    }

}

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

import org.apache.commons.lang.Validate;

/**
 * {@link IDComponentsParser} - Parse possible components of an ID from a given String.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IDComponentsParser {

    public static class IDComponents {

        public String protocol;

        public String component;

        public String user;

        public String context;

        public String resource;
    }

    /**
     * Parse possible components of an ID from a given String.
     * 
     * @param id the given String representation of an ID
     * @return the IDComponent
     */
    public static IDComponents parse(String id) {

        IDComponents components = new IDComponents();
        if (id.contains("://")) {
            String[] split = id.split("://");
            String protocolAndComponent = split[0];
            String userContextAndResource = split[1];
            parseProtocolAndComponent(components, protocolAndComponent);
            parseUserContextAndResource(components, userContextAndResource);
        } else {
            parseUserContextAndResource(components, id);
        }
        return components;
    }

    /**
     * Parse the protocol and component from a string
     * 
     * @param components the components data structure to fill during parsing
     * @param input the string representation of protocol and component protocol[.component]
     */
    private static void parseProtocolAndComponent(IDComponents components, String input) {
        Validate.notNull(components, "Missing obligatory parameter components");
        Validate.notEmpty(input, "Missing obligatory parameter input");

        if (input.contains(".")) {
            String[] protocolAndComponent = input.split("\\.", 2);
            components.protocol = protocolAndComponent[0];
            components.component = protocolAndComponent[1];
        } else {
            components.protocol=input;
        }
    }

    /**
     * Parse user, context and resource from a string
     * 
     * @param components the components data structure to fill during parsing
     * @param input the string representation of user, context and resource user[@context[/resource]]
     * @return the filled IDComponents
     */
    private static void parseUserContextAndResource(IDComponents components, String input) {
        Validate.notNull(components, "Missing obligatory parameter components");
        Validate.notEmpty(input, "Missing obligatory parameter input");

        /*
         * ID parsing modes  
         *  marc.arens@premium/1234
         * |----0-----|---1---|-2--|
         */
        int mode = 0;
        boolean escape = false;
        StringBuilder b = new StringBuilder();
        
        for(char c: input.toCharArray()) {
            if (escape) {
                b.append(c);
                escape = false;
            } else {
                if (c == '\\') {
                    escape = true;
                } else {
                    switch (mode) {
                    case 0: 
                        if (c == '@') {
                            components.user = b.toString();
                            b = new StringBuilder();
                            mode = 1;
                        } else if (c == '/') {
                            components.user = b.toString();
                            b = new StringBuilder();
                            mode = 2;
                        } else {
                            b.append(c);
                        }
                        break;
                    case 1:
                        if (c == '/') {
                            components.context = b.toString();
                            b = new StringBuilder();
                            mode = 2;
                        } else {
                            b.append(c);
                        }
                        break;
                    case 2: 
                        b.append(c);
                    }
                }
            }
        }

        switch (mode) {
        case 0: components.user = b.toString(); break;
        case 1: components.context = b.toString(); break;
        case 2: components.resource = (b.length() != 0) ? b.toString() : null; break;
        }
    }

}

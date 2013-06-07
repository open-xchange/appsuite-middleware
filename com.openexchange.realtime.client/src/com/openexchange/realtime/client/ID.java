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

package com.openexchange.realtime.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * {@link ID}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ID {

    private String protocol;

    private String user;

    private String context;

    private String resource;

    private String component;

    /**
     * Pattern to match IDs consisting of protocol, user, context and resource e.g. xmpp://user@context/notebook
     */
    private static final Pattern PATTERN = Pattern.compile("(?:(\\w+?)(\\.\\w+)?://)?([^@/]+)@?([^/]+)?/?(.*)");

    public ID(final String id, String defaultContext) {
        final Matcher matcher = PATTERN.matcher(id);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse id: " + id + ". User and context are obligatory for ID creation.");
        }
        protocol = matcher.group(1);
        component = matcher.group(2);
        user = matcher.group(3);
        context = matcher.group(4);
        resource = matcher.group(5);

        if (context == null) {
            context = defaultContext;
        }

        sanitize();
        validate();

    }

    /**
     * Initializes a new {@link ID} by a String with the syntax "xmpp://user@context/resource".
     * 
     * @param id String with the syntax "xmpp://user@context/resource".
     * @throws IllegalArgumentException if the id doesn't follow the syntax convention.
     */
    public ID(final String id) {
        final Matcher matcher = PATTERN.matcher(id);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse id: " + id + ". User and context are obligatory for ID creation.");
        }
        protocol = matcher.group(1);
        component = matcher.group(2);
        user = matcher.group(3);
        context = matcher.group(4);
        resource = matcher.group(5);

        sanitize();
        validate();
    }

    /**
     * Initializes a new {@link ID} without a component.
     * 
     * @param protocol The protocol of the ID, ox, xmpp ...
     * @param user The user represented by this ID
     * @param context The context of the user represented by this ID
     * @param resource The resource of the connected user eg. "desktop" or ontoher string identifying the connected client. Must be unique
     *            to enable multiple logins.
     */
    public ID(final String protocol, final String user, final String context, final String resource) {
        this(protocol, null, user, context, resource);
    }

    /**
     * Initializes a new {@link ID}.
     * 
     * @param protocol The protocol of the ID, ox, xmpp ...
     * @param component The component of the id (to address Files and so on)
     * @param user The user represented by this ID
     * @param context The context of the user represented by this ID
     * @param resource The resource of the connected user eg. "desktop" or ontoher string identifying the connected client. Must be unique
     *            to enable multiple logins.
     */
    public ID(final String protocol, final String component, final String user, final String context, final String resource) {
        super();
        this.protocol = protocol;
        this.user = user;
        this.context = context;
        this.resource = resource;
        this.component = component;
        sanitize();
        validate();
    }

    /*
     * Check optional id components for emtpy strings and sanitize by setting to null or default values.
     */
    private void sanitize() {
        if (protocol != null && isEmpty(protocol)) {
            protocol = null;
        }

        if (resource != null && isEmpty(resource)) {
            resource = null;
        }

        if (component != null) {
            if (isEmpty(component)) {
                component = null;
            } else {
                if (component.startsWith(".")) {
                    component = component.substring(1);
                }
            }
        }

    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /*
     * Validate that mandatory id components exist.
     */
    private void validate() throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("User information is obligatory for IDs");
        }

        if (context == null) {
            throw new IllegalArgumentException("Context information is obligatory for IDs");
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
        validate();
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
        validate();

    }

    public String getContext() {
        return context;
    }

    public void setContext(final String context) {
        this.context = context;
        validate();
    }

    public String getResource() {
        return resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
        validate();
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ID)) {
            return false;
        }
        final ID other = (ID) obj;
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        if (protocol == null) {
            if (other.protocol != null) {
                return false;
            }
        } else if (!protocol.equals(other.protocol)) {
            return false;
        }
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        if (component == null) {
            if (other.component != null) {
                return false;
            }
        } else if (!component.equals(other.component)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder(32);
        boolean needSep = false;
        if (protocol != null) {
            b.append(protocol);
            needSep = true;
        }
        if (component != null) {
            b.append(".").append(component);
            needSep = true;
        }
        if (needSep) {
            b.append("://");
        }
        b.append(user).append('@').append(context);
        if (resource != null) {
            b.append('/').append(resource);
        }
        return b.toString();
    }

    /**
     * Strip protocol and resource from this id so that it only contains user@context information.
     * 
     * @return
     */
    public ID toGeneralForm() {
        return new ID(null, component, user, context, null);
    }

    /**
     * Gets a value indicating whether this ID is in general form or not, i.e. if it only contains the mandatory parts and no protocol or
     * concrete resource name.
     * 
     * @return <code>true</code> if the ID is in general form, <code>false</code>, otherwise
     */
    public boolean isGeneralForm() {
        return null == protocol && null == resource;
    }

}

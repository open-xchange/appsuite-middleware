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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.IDComponentsParser.IDComponents;
import com.openexchange.realtime.util.IdLookup;
import com.openexchange.realtime.util.IdLookup.UserAndContext;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * An ID describes a valid sender or recipient of a {@link Stanza}.
 *
 * An ID has the following form: [protocol].[component]://[user]@[context]/[resource]:
 * <ol>
 *   <li><b>protocol</b>: specifies the protocol that the entity behind this ID uses to connect to the OX.</li>
 *   <li><b>component</b> (optional): corresponds to a specific backend service that registered itself for this component name</li>
 *   <li><b>user</b>: specifies the name of the entity we want to address</li>
 *   <li><b>context</b>: specifies the context of the entity we want to address</li>
 *   <li><b>resource</b>: is an unique identifier used to distinguish between multiple instances/connections of the same entitiy</li>
 * </ol>
 *
 * <h4>Examples:</h4>
 * <ol>
 *   <li><b>ox://francisco.laguna@premium/20d39asd9da93249f009d</b>: we want to address the user francisco.laguna in the context premium.
 *       The user is connected via the ox channel that is used between the browser and the backend and has the identifier
 *       20d39asd9da93249f009d (tab/window/browser)</li>
 *   <li><b>synthetic.office://operations@premium/66499.62446</b>: The "synthetic" protocol declares that the entity is synthetic and has no
 *       counterpart in the real world (a bot, a room, general programm construct) instead of a user. The backendservice addressed with this
 *       ID is office. The entity addressed is operations@premium and the resource identifies document via folder.document notation</li>
 *   <li><b>call://356c4ad6a4af46948f9703217a1f5a2d@internal</b>:</li>
 * </ol>

 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
/**
 * {@link ID}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.x.y
 */
public class ID implements Serializable {

    private static final long serialVersionUID = -5237507998711320109L;

    public static final AtomicReference<IDManager> ID_MANAGER_REF = new AtomicReference<IDManager>();

    public static final String INTERNAL_CONTEXT = "internal";

    protected volatile String protocol;
    protected volatile String component;
    protected volatile String user;
    protected volatile String context;
    protected volatile String resource;
    protected volatile String cachedStringRepresentation;

    /**
     * Initializes a new {@link ID}.
     */
    public ID() {
        super();
    }

    /**
     * Initializes a new {@link ID}.
     *
     * @param id the given String representation of an ID
     * @param defaultContext the default context to use if the string representation doesn't contain one
     * @throws IllegalArgumentException if no ID could be created from the given String
     */
    public ID(final String id, String defaultContext) {
        IDComponents idComponents = IDComponentsParser.parse(id);
        protocol = idComponents.protocol;
        component = idComponents.component;
        user = idComponents.user;
        String context = idComponents.context;
        this.context = context == null ? defaultContext : context;
        resource = idComponents.resource;
        sanitize();
        validate();

    }

    /**
     * Initializes a new {@link ID}.
     *
     * @param id the given String representation of an ID
     * @throws IllegalArgumentException if no ID could be created from the given String
     */
    public ID(final String id) {
        IDComponents idComponents = IDComponentsParser.parse(id);
        protocol = idComponents.protocol;
        component = idComponents.component;
        user = idComponents.user;
        context = idComponents.context;
        resource = idComponents.resource;

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

    /**
     * Initializes a new {@link ID}.
     *
     * @param protocol The protocol of the ID, ox, xmpp ...
     * @param component The component of the id (to address Files and so on)
     * @param user The user represented by this ID
     * @param context The context of the user represented by this ID
     * @param resource The resource of the connected user eg. "desktop" or ontoher string identifying the connected client. Must be unique
     *            to enable multiple logins.
     * @param cachedStringRepresentation the cached {@link String} representation of the id. Unless this parameter is empty/null we assume the
     *            cachedStringRepresentation is in sync with the other provided components.
     */
    public ID(final String protocol, final String component, final String user, final String context, final String resource, final String cachedStringRepresentation) {
        super();
        this.protocol = protocol;
        this.user = user;
        this.context = context;
        this.resource = resource;
        this.component = component;
        this.cachedStringRepresentation = Strings.isEmpty(cachedStringRepresentation) ? null : cachedStringRepresentation;
        sanitize();
        validate();
    }

    /**
     * Check optional id components for empty strings and sanitize by setting to null or default values.
     */
    protected void sanitize() {
        String protocol = this.protocol;
        if (Strings.isEmpty(protocol)) {
            this.protocol = null;
        }

        String resource = this.resource;
        if (Strings.isEmpty(resource)) {
            this.resource = null;
        }

        String component = this.component;
        if (Strings.isEmpty(component)) {
            this.component = null;
        }
    }

    /*
     * Validate that mandatory id components exist.
     */
    protected void validate() throws IllegalArgumentException {
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

    public synchronized void setProtocol(final String protocol) {
        this.protocol = protocol;
        validate();
        cachedStringRepresentation = null;
    }

    public String getUser() {
        return user;
    }

    public synchronized void setUser(final String user) {
        this.user = user;
        validate();
        cachedStringRepresentation = null;
    }

    public String getContext() {
        return context;
    }

    public synchronized void setContext(final String context) {
        this.context = context;
        validate();
        cachedStringRepresentation = null;
    }

    public String getResource() {
        return resource;
    }

    public synchronized void setResource(final String resource) {
        this.resource = resource;
        validate();
        cachedStringRepresentation = null;
    }

    public String getComponent() {
        return component;
    }

    public synchronized void setComponent(String component) {
        this.component = component;
        cachedStringRepresentation = null;
    }

    public String getCachedStringRepresentation() {
        return cachedStringRepresentation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String context = this.context;
        result = prime * result + ((context == null) ? 0 : context.hashCode());

        String protocol = this.protocol;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());

        String resource = this.resource;
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());

        String user = this.user;
        result = prime * result + ((user == null) ? 0 : user.hashCode());

        String component = this.component;
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
        String context = this.context;
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        String protocol = this.protocol;
        if (protocol == null) {
            if (other.protocol != null) {
                return false;
            }
        } else if (!protocol.equals(other.protocol)) {
            return false;
        }
        String resource = this.resource;
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        String user = this.user;
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        String component = this.component;
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
        String tmp = this.cachedStringRepresentation;
        if (tmp == null) {
            synchronized (this) {
                final StringBuilder b = new StringBuilder(32);
                boolean needSep = false;
                String protocol = this.protocol;
                if (protocol != null) {
                    b.append(protocol);
                    needSep = true;
                }
                String component = this.component;
                if (component != null) {
                    if (protocol != null) {
                        b.append(".");
                    }
                    b.append(component);
                    needSep = true;
                }
                if (needSep) {
                    b.append("://");
                }
                b.append(escape(user, '@')).append('@').append(escape(context, '@', '/'));
                if (resource != null) {
                    b.append('/').append(resource);
                }
                tmp = b.toString();
                this.cachedStringRepresentation = tmp;
            }
        }
        return tmp;
    }

    private Object getStackTraceString() {
        StringBuilder sb = new StringBuilder(1024);
        for (StackTraceElement ste : new Throwable().getStackTrace()) {
            sb.append(ste).append("\n");
        }
        return sb.toString();
    }

    /**
     * If the given input string contains any of the candidate characters those will be escaped via '\'
     *
     * @param str The input string
     * @param chars The characters to escape in the input string
     * @return The given input string with the candidate characters escaped via '\'
     */
    private String escape(String str, char... chars) {
        StringBuilder b = new StringBuilder();
        for (char c : str.toCharArray()) {
            boolean escape = false;
            if (c == '\\') {
                escape = true;
            } else {
                for (char candidate : chars) {
                    if (candidate == c) {
                        escape = true;
                    }
                }
            }
            if (escape) {
                b.append('\\');
            }
            b.append(c);
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

    /**
     * Create a ServerSession from a dummy SessionObject based on the user infos contained in this {@link ID}. This will fail for synthetic {@link ID}s that don't have real userId and userContextId values.
     *
     * @return a ServerSession from a dummy SessionObject based on the user infos contained in this {@link ID}.
     * @throws OXException if no ServerSession can be ceated based upon this {@link ID}.
     */
    public ServerSession toSession() throws OXException {
        UserAndContext userAndContextIDs = IdLookup.getUserAndContextIDs(this);
        SessionObject sessionObject = SessionObjectWrapper.createSessionObject(
            userAndContextIDs.getUserId(),
            userAndContextIDs.getContextId(),
            (resource != null) ? resource : "rt");
        return ServerSessionAdapter.valueOf(sessionObject);
    }

    /**
     * Get the static {@link IDManager} instance that does the Housekeeping for all {@link ID}s.
     *
     * @return The IDManager instance
     * @throws RealtimeException if the IDManager reference is unset
     */
    private IDManager getManager() throws RealtimeException {
        IDManager manager = ID_MANAGER_REF.get();
        if (manager == null) {
            throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create("IDManager instance is missing. Bundle com.openexchange.realtime stopped?");
        }
        return manager;
    }

    /**
     * Get a {@link Lock} for the given scope from this {@link ID} and lock it.
     *
     * @param scope The scope for the {@link Lock}
     * @throws RealtimeException If getting the {@link Lock}/locking fails
     */
    public void lock(String scope) throws RealtimeException {
        getManager().lock(this, scope);
    }

    /**
     * Get a {@link Lock} for the given scope from this {@link ID} and unlock it.
     *
     * @param scope The scope for the {@link Lock}
     * @throws RealtimeException If getting the {@link Lock}/unlocking fails
     */
    public void unlock(String scope) throws RealtimeException {
        getManager().unlock(this, scope);
    }

    /**
     * Check whether this {@link ID} represents an internal client.
     *
     * @return false if this {@link ID} doesn't represent an internal client, true otherwise.
     */
    public boolean isInternal() {
        return INTERNAL_CONTEXT.equals(context);
    }

}

package com.openexchange.realtime.packet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An ID describes a valid sender or recipient of a {@link Stanza}.
 * It consists of an optional channel, a mandatory user name and mandatory
 * context name and an optional resource.
 * Resources are arbitrary Strings that allow the user to specify how he is
 * currently connected to the service (e.g. one resource per client) and by
 * that enable multiple logins from different machines and locations.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ID implements Serializable {

    private static final long serialVersionUID = -5237507998711320109L;

    private static ConcurrentHashMap<ID, ConcurrentHashMap<String, List<IDEventHandler>>> listeners = new ConcurrentHashMap<ID, ConcurrentHashMap<String, List<IDEventHandler>>>();
    
    private String protocol;
    private String user;
    private String context;
    private String resource;
    private String component;
    private boolean internal;

    /**
     * Pattern to match IDs consisting of protocol, user, context and resource
     * e.g. xmpp://user@context/notebook
     * */
    private static final Pattern PATTERN = Pattern.compile("(?:(\\w+?)(\\.\\w+)?://)?([^@]+)@([^/]+)/?(.*)");

    /**
     * Initializes a new {@link ID} by a String with the syntax "xmpp://user@context/resource".
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
     * Check optional id components for emtpy strings and sanitize by setting
     * to null or default values.
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
                component = component.substring(1);
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
            b.append(component).append(":");
            needSep = true;
        }
        if(needSep) {
            b.append("://");
        }
        b.append(user).append('@').append(context);
        if (resource != null) {
            b.append('/').append(resource);
        }
        return b.toString();
    }

    /**
     * Strip protocol and resource from this id so that it only contains
     * user@context information.
     * @return
     */
    public ID toGeneralForm() {
        return new ID(null, component, user, context, null);
    }

    /**
     * Gets a value indicating whether this ID is in general form or not, i.e. if it only contains the mandatory parts and no protocol
     * or concrete resource name.
     *
     * @return <code>true</code> if the ID is in general form, <code>false</code>, otherwise
     */
    public boolean isGeneralForm() {
        return null == protocol && null == resource;
    }
    
    
    /**
     * Sets the whether this ID can be reached in this cluster
     *
     * @param internal The internal to set
     */
    public void setInternal(boolean internal) {
        this.internal = internal;
    }
    
    
    /**
     * Denotes whether this ID can be reached inside this cluster or external to it.
     *
     * @return The internal
     */
    public boolean isInternal() {
        return internal;
    }
    
    public void on(String event, IDEventHandler handler) {
        handlerList(event).add(handler);
    }
    
    public void one(String event, IDEventHandler handler) {
        on(event, new OneOf(handler));
    }
    
    public void off(String event, IDEventHandler handler) {
        handlerList(event).remove(handler);
    }
    
    public void clearListeners() {
        listeners.put(this, null);
    }
    
    public void trigger(String event, Object source, Map<String, Object> properties) {
        for(IDEventHandler handler: handlerList(event)) {
            handler.handle(event, this, source, properties);
        }
        if (event.equals("dispose")) {
            clearListeners();
        }
    }
    
    public void trigger(String event, Object source) {
        trigger(event, source, new HashMap<String, Object>());
    }
    
    private List<IDEventHandler> handlerList(String event) {
        ConcurrentHashMap<String, List<IDEventHandler>> events = listeners.get(this);
        if (events == null) {
            events = new ConcurrentHashMap<String, List<IDEventHandler>>();
            listeners.put(this, events);
        }
        
        List<IDEventHandler> list = events.get(events);
        
        if (list == null) {
            list = new CopyOnWriteArrayList<IDEventHandler>();
            events.put(event, list);
        }
        
        return list;
        
    }
    
    private class OneOf implements IDEventHandler {
        IDEventHandler delegate;
        
        public OneOf(IDEventHandler delegate) {
            super();
            this.delegate = delegate;
        }



        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            delegate.handle(event, id, source, properties);
            id.off(event, this);
        }
        
    }
}

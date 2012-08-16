package com.openexchange.realtime.packet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An ID describes a valid sender or recipient of a {@link Stanza}.
 * It consists of an optional channel, a mandatory user name and mandatory
 * context name and an optional resource.
 * Resources are arbitrary Strings that allow the user to specify how he is
 * currently connected to the service (e.g. one resource per client) and by
 * that enable multiple logins from different machines and locations.   
 * TODO: Blabla....
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ID {

    private String protocol;
    private String user;
    private String context;
    private String resource;

    /**
     * Pattern to match IDs consisting of protocol, user, context and resource
     * e.g. xmpp://user@context/notebook
     * */
    private static final Pattern PATTERN = Pattern.compile("(?:(\\w+)://)?([^@]+)@([^/]+)/?(.*)");

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
        user = matcher.group(2);
        context = matcher.group(3);
        resource = matcher.group(4);

        sanitize();
        validate();
    }

    public ID(final String protocol, final String user, final String context, final String resource) {
        super();
        this.protocol = protocol;
        this.user = user;
        this.context = context;
        this.resource = resource;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
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
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder(32);
        if (protocol != null) {
            b.append(protocol).append("://");
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
        return new ID(null, user, context, null);
    }

}

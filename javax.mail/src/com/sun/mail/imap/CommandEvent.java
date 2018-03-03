
package com.sun.mail.imap;

import com.sun.mail.iap.Argument;

/**
 * {@link CommandEvent} - Passed to registered {@link ProtocolListener} whenever an IMAP command is about to be issued.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class CommandEvent {

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>ResponseEvent</code> */
    public static class Builder {

        private String command;
        private Argument args;
        private String tag;
        private String host;
        private int port;
        private String user;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the command
         */
        public Builder setCommand(String command) {
            this.command = command;
            return this;
        }

        /**
         * Sets the args
         */
        public Builder setArgs(Argument args) {
            this.args = args;
            return this;
        }

        /**
         * Sets the tag
         */
        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Sets the host
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the port
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the user
         */
        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public CommandEvent build() {
            return new CommandEvent(command, args, tag, host, port, user);
        }
    }

    // -----------------------------------------------------------------------------------------------------------

    private final String command;
    private final Argument args;
    private final String tag;
    private final String host;
    private final int port;
    private final String user;

    /**
     * Initializes a new {@link CommandEvent}.
     */
    CommandEvent(String command, Argument args, String tag, String host, int port, String user) {
        this.command = command;
        this.args = args;
        this.tag = tag;
        this.host = host;
        this.port = port;
        this.user = user;
    }

    /**
     * Gets the command; e.g. <code>"LIST"</code> or <code>"FETCH"</code>
     *
     * @return The command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the command's arguments; e.g. <code>"1:* (ENVELOPE)"</code>
     *
     * @return The arguments
     */
    public Argument getArgs() {
        return args;
    }

    /**
     * Gets the tag
     *
     * @return The tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Gets the host name or textual representation of the IP address
     *
     * @return The host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port number
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the user identifier
     *
     * @return The user
     */
    public String getUser() {
        return user;
    }

}

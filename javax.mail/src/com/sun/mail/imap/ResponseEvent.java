
package com.sun.mail.imap;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;

/**
 * {@link ResponseEvent} - Passed to registered {@link ProtocolListener} whenever an IMAP command has been issued.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ResponseEvent {

    /**
     * An enumeration for known server status responses.
     */
    public static enum Status {
        /** Indicates successful completion of the associated command */
        OK,
        /** Indicates unsuccessful completion of the associated command due to an operational error */
        NO,
        /** Reports a protocol-level error in the client's command */
        BAD,
        /** Indicates that the server is about to close the connection */
        BYE;
    }
    
    /**
     * Represents a server status responses.
     */
    public static class StatusResponse {

        /**
         * Gets the status response for specified response instance
         * 
         * @param response The response instance
         * @return The associated status response or <code>null</code>
         */
        public static StatusResponse statusResponseFor(Response response) {
            if (response.isOK()) {
                return new StatusResponse(Status.OK, response);
            }
            if (response.isBAD()) {
                return new StatusResponse(Status.BAD, response);
            }
            if (response.isBYE()) {
                return new StatusResponse(Status.BYE, response);
            }
            if (response.isNO()) {
                return new StatusResponse(Status.NO, response);
            }
            return null;
        }
        
        private Status status;
        private Response response;
        
        private StatusResponse(Status status, Response response) {
            super();
            this.status = status;
            this.response = response;
        }
        
        /**
         * Gets the associated status response from IMAP server
         * 
         * @return The status response
         */
        public Response getResponse() {
            return response;
        }
        
        /**
         * Gets the status
         * 
         * @return The status
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Checks if the associated status response was synthetically created through {@link Response#byeResponse(Exception)} to advertise an I/O error as BYE response.
         * 
         * @return <code>true</code> if synthetic; otherwise <code>false</code>
         */
        public boolean isSynthetic() {
            return response.isSynthetic();
        }

        /**
         * Gets the exception for which the associated status response was synthetically created through {@link Response#byeResponse(Exception)}.
         * 
         * @return The exception or <code>null</code> (if not synthetic)
         */
        public Exception getException() {
            return response.getException();
        }
        
    }

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

        private StatusResponse statusResponse;
        private Response[] responses;
        private String command;
        private Argument args;
        private long executionMillis;
        private long terminatedTimestamp;
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
         * Sets the type
         */
        public Builder setStatusResponse(StatusResponse statusResponse) {
            this.statusResponse = statusResponse;
            return this;
        }

        /**
         * Sets the responses
         */
        public Builder setResponses(Response[] responses) {
            this.responses = responses;
            return this;
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
         * Sets the execution milliseconds
         */
        public Builder setExecutionMillis(long executionMillis) {
            this.executionMillis = executionMillis;
            return this;
        }

        /**
         * Sets the terminated time stamp
         */
        public Builder setTerminatedTmestamp(long terminatedTimestamp) {
            this.terminatedTimestamp = terminatedTimestamp;
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

        public ResponseEvent build() {
            return new ResponseEvent(statusResponse, responses, command, args, executionMillis, terminatedTimestamp, tag, host, port, user);
        }
    }

    // -----------------------------------------------------------------------------------------------------------

    private final StatusResponse statusResponse;
    private final Response[] responses;
    private final String command;
    private final Argument args;
    private final long executionMillis;
    private final long terminatedTimestamp;
    private final String tag;
    private final String host;
    private final int port;
    private final String user;

    /**
     * Initializes a new {@link ResponseEvent}.
     */
    ResponseEvent(StatusResponse statusResponse, Response[] responses, String command, Argument args, long executionMillis, long terminatedTimestamp, String tag, String host, int port, String user) {
        this.statusResponse = statusResponse;
        this.responses = responses;
        this.command = command;
        this.args = args;
        this.executionMillis = executionMillis;
        this.terminatedTimestamp = terminatedTimestamp;
        this.tag = tag;
        this.host = host;
        this.port = port;
        this.user = user;
    }

    /**
     * Gets the server's status response
     *
     * @return The status response
     */
    public StatusResponse getStatusResponse() {
        return statusResponse;
    }

    /**
     * Gets the responses
     *
     * @return The responses
     */
    public Response[] getResponses() {
        return responses;
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
     * Gets the execution milliseconds
     *
     * @return The execution milliseconds
     */
    public long getExecutionMillis() {
        return executionMillis;
    }

    /**
     * Gets the terminated time stamp (UTC)
     *
     * @return The terminated time stamp
     */
    public long getTerminatedTimestamp() {
        return terminatedTimestamp;
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

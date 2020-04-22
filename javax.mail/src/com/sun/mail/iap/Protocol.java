/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.mail.iap;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.net.ssl.SSLSocket;
import org.slf4j.MDC;
import com.sun.mail.imap.CommandExecutor;
import com.sun.mail.imap.GreetingListener;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.ProtocolAccess;
import com.sun.mail.imap.ProtocolListener;
import com.sun.mail.imap.ProtocolListenerCollection;
import com.sun.mail.imap.ResponseEvent;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;

/**
 * General protocol handling code for IMAP-like protocols. <p>
 *
 * The Protocol object is multithread safe.
 *
 * @author  John Mani
 * @author  Max Spivak
 * @author  Bill Shannon
 */

public class Protocol {
    private final boolean auditLogEnabled;
    protected String greeting;
    protected final String host;
    protected final int port;
    protected final String user;
    private Socket socket;
    // in case we turn on TLS, we'll need these later
    protected final boolean quote;
    protected final MailLogger logger;
    protected MailLogger traceLogger;
    protected final Properties props;
    protected String prefix;

    private TraceInputStream traceInput;	// the Tracer
    private volatile ResponseInputStream input;

    private TraceOutputStream traceOutput;	// the Tracer
    private volatile DataOutputStream output;

    private int tagCounter = 0;
    private final String tagPrefix;

    private String localHostName;

    private final List<ResponseHandler> handlers
	    = new CopyOnWriteArrayList<>();

    private volatile long timestamp;

    // package private, to allow testing
    static final AtomicInteger tagNum = new AtomicInteger();

    private static final byte[] CRLF = { (byte)'\r', (byte)'\n'};
 
    /**
     * Constructor. <p>
     * 
     * Opens a connection to the given host at given port.
     *
     * @param host	host to connect to
     * @param port	portnumber to connect to
     * @param props     Properties object used by this protocol
     * @param prefix 	Prefix to prepend to property keys
     * @param isSSL 	use SSL?
     * @param logger 	log messages here
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public Protocol(String host, int port, String user,
		    Properties props, String prefix,
		    boolean isSSL, MailLogger logger)
		    throws IOException, ProtocolException {
	boolean connected = false;		// did constructor succeed?
	tagPrefix = computePrefix(props, prefix);
	try {
	    this.auditLogEnabled = null == props ? false : PropUtil.getBooleanProperty(props, prefix + ".auditLog.enabled", false);
	    this.host = host;
	    this.port = port;
	    this.user = user;
	    this.props = props;
	    this.prefix = prefix;
	    this.logger = logger;
	    traceLogger = logger.getSubLogger("protocol", null);

	    if (props != null) {
	        ProtocolAccess protocolAccess = ProtocolAccess.instanceFor(user, host, port, props);
	        Optional<CommandExecutor> optionalCommandExecutor = IMAPStore.getMatchingCommandExecutor(protocolAccess);
	        if (optionalCommandExecutor.isPresent()) {	            
	            props.put(prefix + ".protocol.info", protocolAccess);
	            props.put(prefix + ".protocol.connector", optionalCommandExecutor.get());
	        }
	    }
	    socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
	    quote = PropUtil.getBooleanProperty(props,
					"mail.debug.quote", false);

	    initStreams();

	    // Read server greeting
	    {
	        ProtocolAccess protocolAccess = ProtocolAccess.instanceFor(this);
            Optional<CommandExecutor> optionalCommandExecutor = IMAPStore.getMatchingCommandExecutor(protocolAccess);
	        if (optionalCommandExecutor.isPresent()) {
	            processGreeting(optionalCommandExecutor.get().readResponse(protocolAccess));
	        } else {
	            processGreeting(readResponse());
	        }
	    }
	    
	    // Call greeting listeners
	    if (null != props) {
	        Object value = props.get(prefix + ".greeting.listeners");
	        if (value instanceof Collection) {
                Collection<GreetingListener> listeners = (Collection<GreetingListener>) value;
                for (GreetingListener greetingListener : listeners) {
                    greetingListener.onGreetingProcessed(greeting, host, port);
                }
            }
	    }

	    timestamp = System.currentTimeMillis();
 
	    connected = true;	// must be last statement in constructor
	} finally {
	    /*
	     * If we get here because an exception was thrown, we need
	     * to disconnect to avoid leaving a connected socket that
	     * no one will be able to use because this object was never
	     * completely constructed.
	     */
	    if (!connected)
		disconnect();
	}
    }

    private void initStreams() throws IOException {
	traceInput = new TraceInputStream(socket.getInputStream(), traceLogger);
	traceInput.setQuote(quote);
	input = new ResponseInputStream(traceInput);

	traceOutput =
	    new TraceOutputStream(socket.getOutputStream(), traceLogger);
	traceOutput.setQuote(quote);
	output = new DataOutputStream(new BufferedOutputStream(traceOutput));
    }

    /**
     * Compute the tag prefix to be used for this connection.
     * Start with "A" - "Z", then "AA" - "ZZ", and finally "AAA" - "ZZZ".
     * Wrap around after that.
     */
    private String computePrefix(Properties props, String prefix) {
    // XXX - in case someone depends on the tag prefix
    if (PropUtil.getBooleanProperty(props,
                    prefix + ".reusetagprefix", false))
        return "A";
    // tag prefix, wrap around after three letters
    int n = tagNum.getAndIncrement() % (26*26*26 + 26*26 + 26);
    String tagPrefix;
    if (n < 26)
        tagPrefix = new String(new char[] { (char)('A' + n) });
    else if (n < (26*26 + 26)) {
        n -= 26;
        tagPrefix = new String(new char[] {
                (char)('A' + n/26), (char)('A' + n%26) });
    } else {
        n -= (26*26 + 26);
        tagPrefix = new String(new char[] {
        (char)('A' + n/(26*26)),
        (char)('A' + (n%(26*26))/26),
        (char)('A' + n%26) });
    }
    return tagPrefix;
    }

    /**
     * Constructor for debugging.
     *
     * @param in	the InputStream to read from
     * @param out	the PrintStream to write to
     * @param props     Properties object used by this protocol
     * @param debug	true to enable debugging output
     * @exception	IOException	for I/O errors
     */
    public Protocol(InputStream in, PrintStream out, Properties props,
				boolean debug) throws IOException {
    prefix = "mail.imap";
    this.auditLogEnabled = null == props ? false : PropUtil.getBooleanProperty(props, "mail.imap" + ".auditLog.enabled", false);
    this.host = "localhost";
	this.port = 143;
	this.user = null;
	this.props = props;
	this.quote = false;
	tagPrefix = null == props ? null : computePrefix(props, "mail.imap");
	logger = new MailLogger(this.getClass(), "DEBUG", debug, System.out);
	traceLogger = logger.getSubLogger("protocol", null);

	// XXX - inlined initStreams, won't allow later startTLS
	traceInput = new TraceInputStream(in, traceLogger);
	traceInput.setQuote(quote);
	input = new ResponseInputStream(traceInput);

	traceOutput = new TraceOutputStream(out, traceLogger);
	traceOutput.setQuote(quote);
	output = new DataOutputStream(new BufferedOutputStream(traceOutput));

        timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the timestamp.
     *
     * @return	the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Clears response handlers.
     */
    public void clearHandlers() {
    handlers.clear();
    }
    
    /**
     * Adds a response handler.
     *
     * @param	h	the response handler
     */
    public void addResponseHandler(ResponseHandler h) {
	handlers.add(h);
    }

    /**
     * Removed the specified response handler.
     *
     * @param	h	the response handler
     */
    public void removeResponseHandler(ResponseHandler h) {
	handlers.remove(h);
    }

    /**
     * Notify response handlers
     *
     * @param	responses	the responses
     */
    public void notifyResponseHandlers(Response[] responses) {
	if (handlers.isEmpty()) {
	    return;
	}

	for (Response r : responses) {
	    if (r != null) {
		for (ResponseHandler rh : handlers) {
		    if (rh != null) {
			rh.handleResponse(r);
		    }
		}
	    }
	}
    }

    protected void processGreeting(Response r) throws ProtocolException {
        if (r.isBYE())
	        throw new ConnectionException(this, r);
	    greeting = r.toString();
    }

    /**
     * Return the Protocol's InputStream.
     *
     * @return	the input stream
     */
    protected ResponseInputStream getInputStream() {
	return input;
    }

    /**
     * Return the Protocol's OutputStream
     *
     * @return	the output stream
     */
    protected OutputStream getOutputStream() {
	return output;
    }

    /**
     * Returns whether this Protocol supports non-synchronizing literals
     * Default is false. Subclasses should override this if required
     *
     * @return	true if the server supports non-synchronizing literals
     */
    protected synchronized boolean supportsNonSyncLiterals() {
	return false;
    }

    public Response readResponse() 
		throws IOException, ProtocolException {
	return new Response(this);
    }

    /**
     * Is another response available in our buffer?
     *
     * @return	true if another response is in the buffer
     * @since	JavaMail 1.5.4
     */
    public boolean hasResponse() {
	/*
	 * XXX - Really should peek ahead in the buffer to see
	 * if there's a *complete* response available, but if there
	 * isn't who's going to read more data into the buffer 
	 * until there is?
	 */
	try {
	    return input.available() > 0;
	} catch (IOException ex) {
	}
	return false;
    }

    /**
     * Return a buffer to be used to read a response.
     * The default implementation returns null, which causes
     * a new buffer to be allocated for every response.
     *
     * @return	the buffer to use
     * @since	JavaMail 1.4.1
     */
    protected ByteArray getResponseBuffer() {
	return null;
    }

    public String writeCommand(String command, Argument args) 
        throws IOException, ProtocolException {
	// assert Thread.holdsLock(this);
	// can't assert because it's called from constructor
	String tag = new StringBuilder(6).append('A').append(Integer.toString(tagCounter++, 10)).toString(); // unique tag

	output.writeBytes(tag + " " + command);

	if (args != null) {
	    output.write(' ');
	    args.write(this);
	}

	output.write(CRLF);
	output.flush();
	return tag;
    }

    /**
     * Send a command to the server possibly using a command executor.
     * <p>
     * Collect all responses until either the corresponding command
     * completion response or a BYE response (indicating server failure).
     * Return all the collected responses.
     *
     * @param   command the command
     * @param   args    the arguments
     * @return      array of Response objects returned by the server
     */
    public synchronized Response[] command(String command, Argument args) {
        return command(command, args, Optional.empty());
    }

    /**
     * Send a command to the server possibly using a command executor.
     * <p>
     * Collect all responses until either the corresponding command
     * completion response or a BYE response (indicating server failure).
     * Return all the collected responses.
     *
     * @param   command             the command
     * @param   args                the arguments
     * @param   optionalInterceptor the optional interceptor
     * @return      array of Response objects returned by the server
     */
    public synchronized Response[] command(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor) {
        // Determine suitable executor
        ProtocolAccess protocolAccess = ProtocolAccess.instanceFor(this);
        Optional<CommandExecutor> optionalCommandExecutor = IMAPStore.getMatchingCommandExecutor(protocolAccess);
        if (optionalCommandExecutor.isPresent()) {            
            // Issue command using matching executor
            return optionalCommandExecutor.get().executeCommand(command, args, optionalInterceptor, protocolAccess);
        }

        // No matching executor available
        return executeCommand(command, args, optionalInterceptor);
    }

    /**
     * The no-op interceptor that does nothing at all.
     */
    private static final ResponseInterceptor NOOP_INTERCEPTOR = new ResponseInterceptor() {
        
        @Override
        public boolean intercept(Response r) {
            return false;
        }
    };

    /**
     * Send a command to the server. Collect all responses until either
     * the corresponding command completion response or a BYE response 
     * (indicating server failure).  Return all the collected responses;
     * unless an interceptor has been specified.
     *
     * @param	command	            the command
     * @param	args	            the arguments
     * @param   optionalInterceptor the optional interceptor
     * @return		array of Response objects returned by the server
     */
    public synchronized Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor) {
	commandStart(command);
	List<Response> v = null;
	boolean done = false;
	String tag = null;
	ResponseInterceptor interceptor = optionalInterceptor.orElse(NOOP_INTERCEPTOR);

	// write the command
	ProtocolListenerCollection protocolListeners = IMAPStore.getProtocolListeners();
	boolean measure = null != protocolListeners || auditLogEnabled;
	long start = measure ? System.currentTimeMillis() : 0L;
    try {
	    tag = writeCommand(command, args);
	    v = new java.util.ArrayList<Response>(32);
	} catch (LiteralException lex) {
	    Response lexResponse = lex.getResponse();
	    isInterceptedAndConsumed(lexResponse, interceptor);
        v = new java.util.ArrayList<Response>(1);
        v.add(lexResponse);
	    done = true;
	} catch (Exception ex) {
	    // Convert this into a BYE response
	    Response byeResponse = Response.byeResponse(ex);
	    isInterceptedAndConsumed(byeResponse, interceptor);
        v = new java.util.ArrayList<Response>(1);
        v.add(byeResponse);
	    done = true;
	}

    boolean discardResponses = "true".equals(MDC.get("mail.imap.discardresponses"));
    String lowerCaseCommand = null;

    Response taggedResp = null;
    Response byeResp = null;
    while (!done) {
        Response r = null;
        try {
        r = readResponse();
        } catch (IOException ioex) {
        if (byeResp == null)    // convert this into a BYE response
            byeResp = Response.byeResponse(ioex);
        // else, connection closed after BYE was sent
        break;
        } catch (ProtocolException pex) {
        logger.log(Level.FINE, "ignoring bad response", pex);
        continue; // skip this response
        }

        if (r.isBYE()) {
        byeResp = r;
        continue;
        }

        // If this is a matching command completion response, we are done
        boolean tagged = r.isTagged();
        if (tagged && r.getTag().equals(tag)) {
            taggedResp = r;
            isInterceptedAndConsumed(r, interceptor);
            v.add(r);
            done = true;
        } else {
            if (!isInterceptedAndConsumed(r, interceptor)) {
                if (discardResponses && !tagged && !r.isSynthetic() && (r instanceof IMAPResponse)) {
                    IMAPResponse imapResponse = (IMAPResponse) r;
                    if (lowerCaseCommand == null) {
                        lowerCaseCommand = asciiLowerCase(command);
                    }
                    String key = asciiLowerCase(imapResponse.getKey());
                    if ((key == null) || (lowerCaseCommand.indexOf(key) < 0)) {
                        v.add(r);
                    }
                } else {
                    v.add(r);
                }
            }
        }
    }

    if (byeResp != null) {
        isInterceptedAndConsumed(byeResp, interceptor);
        v.add(byeResp); // must be last
    }

	Response[] responses = v.toArray(new Response[v.size()]);
	v = null;
	long end = System.currentTimeMillis();
        timestamp = end;
	commandEnd();

	if (measure) {
	    long executionMillis = end - start;
	    if (auditLogEnabled) {
            com.sun.mail.imap.AuditLog.LOG.info("command='{}' time={} timestamp={} taggedResponse='{}'", (null == args ? command : command + " " + args.toString()), Long.valueOf(executionMillis), Long.valueOf(end), null == taggedResp ? "<none>" : taggedResp.toString());
	    }
	    if (null != protocolListeners) {
	        Iterator<ProtocolListener> it = protocolListeners.protocolListeners();
	        if (it.hasNext()) {
	            ResponseEvent responseEvent = ResponseEvent.builder()
                    .setArgs(args)
                    .setCommand(command)
                    .setExecutionMillis(executionMillis)
                    .setHost(host)
                    .setPort(port)
                    .setResponses(responses)
                    .setTag(tag)
                    .setTerminatedTmestamp(end)
                    .setStatusResponse(ResponseEvent.StatusResponse.statusResponseFor(responses))
                    .setUser(user)
                    .build();
	            do {
                    ProtocolListener protocolListener = it.next();
                    try {
                        protocolListener.onResponse(responseEvent);
                    } catch (ProtocolException e) {
                        logger.log(java.util.logging.Level.FINE, "Failed protocol listener " + protocolListener.getClass().getName(), e);
                    }
                } while (it.hasNext());
            }
        }
	}

	return responses;
    }

    private boolean isInterceptedAndConsumed(Response response, ResponseInterceptor interceptor) {
        try {
            return interceptor.intercept(response);
        } catch (Exception e) {
            logger.log(java.util.logging.Level.WARNING, "Failed to intercept IMAP response using " + interceptor.getClass().getName(), e);
        }
        return false;
    }

    /**
     * Convenience routine to handle OK, NO, BAD and BYE responses.
     *
     * @param	response	the response
     * @exception	ProtocolException	for protocol failures
     */
    public void handleResult(Response response) throws ProtocolException {
	if (response.isOK())
	    return;
	else if (response.isNO())
	    throw new CommandFailedException(response);
	else if (response.isBAD())
	    throw new BadCommandException(response);
	else if (response.isBYE()) {
	    disconnect();
	    Exception byeException = response.byeException;
	    if (null != byeException) {
	        throw new ConnectionException(this, response, byeException);
        }
	    throw new ConnectionException(this, response);
	}
    }

    /**
     * Convenience routine to handle simple IAP commands
     * that do not have responses specific to that command.
     *
     * @param	cmd	the command
     * @param	args	the arguments
     * @exception	ProtocolException	for protocol failures
     */
    public void simpleCommand(String cmd, Argument args)
			throws ProtocolException {
	// Issue command
	Response[] r = command(cmd, args);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	handleResult(r[r.length-1]);
    }

    /**
     * Start TLS on the current connection.
     * <code>cmd</code> is the command to issue to start TLS negotiation.
     * If the command succeeds, we begin TLS negotiation.
     * If the socket is already an SSLSocket this is a nop and the command
     * is not issued.
     *
     * @param	cmd	the command to issue
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public synchronized void startTLS(String cmd)
				throws IOException, ProtocolException {
	if (socket instanceof SSLSocket)
	    return;	// nothing to do
	simpleCommand(cmd, null);
	socket = SocketFetcher.startTLS(socket, host, props, prefix);
	initStreams();
    }

    /**
     * Start compression on the current connection.
     * <code>cmd</code> is the command to issue to start compression.
     * If the command succeeds, we begin compression.
     *
     * @param	cmd	the command to issue
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public synchronized void startCompression(String cmd)
				throws IOException, ProtocolException {
	// XXX - check whether compression is already enabled?
	simpleCommand(cmd, null);

	// need to create our own Inflater and Deflater in order to set nowrap
	java.util.zip.Inflater inf = new java.util.zip.Inflater(true);
	traceInput = new TraceInputStream(new java.util.zip.InflaterInputStream(
			    socket.getInputStream(), inf), traceLogger);
	traceInput.setQuote(quote);
	input = new ResponseInputStream(traceInput);

	// configure the Deflater
	int level = PropUtil.getIntProperty(props, prefix + ".compress.level",
	                    java.util.zip.Deflater.DEFAULT_COMPRESSION);
	int strategy = PropUtil.getIntProperty(props,
						prefix + ".compress.strategy",
						java.util.zip.Deflater.DEFAULT_STRATEGY);
	if (logger.isLoggable(Level.FINE))
	    logger.log(Level.FINE,
		"Creating Deflater with compression level {0} and strategy {1}",
		new Object[] { level, strategy });
	java.util.zip.Deflater def = new java.util.zip.Deflater(java.util.zip.Deflater.DEFAULT_COMPRESSION, true);
	try {
	    def.setLevel(level);
	} catch (IllegalArgumentException ex) {
	    logger.log(Level.FINE, "Ignoring bad compression level", ex);
	}
	try {
	    def.setStrategy(strategy);
	} catch (IllegalArgumentException ex) {
	    logger.log(Level.FINE, "Ignoring bad compression strategy", ex);
	}
    traceOutput = new TraceOutputStream(new java.util.zip.DeflaterOutputStream(
                socket.getOutputStream(), def, true), traceLogger);
    traceOutput.setQuote(quote);
    output = new DataOutputStream(new BufferedOutputStream(traceOutput));
    }

    /**
     * Is this connection using an SSL socket?
     *
     * @return	true if using SSL
     * @since	JavaMail 1.4.6
     */
    public boolean isSSL() {
	return socket instanceof SSLSocket;
    }

    /**
     * Return the address the socket connected to.
     *
     * @return	the InetAddress the socket is connected to
     * @since	JavaMail 1.5.2
     */
    public InetAddress getInetAddress() {
	return socket.getInetAddress();
    }

    /**
     * Return the SocketChannel associated with this connection, if any.
     *
     * @return	the SocketChannel
     * @since	JavaMail 1.5.2
     */
    public SocketChannel getChannel() {
	SocketChannel ret = socket.getChannel();
	if (ret != null)
	    return ret;

	// XXX - Android is broken and SSL wrapped sockets don't delegate
	// the getChannel method to the wrapped Socket
	if (socket instanceof SSLSocket) {
	    try {
		Field f = socket.getClass().getDeclaredField("socket");
		f.setAccessible(true);
		Socket s = (Socket)f.get(socket);
		ret = s.getChannel();
	    } catch (Exception ex) {
		// ignore anything that might go wrong
	    }
	}
	return ret;
    }

    /**
     * Return the local SocketAddress (host and port) for this
     * end of the connection.
     *
     * @return  the SocketAddress
     * @since   JavaMail 1.6.4
     */
    public SocketAddress getLocalSocketAddress() {
    return socket.getLocalSocketAddress();
    }

    /**
     * Does the server support UTF-8?
     * This implementation returns false.
     * Subclasses should override as appropriate.
     *
     * @return   true if the server supports UTF-8
     * @since JavaMail 1.6.0
     */
    public boolean supportsUtf8() {
	return false;
    }

    /**
     * Disconnect.
     */
    protected synchronized void disconnect() {
    Socket socket = this.socket;
	if (socket != null) {
	    this.socket = null;
	    try {
		socket.close();
	    } catch (IOException e) {
		// ignore it
	    }
	}
    }

    /**
     * Get the name of the local host.
     * The property &lt;prefix&gt;.localhost overrides
     * &lt;prefix&gt;.localaddress,
     * which overrides what InetAddress would tell us.
     *
     * @return	the name of the local host
     */
    protected synchronized String getLocalHost() {
	// get our hostname and cache it for future use
	if (localHostName == null || localHostName.length() <= 0)
	    localHostName =
		    props.getProperty(prefix + ".localhost");
	if (localHostName == null || localHostName.length() <= 0)
	    localHostName =
		    props.getProperty(prefix + ".localaddress");
	try {
	    if (localHostName == null || localHostName.length() <= 0) {
		InetAddress localHost = InetAddress.getLocalHost();
		localHostName = localHost.getCanonicalHostName();
		// if we can't get our name, use local address literal
		if (localHostName == null)
		    // XXX - not correct for IPv6
		    localHostName = "[" + localHost.getHostAddress() + "]";
	    }
	} catch (UnknownHostException uhex) {
	}

	// last chance, try to get our address from our socket
	if (localHostName == null || localHostName.length() <= 0) {
	    if (socket != null && socket.isBound()) {
		InetAddress localHost = socket.getLocalAddress();
		localHostName = localHost.getCanonicalHostName();
		// if we can't get our name, use local address literal
		if (localHostName == null)
		    // XXX - not correct for IPv6
		    localHostName = "[" + localHost.getHostAddress() + "]";
	    }
	}
	return localHostName;
    }

    /**
     * Is protocol tracing enabled?
     *
     * @return	true if protocol tracing is enabled
     */
    protected boolean isTracing() {
	return traceLogger.isLoggable(Level.FINEST);
    }

    /**
     * Temporarily turn off protocol tracing, e.g., to prevent
     * tracing the authentication sequence, including the password.
     */
    protected void suspendTracing() {
	if (traceLogger.isLoggable(Level.FINEST)) {
	    traceInput.setTrace(false);
	    traceOutput.setTrace(false);
	}
    }

    /**
     * Resume protocol tracing, if it was enabled to begin with.
     */
    protected void resumeTracing() {
	if (traceLogger.isLoggable(Level.FINEST)) {
	    traceInput.setTrace(true);
	    traceOutput.setTrace(true);
	}
    }

    /**
     * Finalizer.
     */
    @Override
    protected void finalize() throws Throwable {
	try {
	    disconnect();
	} finally {
	    super.finalize();
	}
    }

    /*
     * Probe points for GlassFish monitoring.
     */
    private void commandStart(String command) { }
    private void commandEnd() { }

    /**
     * Gets the host
     *
     * @return The host
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Gets the port
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Gets the user
     *
     * @return The user
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets the properties used by this protocol.
     *
     * @return The properties
     */
    public Properties getProps() {
        return props;
    }

    /**
     * Gets the remote IP address of the end-point this instance is connected to, or <code>null</code> if it is unconnected.
     * 
     * @return The remote IP address, or <code>null</code> if it is unconnected.
     */
    public java.net.InetAddress getRemoteAddress() {
    Socket socket = this.socket;
    return null == socket ? null : socket.getInetAddress();
    }
    
    /**
     * Sets the specified read timeout and returns the previously applicable SO_TIMEOUT value.
     *
     * @param readTimeout The new SO_TIMEOUT to set. A timeout of zero is interpreted as an infinite timeout. A value of less than zero is ignored
     * @return The previously applicable SO_TIMEOUT value
     * @throws ProtocolException If SO_TIMEOUT cannot be set
     */
    public int setAndGetReadTimeout(int readTimeout) throws ProtocolException {
        try {
            Socket socket = this.socket;
            if (null == socket) {
                return -1;
            }

            int to = socket.getSoTimeout();
            if (readTimeout >= 0) {
                socket.setSoTimeout(readTimeout);
            }
            return to;
        } catch (SocketException ex) {
            throw new ProtocolException("can't set read timeout", ex);
        }
    }
    
    private static char[] lowercases = { '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014', '\015', '\016', '\017', '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035', '\036', '\037', '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056', '\057', '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077', '\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157', '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137', '\140', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157', '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };

    /**
     * Fast lower-case conversion.
     *
     * @param s The string
     * @return The lower-case string
     */
    private static String asciiLowerCase(String s) {
        if (null == s) {
            return null;
        }

        char[] c = null;
        int i = s.length();

        // look for first conversion
        while (i-- > 0) {
            char c1 = s.charAt(i);
            if (c1 <= 127) {
                char c2 = lowercases[c1];
                if (c1 != c2) {
                    c = s.toCharArray();
                    c[i] = c2;
                    break;
                }
            }
        }

        while (i-- > 0) {
            if (c[i] <= 127) {
                c[i] = lowercases[c[i]];
            }
        }

        return c == null ? s : new String(c);
    }
    
}

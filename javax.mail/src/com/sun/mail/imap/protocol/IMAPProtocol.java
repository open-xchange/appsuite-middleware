/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.imap.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Quota;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;
import com.sun.mail.auth.Ntlm;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Literal;
import com.sun.mail.iap.LiteralException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.SortTerm;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;

/**
 * This class extends the iap.Protocol object and implements IMAP
 * semantics. In general, there is a method corresponding to each
 * IMAP protocol command. The typical implementation issues the
 * appropriate protocol command, collects all responses, processes
 * those responses that are specific to this command and then
 * dispatches the rest (the unsolicited ones) to the dispatcher
 * using the <code>notifyResponseHandlers(r)</code>.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class IMAPProtocol extends Protocol {
    
    private boolean failOnNOFetch = false; // Whether a NO response for issued FETCH throws a CommandFailedException or only returns null
    private boolean connected = false;	// did constructor succeed?
    private boolean rev1 = false;	// REV1 server ?
    private boolean noauthdebug = true;	// hide auth info in debug output
    private boolean authenticated;	// authenticated?
    private boolean logoutSignaled;  // logout signaled?
    // WARNING: authenticated may be set to true in superclass
    //		constructor, don't initialize it here.

    private Map<String, String> capabilities;
    // WARNING: capabilities may be initialized as a result of superclass
    //		constructor, don't initialize it here.
    private List<String> authmechs;
    // WARNING: authmechs may be initialized as a result of superclass
    //		constructor, don't initialize it here.

    protected SearchSequence searchSequence;
    protected String[] searchCharsets; 	// array of search charsets

    private String name;
    private SaslAuthenticator saslAuthenticator;	// if SASL is being used

    private ByteArray ba;		// a buffer for fetchBody

    private static final byte[] CRLF = { (byte)'\r', (byte)'\n'};

    private static final FetchItem[] fetchItems = { };

    /**
     * Constructor.
     * Opens a connection to the given host at given port.
     *
     * @param host	host to connect to
     * @param port	portnumber to connect to
     * @param debug     debug mode
     * @param props     Properties object used by this protocol
     */
    public IMAPProtocol(String name, String host, int port, 
			Properties props, boolean isSSL, MailLogger logger)
			throws IOException, ProtocolException {
	super(host, port, props, "mail." + name, isSSL, logger);

	try {
	    this.name = name;
	    noauthdebug =
		!PropUtil.getBooleanProperty(props, "mail.debug.auth", false);

	    if (capabilities == null) {
            capability();
        }

	    if (hasCapability("IMAP4rev1")) {
            rev1 = true;
        }

	    searchCharsets = new String[2]; // 2, for now.
	    searchCharsets[0] = "UTF-8";
	    searchCharsets[1] = MimeUtility.mimeCharset(
				    MimeUtility.getDefaultJavaCharset()
				);

	    connected = true;	// must be last statement in constructor
	} finally {
	    /*
	     * If we get here because an exception was thrown, we need
	     * to disconnect to avoid leaving a connected socket that
	     * no one will be able to use because this object was never
	     * completely constructed.
	     */
	    if (!connected) {
            disconnect();
        }
	}
    }

    /**
     * Return an array of FetchItem objects describing the
     * FETCH items supported by this protocol.  Subclasses may
     * override this method to combine their FetchItems with
     * the FetchItems returned by the superclass.
     *
     * @since JavaMail 1.4.6
     */
    public FetchItem[] getFetchItems() {
	return fetchItems;
    }

    /**
     * CAPABILITY command.
     *
     * @see "RFC2060, section 6.1.1"
     */
    public void capability() throws ProtocolException {
	// Check CAPABILITY
	final Response[] r = command("CAPABILITY", null);

	if (!r[r.length-1].isOK()) {
        throw new ProtocolException(r[r.length-1].toString());
    }

	capabilities = new HashMap<String, String>(10);
	authmechs = new ArrayList<String>(5);
	for (int i = 0, len = r.length; i < len; i++) {
	    if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

	    final IMAPResponse ir = (IMAPResponse)r[i];

	    // Handle *all* untagged CAPABILITY responses.
	    //   Though the spec seemingly states that only
	    // one CAPABILITY response string is allowed (6.1.1),
	    // some server vendors claim otherwise.
	    if (ir.keyEquals("CAPABILITY")) {
            parseCapabilities(ir);
        }
	}
    }

    /**
     * If the response contains a CAPABILITY response code, extract
     * it and save the capabilities.
     */
    protected void setCapabilities(final Response r) {
	byte b;
	while ((b = r.readByte()) > 0 && b != (byte)'[') {
        ;
    }
	if (b == 0) {
        return;
    }
	String s;
	s = r.readAtom();
	if (!s.equalsIgnoreCase("CAPABILITY")) {
        return;
    }
	capabilities = new HashMap<String, String>(10);
	authmechs = new ArrayList<String>(5);
	parseCapabilities(r);
    }

    /**
     * Parse the capabilities from a CAPABILITY response or from
     * a CAPABILITY response code attached to (e.g.) an OK response.
     */
    protected void parseCapabilities(final Response r) {
	String s;
	while ((s = r.readAtom(']')) != null) {
	    if (s.length() == 0) {
		if (r.peekByte() == (byte)']') {
            break;
        }
		/*
		 * Probably found something here that's not an atom.
		 * Rather than loop forever or fail completely, we'll
		 * try to skip this bogus capability.  This is known
		 * to happen with:
		 *   Netscape Messaging Server 4.03 (built Apr 27 1999)
		 * that returns:
		 *   * CAPABILITY * CAPABILITY IMAP4 IMAP4rev1 ...
		 * The "*" in the middle of the capability list causes
		 * us to loop forever here.
		 */
		r.skipToken();
	    } else {
		capabilities.put(toUpperCase(s), s);
		if (s.regionMatches(true, 0, "AUTH=", 0, 5)) {
		    authmechs.add(s.substring(5));
		    if (logger.isLoggable(Level.FINE))
			logger.fine("AUTH: " + s.substring(5));
		}
	    }
	}
    }

    /**
     * Check the greeting when first connecting; look for PREAUTH response.
     */
    @Override
    protected void processGreeting(final Response r) throws ProtocolException {
	super.processGreeting(r);	// check if it's BAD
	if (r.isOK()) {			// check if it's OK
	    setCapabilities(r);
	    return;
	}
	// only other choice is PREAUTH
	final IMAPResponse ir = (IMAPResponse)r;
	if (ir.keyEquals("PREAUTH")) {
	    authenticatedStatusChanging(true, "PREAUTH", null);
	    authenticated = true;
	    setCapabilities(r);
	} else {
        throw new ConnectionException(this, r);
    }
    }

    
    /**
     * Sets whether a NO response for issued FETCH throws a <tt>CommandFailedException</tt> or only returns <tt>null</tt>
     * 
     * @param failOnNOFetch <code>true</code> to throws a <tt>CommandFailedException</tt>; otherwise return <tt>null</tt> 
     */
    public void setFailOnNOFetch(final boolean failOnNOFetch) {
    this.failOnNOFetch = failOnNOFetch;
    }

    /**
     * Returns <code>true</code> if the connection has been authenticated,
     * either due to a successful login, or due to a PREAUTH greeting response.
     */
    public boolean isAuthenticated() {
	return authenticated;
    }

    /**
     * Returns <code>true</code> if this is a IMAP4rev1 server
     */
    public boolean isREV1() {
	return rev1;
    }

    /**
     * Returns whether this Protocol supports non-synchronizing literals.
     */
    @Override
    protected boolean supportsNonSyncLiterals() {
	return hasCapability("LITERAL+");
    }

    /**
     * Read a response from the server.
     */
    @Override
    public Response readResponse() throws IOException, ProtocolException {
	// assert Thread.holdsLock(this);
	// can't assert because it's called from constructor
	IMAPResponse r = new IMAPResponse(this);
	if (r.keyEquals("FETCH"))
	    r = new FetchResponse(r, getFetchItems());
	return r;
    }

    /**
     * Check whether the given capability is supported by
     * this server. Returns <code>true</code> if so, otherwise
     * returns false.
     */
    public boolean hasCapability(String cap) {
    if (null == cap)
        return false;
    String c = cap;
	if (c.endsWith("*")) {
	    c = toUpperCase(c.substring(0, c.length() - 1));
	    final Iterator<String> it = capabilities.keySet().iterator();
	    while (it.hasNext()) {
		if (it.next().startsWith(c)) {
            return true;
        }
	    }
	    return false;
	}
	return capabilities.containsKey(toUpperCase(c));
    }

    /**
     * Gets the server greeting consumed.
     * 
     * @return The server greeting
     */
    public String getGreeting() {
    return greeting;
    }

    /**
     * Return the map of capabilities returned by the server.
     *
      * @since	JavaMail 1.4.1
     */
    public Map<String, String> getCapabilities() {
	return capabilities;
    }

    /**
     * Close socket connection.
     *
     * This method just makes the Protocol.disconnect() method
     * public.
     */
    @Override
    public void disconnect() {
	try {
        super.disconnect();
    } finally {
        if (!logoutSignaled) {
            try { authenticatedStatusChanging(false, null, null); } catch(Exception x) {/*ignore*/}
            logoutSignaled = true;
        }
        authenticated = false;	// just in case
    }
    }

    /**
     * The NOOP command.
     *
     * @see "RFC2060, section 6.1.2"
     */
    public void noop() throws ProtocolException {
	logger.fine("IMAPProtocol noop");
	simpleCommand("NOOP", null);
    }

    /**
     * The NOOP command with an extra argument.
     *
     * @param extraArgument The extra argument
     */
    public void noop(final String extraArgument) {
    logger.fine("IMAPProtocol noop extra argument");
    final Response[] r = command("NOOP " + extraArgument, null);
    // Ignore failed, therefore set last response to null
    r[r.length - 1] = null;
    notifyResponseHandlers(r);
    }

    /**
     * LOGOUT Command.
     *
     * @see "RFC2060, section 6.1.3"
     */
    public void logout() throws ProtocolException {
    boolean notified = false;
	try {
	    authenticatedStatusChanging(false, null, null);
	    logoutSignaled = true;

	    final Response[] r = command("LOGOUT", null);

	    authenticated = false;
	    notified = true;
	    // dispatch any unsolicited responses.
	    //  NOTE that the BYE response is dispatched here as well
	    notifyResponseHandlers(r);
	} finally {
	    disconnect();
	}
    }

    /**
     * LOGIN Command.
     * 
     * @see "RFC2060, section 6.2.2"
     */
    public void login(final String u, final String p) throws ProtocolException {
    try {
        authenticatedStatusChanging(true, u, p);
        final Argument args = new Argument();
        args.writeString(u);
        args.writeString(p);
        Response[] r = null;
        try {
            if (noauthdebug && isTracing()) {
                logger.fine("LOGIN command trace suppressed");
                suspendTracing();
            }
            r = command("LOGIN", args);
        } finally {
            resumeTracing();
        }
        // dispatch untagged responses
        notifyResponseHandlers(r);
        // Handle result of this command
        if (noauthdebug && isTracing())
            logger.fine("LOGIN command result: " + r[r.length - 1]);
        handleResult(r[r.length - 1]);
        // If the response includes a CAPABILITY response code, process it
        setCapabilities(r[r.length - 1]);
        // if we get this far without an exception, we're authenticated
        authenticated = true;
    } finally {
        if (!authenticated) {
            authenticatedStatusChanging(false, u, p);
        }
    }
    }

    /**
     * The AUTHENTICATE command with AUTH=LOGIN authenticate scheme
     *
     * @see "RFC2060, section 6.2.1"
     */
    public synchronized void authlogin(final String u, final String p)
				throws ProtocolException {
    authenticatedStatusChanging(true, u, p);
    try {
        final List<Response> v = new ArrayList<Response>();
        String tag = null;
        Response r = null;
        boolean done = false;

        try {

        if (noauthdebug && isTracing()) {
            logger.fine("AUTHENTICATE LOGIN command trace suppressed");
            suspendTracing();
        }

        try {
            tag = writeCommand("AUTHENTICATE LOGIN", null);
        } catch (final Exception ex) {
            // Convert this into a BYE response
            r = Response.byeResponse(ex);
            done = true;
        }

        final OutputStream os = getOutputStream(); // stream to IMAP server

        /* Wrap a BASE64Encoder around a ByteArrayOutputstream
         * to craft b64 encoded username and password strings
         *
         * Note that the encoded bytes should be sent "as-is" to the
         * server, *not* as literals or quoted-strings.
         *
         * Also note that unlike the B64 definition in MIME, CRLFs 
         * should *not* be inserted during the encoding process. So, I
         * use Integer.MAX_VALUE (0x7fffffff (> 1G)) as the bytesPerLine,
         * which should be sufficiently large !
         *
         * Finally, format the line in a buffer so it can be sent as
         * a single packet, to avoid triggering a bug in SUN's SIMS 2.0
         * server caused by patch 105346.
         */

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
        boolean first = true;

        while (!done) { // loop till we are done
            try {
        	r = readResponse();
            	if (r.isContinuation()) {
        	    // Server challenge ..
        	    String s;
        	    if (first) { // Send encoded username
        		s = u;
        		first = false;
        	    } else 	// Send encoded password
        		s = p;
        	    
        	    // obtain b64 encoded bytes
        	    b64os.write(ASCIIUtility.getBytes(s));
        	    b64os.flush(); 	// complete the encoding

        	    bos.write(CRLF); 	// CRLF termination
        	    os.write(bos.toByteArray()); // write out line
        	    os.flush(); 	// flush the stream
        	    bos.reset(); 	// reset buffer
        	} else if (r.isTagged() && r.getTag().equals(tag)) {
                // Ah, our tagged response
        	    done = true;
            } else if (r.isBYE()) {
                done = true;
            } else {
                v.add(r);
            }
            } catch (final Exception ioex) {
        	// convert this into a BYE response
        	r = Response.byeResponse(ioex);
        	done = true;
            }
        }

        } finally {
            resumeTracing();
        }

        /* Dispatch untagged responses.
         * NOTE: in our current upper level IMAP classes, we add the
         * responseHandler to the Protocol object only *after* the 
         * connection has been authenticated. So, for now, the below
         * code really ends up being just a no-op.
         */
        final Response[] responses = v.toArray(new Response[v.size()]);
        notifyResponseHandlers(responses);

        // Handle the final OK, NO, BAD or BYE response
        if (noauthdebug && isTracing())
            logger.fine("AUTHENTICATE LOGIN command result: " + r);
        handleResult(r);
        // If the response includes a CAPABILITY response code, process it
        setCapabilities(r);
        // if we get this far without an exception, we're authenticated
        authenticated = true;
    } finally {
        if (!authenticated) {
            authenticatedStatusChanging(false, u, p);
        }
    }
    }


    /**
     * The AUTHENTICATE command with AUTH=PLAIN authentication scheme.
     * This is based heavly on the {@link #authlogin} method.
     *
     * @param  authzid		the authorization id
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC3501, section 6.2.2"
     * @see "RFC2595, section 6"
     * @since  JavaMail 1.3.2
     */
    public synchronized void authplain(final String authzid, final String u, final String p)
				throws ProtocolException {
    try {
        authenticatedStatusChanging(true, u, p);
        final List<Response> v = new ArrayList<Response>();
        String tag = null;
        Response r = null;
        boolean done = false;
        try {

            if (noauthdebug && isTracing()) {
                logger.fine("AUTHENTICATE PLAIN command trace suppressed");
                suspendTracing();
            }

            try {
                tag = writeCommand("AUTHENTICATE PLAIN", null);
            } catch (final Exception ex) {
                // Convert this into a BYE response
                r = Response.byeResponse(ex);
                done = true;
            }

            final OutputStream os = getOutputStream(); // stream to IMAP server

            /* Wrap a BASE64Encoder around a ByteArrayOutputstream
             * to craft b64 encoded username and password strings
             *
             * Note that the encoded bytes should be sent "as-is" to the
             * server, *not* as literals or quoted-strings.
             *
             * Also note that unlike the B64 definition in MIME, CRLFs
             * should *not* be inserted during the encoding process. So, I
             * use Integer.MAX_VALUE (0x7fffffff (> 1G)) as the bytesPerLine,
             * which should be sufficiently large !
             *
             * Finally, format the line in a buffer so it can be sent as
             * a single packet, to avoid triggering a bug in SUN's SIMS 2.0
             * server caused by patch 105346.
             */

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);

            while (!done) { // loop till we are done
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        // Server challenge ..
                        final String nullByte = "\0";
                        final String s = (authzid == null ? "" : authzid) + nullByte + u + nullByte + p;

                        // obtain b64 encoded bytes
                        b64os.write(ASCIIUtility.getBytes(s));
                        b64os.flush(); // complete the encoding

                        bos.write(CRLF); // CRLF termination
                        os.write(bos.toByteArray()); // write out line
                        os.flush(); // flush the stream
                        bos.reset(); // reset buffer
                    } else if (r.isTagged() && r.getTag().equals(tag)) {
                        // Ah, our tagged response
                        done = true;
                    } else if (r.isBYE()) {
                        done = true;
                    } else {
                        v.add(r);
                    }
                } catch (final Exception ioex) {
                    // convert this into a BYE response
                    r = Response.byeResponse(ioex);
                    done = true;
                }
            }

        } finally {
            resumeTracing();
        }
        /* Dispatch untagged responses.
         * NOTE: in our current upper level IMAP classes, we add the
         * responseHandler to the Protocol object only *after* the
         * connection has been authenticated. So, for now, the below
         * code really ends up being just a no-op.
         */
        final Response[] responses = v.toArray(new Response[v.size()]);
        notifyResponseHandlers(responses);
        // Handle the final OK, NO, BAD or BYE response
        if (noauthdebug && isTracing())
            logger.fine("AUTHENTICATE PLAIN command result: " + r);
        handleResult(r);
        // If the response includes a CAPABILITY response code, process it
        setCapabilities(r);
        // if we get this far without an exception, we're authenticated
        authenticated = true;
    } finally {
        if (!authenticated) {
            authenticatedStatusChanging(false, u, p);
        }
    }
    }

    /**
     * The AUTHENTICATE command with AUTH=NTLM authentication scheme.
     * This is based heavly on the {@link #authlogin} method.
     *
     * @param  authzid		the authorization id
     * @param  u		the username
     * @param  p		the password
     * @throws ProtocolException as thrown by {@link Protocol#handleResult}.
     * @see "RFC3501, section 6.2.2"
     * @see "RFC2595, section 6"
     * @since  JavaMail 1.4.3
     */
    public synchronized void authntlm(final String authzid, final String u, final String p)
				throws ProtocolException {
    authenticatedStatusChanging(true, u, p);
    try {
        final List<Response> v = new ArrayList<Response>();
        String tag = null;
        Response r = null;
        boolean done = false;
        final String type1Msg = null;
        final int flags = PropUtil.getIntProperty(props, "mail." + name + ".auth.ntlm.flags", 0);
        final String domain = props.getProperty("mail." + name + ".auth.ntlm.domain", "");
        Ntlm ntlm = new Ntlm(domain, getLocalHost(), u, p, logger);
        try {

            if (noauthdebug && isTracing()) {
                logger.fine("AUTHENTICATE NTLM command trace suppressed");
                suspendTracing();
            }

            try {
                tag = writeCommand("AUTHENTICATE NTLM", null);
            } catch (final Exception ex) {
                // Convert this into a BYE response
                r = Response.byeResponse(ex);
                done = true;
            }

            final OutputStream os = getOutputStream(); // stream to IMAP server
            boolean first = true;

            while (!done) { // loop till we are done
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        // Server challenge ..
                        String s;
                        if (first) {
                            s = ntlm.generateType1Msg(flags);
                            first = false;
                        } else {
                            s = ntlm.generateType3Msg(r.getRest());
                        }

                        os.write(ASCIIUtility.getBytes(s));
                        os.write(CRLF); // CRLF termination
                        os.flush(); // flush the stream
                    } else if (r.isTagged() && r.getTag().equals(tag)) {
                        // Ah, our tagged response
                        done = true;
                    } else if (r.isBYE()) {
                        done = true;
                    } else {
                        v.add(r);
                    }
                } catch (final Exception ioex) {
                    // convert this into a BYE response
                    r = Response.byeResponse(ioex);
                    done = true;
                }
            }

        } finally {
            resumeTracing();
        }
        /*
         * Dispatch untagged responses.
         * NOTE: in our current upper level IMAP classes, we add the
         * responseHandler to the Protocol object only *after* the
         * connection has been authenticated. So, for now, the below
         * code really ends up being just a no-op.
         */
        final Response[] responses = v.toArray(new Response[v.size()]);
        notifyResponseHandlers(responses);
        // Handle the final OK, NO, BAD or BYE response
        if (noauthdebug && isTracing())
            logger.fine("AUTHENTICATE NTLM command result: " + r);
        handleResult(r);
        // If the response includes a CAPABILITY response code, process it
        setCapabilities(r);
        // if we get this far without an exception, we're authenticated
        authenticated = true;
    } finally {
        if (!authenticated) {
            authenticatedStatusChanging(false, u, p);
        }
    }
    }

    /**
     * SASL-based login.
     */
    public void sasllogin(final String[] allowed, final String realm, final String authzid,
				final String u, final String p) throws ProtocolException {
    authenticatedStatusChanging(true, u, p);
    if (saslAuthenticator == null) {
	    try {
		final Class sac = Class.forName(
		    "com.sun.mail.imap.protocol.IMAPSaslAuthenticator");
		final Constructor c = sac.getConstructor(new Class[] {
					IMAPProtocol.class,
					String.class,
					Properties.class,
					MailLogger.class,
					String.class
					});
		saslAuthenticator = (SaslAuthenticator)c.newInstance(
					new Object[] {
					this,
					name,
					props,
					logger,
					host
					});
	    } catch (Exception ex) {
		logger.log(Level.FINE, "Can't load SASL authenticator", ex);
		// probably because we're running on a system without SASL
		return;	// not authenticated, try without SASL
	    }
	}

	// were any allowed mechanisms specified?
	List<String> v;
	if (allowed != null && allowed.length > 0) {
	    // remove anything not supported by the server
	    v = new ArrayList<String>(allowed.length);
	    for (int i = 0; i < allowed.length; i++) {
            if (authmechs.contains(allowed[i])) {
                v.add(allowed[i]);
            }
        }
	} else {
	    // everything is allowed
	    v = authmechs;
	}
	final String[] mechs = v.toArray(new String[v.size()]);

	try {

	    if (noauthdebug && isTracing()) {
		logger.fine("SASL authentication command trace suppressed");
		suspendTracing();
	    }

	    if (saslAuthenticator.authenticate(mechs, realm, authzid, u, p)) {
		if (noauthdebug && isTracing())
		    logger.fine("SASL authentication succeeded");
		authenticated = true;
	    } else {
		if (noauthdebug && isTracing())
		    logger.fine("SASL authentication failed");
	    }
	} finally {
	    resumeTracing();
	}
    }

    // XXX - for IMAPSaslAuthenticator access to protected method
    OutputStream getIMAPOutputStream() {
	return getOutputStream();
    }

    /**
     * PROXYAUTH Command.
     * 
     * @see "Netscape/iPlanet/SunONE Messaging Server extension"
     */
    public void proxyauth(final String u) throws ProtocolException {
	final Argument args = new Argument();
	args.writeString(u);

	simpleCommand("PROXYAUTH", args);
    }

    /**
     * ID Command, for Yahoo! Mail IMAP server.
     *
     * See <A HREF="http://en.wikipedia.org/wiki/Yahoo%21_Mail#Free_IMAP_and_SMTPs_access">
     * http://en.wikipedia.org/wiki/Yahoo%21_Mail#Free_IMAP_and_SMTPs_access</A>
     *
     * @since JavaMail 1.4.4
     */
    public void id(final String guid) throws ProtocolException {
	/*
	 * XXX - need to be able to write a string instead
	 * of an astring for the following to work.
	Argument garg = new Argument();
	garg.writeString("GUID");
	garg.writeString(guid);
	Argument args = new Argument();
	args.writeArgument(garg);
	simpleCommand("ID", args);
	 */
	simpleCommand("ID (\"GUID\" \"" + guid + "\")", null);
    }

    /**
     * STARTTLS Command.
     * 
     * @see "RFC3501, section 6.2.1"
     */
    public void startTLS() throws ProtocolException {
	try {
	    super.startTLS("STARTTLS");
	} catch (ProtocolException pex) {
	    logger.log(Level.FINE, "STARTTLS ProtocolException", pex);
	    // ProtocolException just means the command wasn't recognized,
	    // or failed.  This should never happen if we check the
	    // CAPABILITY first.
	    throw pex;
	} catch (Exception ex) {
	    logger.log(Level.FINE, "STARTTLS Exception", ex);
	    // any other exception means we have to shut down the connection
	    // generate an artificial BYE response and disconnect
	    final Response[] r = { Response.byeResponse(ex) };
	    notifyResponseHandlers(r);
	    disconnect();
	    throw new ProtocolException("STARTTLS failure", ex);
	}
    }

    /**
     * SELECT Command.
     *
     * @see "RFC2060, section 6.3.1"
     */
    public MailboxInfo select(String mbox) throws ProtocolException {
	// encode the mbox as per RFC2060
	mbox = BASE64MailboxEncoder.encode(mbox);

	final Argument args = new Argument();	
	args.writeString(mbox);

	final Response[] r = command("SELECT", args);

	// Note that MailboxInfo also removes those responses 
	// it knows about
	final MailboxInfo minfo = new MailboxInfo(r);
	
	// dispatch any remaining untagged responses
	notifyResponseHandlers(r);

	final Response response = r[r.length-1];

	if (response.isOK()) { // command succesful 
	    if (response.toString().indexOf("READ-ONLY") != -1) {
            minfo.mode = Folder.READ_ONLY;
        } else {
            minfo.mode = Folder.READ_WRITE;
        }
	} 
	
	handleResult(response);
	return minfo;
    }

    /**
     * EXAMINE Command.
     *
     * @see "RFC2060, section 6.3.2"
     */
    public MailboxInfo examine(String mbox1) throws ProtocolException {
	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	final Response[] r = command("EXAMINE", args);

	// Note that MailboxInfo also removes those responses
	// it knows about
	final MailboxInfo minfo = new MailboxInfo(r);
	minfo.mode = Folder.READ_ONLY; // Obviously

	// dispatch any remaining untagged responses
	notifyResponseHandlers(r);

	handleResult(r[r.length-1]);
	return minfo;
    }

    /**
     * UNSELECT Command.
     *
     * @see "RFC 3691"
     * @since	JavaMail 1.4.4
     */
    public void unselect() throws ProtocolException {
	if (!hasCapability("UNSELECT")) {
        throw new BadCommandException("UNSELECT not supported");
    }
	simpleCommand("UNSELECT", null);
    }

    /**
     * STATUS Command.
     *
     * @see "RFC2060, section 6.3.10"
     */
    public Status status(String mbox1, String[] items1) 
		throws ProtocolException {
	if (!isREV1() && !hasCapability("IMAP4SUNVERSION")) {
        // STATUS is rev1 only, however the non-rev1 SIMS2.0 
	    // does support this.
	    throw new BadCommandException("STATUS not supported");
    }

	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	final Argument itemArgs = new Argument();
	String[] items = items1;
	if (items == null) {
        items = Status.standardItems;
    }

	for (int i = 0, len = items.length; i < len; i++) {
        itemArgs.writeAtom(items[i]);
    }
	args.writeArgument(itemArgs);

	final Response[] r = command("STATUS", args);

	Status status = null;
	final Response response = r[r.length-1];

	// Grab all STATUS responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("STATUS")) {
		    if (status == null) {
                status = new Status(ir);
            } else {
                Status.add(status, new Status(ir));
            }
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return status;
    }

    /**
     * CREATE Command.
     *
     * @see "RFC2060, section 6.3.3"
     */
    public void create(String mbox1) throws ProtocolException {
	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	simpleCommand("CREATE", args);
    }

    /**
     * DELETE Command.
     *
     * @see "RFC2060, section 6.3.4"
     */
    public void delete(String mbox1) throws ProtocolException {
	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	simpleCommand("DELETE", args);
    }

    /**
     * RENAME Command.
     *
     * @see "RFC2060, section 6.3.5"
     */
    public void rename(String o, String n) throws ProtocolException {
	// encode the mbox as per RFC2060
	o = BASE64MailboxEncoder.encode(o);
	n = BASE64MailboxEncoder.encode(n);

	final Argument args = new Argument();	
	args.writeString(o);
	args.writeString(n);

	simpleCommand("RENAME", args);
    }

    /**
     * SUBSCRIBE Command.
     *
     * @see "RFC2060, section 6.3.6"
     */
    public void subscribe(String mbox1) throws ProtocolException {
	final Argument args = new Argument();	
	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);
	args.writeString(mbox);

	simpleCommand("SUBSCRIBE", args);
    }

    /**
     * UNSUBSCRIBE Command.
     *
     * @see "RFC2060, section 6.3.7"
     */
    public void unsubscribe(String mbox1) throws ProtocolException {
	final Argument args = new Argument();	
	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);
	args.writeString(mbox);

	simpleCommand("UNSUBSCRIBE", args);
    }

    /**
     * LIST Command.
     *
     * @see "RFC2060, section 6.3.8"
     */
    public ListInfo[] list(final String ref, final String pattern) 
			throws ProtocolException {
	return doList("LIST", ref, pattern);
    }

    /**
     * LSUB Command.
     *
     * @see "RFC2060, section 6.3.9"
     */
    public ListInfo[] lsub(final String ref, final String pattern) 
			throws ProtocolException {
	return doList("LSUB", ref, pattern);
    }

    /**
     * Execute the specified LIST-like command (e.g., "LIST" or "LSUB"),
     * using the reference and pattern.
     *
     * @since JavaMail 1.4.6
     */
    protected ListInfo[] doList(String cmd, String ref, String pat)
			throws ProtocolException {
	// encode the mbox as per RFC2060
	ref = BASE64MailboxEncoder.encode(ref);
	pat = BASE64MailboxEncoder.encode(pat);

	final Argument args = new Argument();	
	args.writeString(ref);
	args.writeString(pat);

	final Response[] r = command(cmd, args);

	ListInfo[] linfo = null;
	final Response response = r[r.length-1];

	if (response.isOK()) { // command succesful 
	    final List<ListInfo> v = new ArrayList<ListInfo>(1);
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals(cmd)) {
		    v.add(new ListInfo(ir));
		    r[i] = null;
		}
	    }
	    if (v.size() > 0) {
		linfo = v.toArray(new ListInfo[v.size()]);
	    }
	}
	
	// Dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return linfo;
    }
		
    /**
     * APPEND Command.
     *
     * @see "RFC2060, section 6.3.11"
     */
    public void append(final String mbox, final Flags f, final Date d,
			final Literal data) throws ProtocolException {
	appenduid(mbox, f, d, data, false);	// ignore return value
    }

    /**
     * APPEND Command, return uid from APPENDUID response code.
     *
     * @see "RFC2060, section 6.3.11"
     */
    public AppendUID appenduid(final String mbox, final Flags f, final Date d,
			final Literal data) throws ProtocolException {
	return appenduid(mbox, f, d, data, true);
    }

    public AppendUID appenduid(String mbox1, Flags f, final Date d,
			final Literal data, final boolean uid) throws ProtocolException {
	// encode the mbox as per RFC2060
    String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	if (f != null) { // set Flags in appended message
	    // can't set the \Recent flag in APPEND
	    if (f.contains(Flags.Flag.RECENT)) {
		f = new Flags(f);		// copy, don't modify orig
		f.remove(Flags.Flag.RECENT);	// remove RECENT from copy
	    }

	    /*
	     * HACK ALERT: We want the flag_list to be written out
	     * without any checking/processing of the bytes in it. If
	     * I use writeString(), the flag_list will end up being
	     * quoted since it contains "illegal" characters. So I
	     * am depending on implementation knowledge that writeAtom()
	     * does not do any checking/processing - it just writes out
	     * the bytes. What we really need is a writeFoo() that just
	     * dumps out its argument.
	     */
	    args.writeAtom(createFlagList(f));
	}
	if (d != null) {
        args.writeString(INTERNALDATE.format(d));
    }

	args.writeBytes(data);

	final Response[] r = command("APPEND", args);

	// dispatch untagged responses
	notifyResponseHandlers(r);

	// Handle result of this command
	handleResult(r[r.length-1]);

	if (uid) {
        return getAppendUID(r[r.length-1]);
    } else {
        return null;
    }
    }

    /**
     * If the response contains an APPENDUID response code, extract
     * it and return an AppendUID object with the information.
     */
    private AppendUID getAppendUID(final Response r) {
	if (!r.isOK()) {
        return null;
    }
	byte b;
	while ((b = r.readByte()) > 0 && b != (byte)'[') {
        ;
    }
	if (b == 0) {
        return null;
    }
	String s;
	s = r.readAtom();
	if (!s.equalsIgnoreCase("APPENDUID")) {
        return null;
    }

	final long uidvalidity = r.readLong();
	final long uid = r.readLong();
	return new AppendUID(uidvalidity, uid);
    }

    /**
     * CHECK Command.
     *
     * @see "RFC2060, section 6.4.1"
     */
    public void check() throws ProtocolException {
	simpleCommand("CHECK", null);
    }

    /**
     * CLOSE Command.
     *
     * @see "RFC2060, section 6.4.2"
     */
    public void close() throws ProtocolException {
	simpleCommand("CLOSE", null);
    }

    /**
     * EXPUNGE Command.
     *
     * @see "RFC2060, section 6.4.3"
     */
    public void expunge() throws ProtocolException {
	simpleCommand("EXPUNGE", null);
    }

    /**
     * UID EXPUNGE Command.
     *
     * @see "RFC2359, section 4.1"
     */
    public void uidexpunge(final UIDSet[] set) throws ProtocolException {
	if (!hasCapability("UIDPLUS")) {
        throw new BadCommandException("UID EXPUNGE not supported");
    }
	simpleCommand("UID EXPUNGE " + UIDSet.toString(set), null);
    }

    /**
     * Fetch the BODYSTRUCTURE of the specified message.
     */
    public BODYSTRUCTURE fetchBodyStructure(final int msgno) 
			throws ProtocolException {
	final Response[] r = fetch(msgno, "BODYSTRUCTURE");
	notifyResponseHandlers(r);

	final Response response = r[r.length-1];
	if (response.isOK()) {
        return (BODYSTRUCTURE)FetchResponse.getItem(r, msgno, 
					BODYSTRUCTURE.class);
    } else if (response.isNO()) {
        return null;
    } else {
	    handleResult(response);
	    return null;
	}
    }

    /**
     * Fetch the BODYSTRUCTURE of the specified message.
     */
    public BODYSTRUCTURE fetchBodyStructure(final long uid) 
            throws ProtocolException {
    final Response[] r = fetch(uid, "BODYSTRUCTURE");
    notifyResponseHandlers(r);

    final Response response = r[r.length-1];
    if (response.isOK()) {
        return (BODYSTRUCTURE)FetchResponse.getItem(r, BODYSTRUCTURE.class);
    } else if (response.isNO()) {
        return null;
    } else {
        handleResult(response);
        return null;
    }
    }

    /**
     * Fetch given BODY section, without marking the message
     * as SEEN.
     */
    public BODY peekBody(final int msgno, final String section)
			throws ProtocolException {
	return fetchBody(msgno, section, true);
    }

    /**
     * Fetch given BODY section, without marking the message
     * as SEEN.
     */
    public BODY peekBody(final long uid, final String section)
            throws ProtocolException {
    return fetchBody(uid, section, true);
    }

    /**
     * Fetch given BODY section.
     */
    public BODY fetchBody(final int msgno, final String section)
			throws ProtocolException {
	return fetchBody(msgno, section, false);
    }

    /**
     * Fetch given BODY section.
     */
    public BODY fetchBody(final long uid, final String section)
            throws ProtocolException {
    return fetchBody(uid, section, false);
    }

    protected BODY fetchBody(final int msgno, final String section, final boolean peek)
			throws ProtocolException {
	Response[] r;

	if (peek) {
        r = fetch(msgno,
		     "BODY.PEEK[" + (section == null ? "]" : section + "]"));
    } else {
        r = fetch(msgno,
		     "BODY[" + (section == null ? "]" : section + "]"));
    }

	notifyResponseHandlers(r);

	final Response response = r[r.length-1];
	if (response.isOK()) {
        return (BODY)FetchResponse.getItem(r, msgno, BODY.class);
    } else if (response.isNO()) {
        if (failOnNOFetch) {
            throw new CommandFailedException(response);
        }
        return null;
    } else {
	    handleResult(response);
	    return null;
	}
    }

    protected BODY fetchBody(final long uid, final String section, final boolean peek)
        throws ProtocolException {
    Response[] r;
    
    if (peek) {
        r = fetch(uid,
             "BODY.PEEK[" + (section == null ? "]" : section + "]"));
    } else {
        r = fetch(uid,
             "BODY[" + (section == null ? "]" : section + "]"));
    }
    
    notifyResponseHandlers(r);
    
    final Response response = r[r.length-1];
    if (response.isOK()) {
        return (BODY)FetchResponse.getItem(r, BODY.class);
    } else if (response.isNO()) {
        if (failOnNOFetch) {
            throw new CommandFailedException(response);
        }
        return null;
    } else {
        handleResult(response);
        return null;
    }
    }

    /**
     * Partial FETCH of given BODY section, without setting SEEN flag.
     */
    public BODY peekBody(final int msgno, final String section, final int start, final int size)
			throws ProtocolException {
	return fetchBody(msgno, section, start, size, true, null);
    }

    /**
     * Partial FETCH of given BODY section.
     */
    public BODY fetchBody(final int msgno, final String section, final int start, final int size)
			throws ProtocolException {
	return fetchBody(msgno, section, start, size, false, null);
    }

    /**
     * Partial FETCH of given BODY section, without setting SEEN flag.
     */
    public BODY peekBody(final int msgno, final String section, final int start, final int size,
				final ByteArray ba) throws ProtocolException {
	return fetchBody(msgno, section, start, size, true, ba);
    }

    /**
     * Partial FETCH of given BODY section.
     */
    public BODY fetchBody(final int msgno, final String section, final int start, final int size,
				final ByteArray ba) throws ProtocolException {
	return fetchBody(msgno, section, start, size, false, ba);
    }

    /**
     * Partial UID FETCH of given BODY section, without setting SEEN flag.
     */
    public BODY peekBody(final long msgno, final String section, final int start, final int size,
                final ByteArray ba) throws ProtocolException {
    return fetchBody(msgno, section, start, size, true, ba);
    }

    /**
     * Partial UID FETCH of given BODY section.
     */
    public BODY fetchBody(final long msgno, final String section, final int start, final int size,
                final ByteArray ba) throws ProtocolException {
    return fetchBody(msgno, section, start, size, false, ba);
    }

    protected BODY fetchBody(final int msgno, final String section, final int start, final int size,
			final boolean peek, final ByteArray ba) throws ProtocolException {
    	this.ba = ba;	// save for later use by getResponseBuffer
    	StringBuilder sb = new StringBuilder(32);
        sb.append((peek ? "BODY.PEEK[" : "BODY[" ));
        sb.append((section == null ? "]<" : (section +"]<")));
        sb.append(start).append('.').append(size).append('>');
    	final Response[] r = fetch(msgno, sb.toString());
    	sb = null;
    
    	notifyResponseHandlers(r);
    
    	final Response response = r[r.length-1];
    	if (response.isOK()) {
            return (BODY)FetchResponse.getItem(r, msgno, BODY.class);
        } else if (response.isNO()) {
            return null;
        } else {
    	    handleResult(response);
    	    return null;
    	}
    }

    protected BODY fetchBody(final long msgno, final String section, final int start, final int size,
        final boolean peek, final ByteArray ba) throws ProtocolException {
        this.ba = ba;   // save for later use by getResponseBuffer
        StringBuilder sb = new StringBuilder(32);
        sb.append((peek ? "BODY.PEEK[" : "BODY[" ));
        sb.append((section == null ? "]<" : (section +"]<")));
        sb.append(start).append('.').append(size).append('>');
        final Response[] r = fetch(msgno, sb.toString());
        sb = null;
        
        notifyResponseHandlers(r);
        
        final Response response = r[r.length-1];
        if (response.isOK()) {
            return (BODY)FetchResponse.getItem(r, BODY.class);
        } else if (response.isNO()) {
            return null;
        } else {
            handleResult(response);
            return null;
        }
    }

    /**
     * Return a buffer to read a response into.
     * The buffer is provided by fetchBody and is
     * used only once.
     */
    @Override
    protected ByteArray getResponseBuffer() {
	final ByteArray ret = ba;
	ba = null;
	return ret;
    }

    /**
     * Fetch the specified RFC822 Data item. 'what' names
     * the item to be fetched. 'what' can be <code>null</code>
     * to fetch the whole message.
     */
    public RFC822DATA fetchRFC822(final int msgno, final String what)
			throws ProtocolException {
	final Response[] r = fetch(msgno,
			     what == null ? "RFC822" : "RFC822." + what
			    );

	// dispatch untagged responses
	notifyResponseHandlers(r);

	final Response response = r[r.length-1]; 
	if (response.isOK()) {
        return (RFC822DATA)FetchResponse.getItem(r, msgno, 
					RFC822DATA.class);
    } else if (response.isNO()) {
        return null;
    } else {
	    handleResult(response);
	    return null;
	}
    }

    /**
     * Fetch the FLAGS for the given message.
     */
    public Flags fetchFlags(final int msgno) throws ProtocolException {
	Flags flags = null;
	final Response[] r = fetch(msgno, "FLAGS");

	// Search for our FLAGS response
	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null ||
		!(r[i] instanceof FetchResponse) ||
		((FetchResponse)r[i]).getNumber() != msgno) {
            continue;
        }		
	    
	    final FetchResponse fr = (FetchResponse)r[i];
	    if ((flags = (Flags)fr.getItem(Flags.class)) != null) {
		r[i] = null; // remove this response
		break;
	    }
	}

	// dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
	return flags;
    }

    /**
     * Fetch the FLAGS for the given message.
     */
    public Flags fetchFlags(final long uid) throws ProtocolException {
    Flags flags = null;
    final Response[] r = fetch(uid, "FLAGS");

    // Search for our FLAGS response
    for (int i = 0, len = r.length; i < len; i++) {
        if (r[i] == null || !(r[i] instanceof FetchResponse)) {
            continue;
        }       
        
        final FetchResponse fr = (FetchResponse)r[i];
        if ((flags = (Flags)fr.getItem(Flags.class)) != null) {
        r[i] = null; // remove this response
        break;
        }
    }

    // dispatch untagged responses
    notifyResponseHandlers(r);
    handleResult(r[r.length-1]);
    return flags;
    }

    /**
     * Fetch the IMAP UID for the given message.
     */
    public UID fetchUID(final int msgno) throws ProtocolException {
	final Response[] r = fetch(msgno, "UID");

	// dispatch untagged responses
	notifyResponseHandlers(r);

	final Response response = r[r.length-1]; 
	if (response.isOK()) {
        return (UID)FetchResponse.getItem(r, msgno, UID.class);
    } else if (response.isNO()) {
        return null;
    } else {
	    handleResult(response);
	    return null; // NOTREACHED
	}
    }
		
    /**
     * Get the sequence number for the given UID. A UID object
     * containing the sequence number is returned. If the given UID
     * is invalid, <code>null</code> is returned.
     */
    public UID fetchSequenceNumber(final long uid) throws ProtocolException {
	UID u = null;
	final Response[] r = fetch(String.valueOf(uid), "UID", true);	

	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null || !(r[i] instanceof FetchResponse)) {
            continue;
        }
	    
	    final FetchResponse fr = (FetchResponse)r[i];
	    if ((u = (UID)fr.getItem(UID.class)) != null) {
		if (u.uid == uid) {
            break;
        } else {
            u = null;
        }
	    }
	}
		
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
	return u;
    }

    /**
     * Get the sequence numbers for UIDs ranging from start till end.
     * UID objects that contain the sequence numbers are returned.
     * If no UIDs in the given range are found, an empty array is returned.
     */
    public UID[] fetchSequenceNumbers(final long start, final long end)
			throws ProtocolException {
	final Response[] r = fetch(String.valueOf(start) + ":" + 
				(end == UIDFolder.LASTUID ? "*" : 
				String.valueOf(end)),
			     "UID", true);	

	UID u;
	final List<UID> v = new ArrayList<UID>(r.length);
	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null || !(r[i] instanceof FetchResponse)) {
            continue;
        }
	    
	    final FetchResponse fr = (FetchResponse)r[i];
	    if ((u = (UID)fr.getItem(UID.class)) != null) {
            v.add(u);
        }
	}
		
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);

	return v.toArray(new UID[v.size()]);
    }

    /**
     * Get the sequence numbers for UIDs ranging from start till end.
     * UID objects that contain the sequence numbers are returned.
     * If no UIDs in the given range are found, an empty array is returned.
     */
    public UID[] fetchSequenceNumbers(final long[] uids) throws ProtocolException {
	final StringBuilder sb = new StringBuilder();
	for (int i = 0; i < uids.length; i++) {
	    if (i > 0) {
            sb.append(",");
        }
	    sb.append(String.valueOf(uids[i]));
	}

	final Response[] r = fetch(sb.toString(), "UID", true);	

	UID u;
	final List<UID> v = new ArrayList<UID>();
	for (int i = 0, len = r.length; i < len; i++) {
	    if (r[i] == null || !(r[i] instanceof FetchResponse)) {
            continue;
        }
	    
	    final FetchResponse fr = (FetchResponse)r[i];
	    if ((u = (UID)fr.getItem(UID.class)) != null) {
            v.add(u);
        }
	}
		
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);

	return v.toArray(new UID[v.size()]);
    }

    public Response[] fetch(final MessageSet[] msgsets, final String what)
			throws ProtocolException {
	return fetch(MessageSet.toString(msgsets), what, false);
    }

    public Response[] fetch(final int start, final int end, final String what)
			throws ProtocolException {
	return fetch(String.valueOf(start) + ":" + String.valueOf(end), 
		     what, false);
    }

    public Response[] fetch(final int msg, final String what) 
			throws ProtocolException {
	return fetch(String.valueOf(msg), what, false);
    }

    public Response[] fetch(final long msg, final String what) 
        throws ProtocolException {
    return fetch(Long.toString(msg), what, true);
    }

    private Response[] fetch(final String msgSequence, final String what, final boolean uid)
			throws ProtocolException {
	if (uid) {
        return command("UID FETCH " + msgSequence +" (" + what + ')',null);
    } else {
        return command("FETCH " + msgSequence + " (" + what + ')', null);
    }
    }

    /**
     * COPY command.
     */
    public void copy(final MessageSet[] msgsets, final String mbox)
			throws ProtocolException {
	copy(MessageSet.toString(msgsets), mbox);
    }

    public void copy(final int start, final int end, final String mbox)
			throws ProtocolException {
	copy(String.valueOf(start) + ":" + String.valueOf(end),
		    mbox);
    }

    private void copy(final String msgSequence, String mbox1)
			throws ProtocolException {
	// encode the mbox as per RFC2060
    String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeAtom(msgSequence);
	args.writeString(mbox);

	simpleCommand("COPY", args);
    }
		    
    public void storeFlags(final MessageSet[] msgsets, final Flags flags, final boolean set)
			throws ProtocolException {
	storeFlags(MessageSet.toString(msgsets), flags, set);
    }

    public void storeFlags(final int start, final int end, final Flags flags, final boolean set)
			throws ProtocolException {
	storeFlags(String.valueOf(start) + ":" + String.valueOf(end),
		   flags, set);
    }

    /**
     * Set the specified flags on this message. <p>
     */
    public void storeFlags(final int msg, final Flags flags, final boolean set)
			throws ProtocolException { 
	storeFlags(String.valueOf(msg), flags, set);
    }

    private void storeFlags(final String msgset, final Flags flags, final boolean set)
			throws ProtocolException {
	Response[] r;
	if (set) {
        r = command("STORE " + msgset + " +FLAGS " + 
			 createFlagList(flags), null);
    } else {
        r = command("STORE " + msgset + " -FLAGS " + 
			createFlagList(flags), null);
    }
	
	// Dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(r[r.length-1]);
    }

    /**
     * Creates an IMAP flag_list from the given Flags object.
     */
    private String createFlagList(final Flags flags) {
	final StringBuilder sb = new StringBuilder();
	sb.append("("); // start of flag_list

	final Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags
	boolean first = true;
	for (int i = 0; i < sf.length; i++) {
	    String s;
	    final Flags.Flag f = sf[i];
	    if (f == Flags.Flag.ANSWERED) {
            s = "\\Answered";
        } else if (f == Flags.Flag.DELETED) {
            s = "\\Deleted";
        } else if (f == Flags.Flag.DRAFT) {
            s = "\\Draft";
        } else if (f == Flags.Flag.FLAGGED) {
            s = "\\Flagged";
        } else if (f == Flags.Flag.RECENT) {
            s = "\\Recent";
        } else if (f == Flags.Flag.SEEN) {
            s = "\\Seen";
        }
        else {
            continue;	// skip it
        }
	    if (first) {
            first = false;
        } else {
            sb.append(' ');
        }
	    sb.append(s);
	}

	final String[] uf = flags.getUserFlags(); // get the user flag strings
	for (int i = 0; i < uf.length; i++) {
	    if (first) {
            first = false;
        } else {
            sb.append(' ');
        }
	    sb.append(uf[i]);
	}

	sb.append(")"); // terminate flag_list
	return sb.toString();
    }

    /**
     * Issue the given search criterion on the specified message sets.
     * Returns array of matching sequence numbers. An empty array
     * is returned if no matches are found.
     *
     * @param	msgsets	array of MessageSets
     * @param	term	SearchTerm
     * @return	array of matching sequence numbers.
     */
    public int[] search(final MessageSet[] msgsets, final SearchTerm term)
			throws ProtocolException, SearchException {
	return search(MessageSet.toString(msgsets), term);
    }

    /**
     * Issue the given search criterion on all messages in this folder.
     * Returns array of matching sequence numbers. An empty array
     * is returned if no matches are found.
     *
     * @param	term	SearchTerm
     * @return	array of matching sequence numbers.
     */
    public int[] search(final SearchTerm term) 
			throws ProtocolException, SearchException {
	return search("ALL", term);
    }

    /*
     * Apply the given SearchTerm on the specified sequence.
     * Returns array of matching sequence numbers. Note that an empty
     * array is returned for no matches.
     */
    private int[] search(final String msgSequence, final SearchTerm term)
			throws ProtocolException, SearchException {
	// Check if the search "text" terms contain only ASCII chars
	if (SearchSequence.isAscii(term)) {
	    try {
		return issueSearch(msgSequence, term, null);
	    } catch (final IOException ioex) { /* will not happen */ }
	}

	/*
	 * The search "text" terms do contain non-ASCII chars. We need to
	 * use SEARCH CHARSET <charset> ...
	 *	The charsets we try to use are UTF-8 and the locale's
	 * default charset. If the server supports UTF-8, great, 
	 * always use it. Else we try to use the default charset.
	 */

	// Cycle thru the list of charsets
	for (int i = 0; i < searchCharsets.length; i++) {
	    if (searchCharsets[i] == null) {
            continue;
        }

	    try {
		return issueSearch(msgSequence, term, searchCharsets[i]);
	    } catch (final CommandFailedException cfx) {
		/*
		 * Server returned NO. For now, I'll just assume that 
		 * this indicates that this charset is unsupported.
		 * We can check the BADCHARSET response code once
		 * that's spec'd into the IMAP RFC ..
		 */
		searchCharsets[i] = null;
		continue;
	    } catch (final IOException ioex) {
		/* Charset conversion failed. Try the next one */
		continue;
	    } catch (final ProtocolException pex) {
		throw pex;
	    } catch (final SearchException sex) {
		throw sex;
	    }
	}

	// No luck.
	throw new SearchException("Search failed");
    }

    /* Apply the given SearchTerm on the specified sequence, using the
     * given charset. <p>
     * Returns array of matching sequence numbers. Note that an empty
     * array is returned for no matches.
     */
    private int[] issueSearch(final String msgSequence, final SearchTerm term,
      			      final String charset) 
	     throws ProtocolException, SearchException, IOException {

	// Generate a search-sequence with the given charset
	final Argument args = getSearchSequence().generateSequence(term, 
			  charset == null ? null : 
					    MimeUtility.javaCharset(charset)
			);
	args.writeAtom(msgSequence);

	Response[] r;

	if (charset == null) {
        r = command("SEARCH", args);
    } else {
        r = command("SEARCH CHARSET " + charset, args);
    }

	final Response response = r[r.length-1];
	int[] matches = null;

	// Grab all SEARCH responses
	if (response.isOK()) { // command succesful
	    final List<Integer> v = new ArrayList<Integer>();
	    int num;
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		// There *will* be one SEARCH response.
		if (ir.keyEquals("SEARCH")) {
		    while ((num = ir.readNumber()) != -1) {
                v.add(Integer.valueOf(num));
            }
		    r[i] = null;
		}
	    }

	    // Copy the list into 'matches'
	    final int vsize = v.size();
	    matches = new int[vsize];
	    for (int i = 0; i < vsize; i++) {
            matches[i] = v.get(i).intValue();
        }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return matches;
    }

    /**
     * Get the SearchSequence object.
     * The SearchSequence object instance is saved in the searchSequence
     * field.  Subclasses of IMAPProtocol may override this method to
     * return a subclass of SearchSequence, in order to add support for
     * product-specific search terms.
     *
     * @since JavaMail 1.4.6
     */
    protected SearchSequence getSearchSequence() {
	if (searchSequence == null)
	    searchSequence = new SearchSequence();
	return searchSequence;
    }

    /**
     * Sort messages in the folder according to the specified sort criteria.
     * If the search term is not null, limit the sort to only the messages
     * that match the search term.
     * Returns an array of sorted sequence numbers. An empty array
     * is returned if no matches are found.
     *
     * @param	term	sort criteria
     * @param	sterm	SearchTerm
     * @return	array of matching sequence numbers.
     *
     * @see	"RFC 5256"
     * @since	JavaMail 1.4.4
     */
    public int[] sort(final SortTerm[] term, final SearchTerm sterm)
			throws ProtocolException, SearchException {
	if (!hasCapability("SORT*")) {
        throw new BadCommandException("SORT not supported");
    }

	if (term == null || term.length == 0) {
        throw new BadCommandException("Must have at least one sort term");
    }

	final Argument args = new Argument();
	final Argument sargs = new Argument();
	for (int i = 0; i < term.length; i++) {
        sargs.writeAtom(term[i].toString());
    }
	args.writeArgument(sargs);	// sort criteria

	args.writeAtom("UTF-8");	// charset specification
	if (sterm != null) {
	    try {
		args.append(getSearchSequence().generateSequence(sterm, "UTF-8"));
	    } catch (final IOException ioex) {
		// should never happen
		throw new SearchException(ioex.toString());
	    }
	} else {
        args.writeAtom("ALL");
    }

	final Response[] r = command("SORT", args);
	final Response response = r[r.length-1];
	int[] matches = null;

	// Grab all SORT responses
	if (response.isOK()) { // command succesful
	    final List<Integer> v = new ArrayList<Integer>(r.length);
	    int num;
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("SORT")) {
		    while ((num = ir.readNumber()) != -1) {
                v.add(Integer.valueOf(num));
            }
		    r[i] = null;
		}
	    }

	    // Copy the vector into 'matches'
	    final int vsize = v.size();
	    matches = new int[vsize];
	    for (int i = 0; i < vsize; i++) {
            matches[i] = v.get(i).intValue();
        }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return matches;
    }

    /**
     * NAMESPACE Command.
     *
     * @see "RFC2342"
     */
    public Namespaces namespace() throws ProtocolException {
	if (!hasCapability("NAMESPACE")) {
        throw new BadCommandException("NAMESPACE not supported");
    }

	final Response[] r = command("NAMESPACE", null);

	Namespaces namespace = null;
	final Response response = r[r.length-1];

	// Grab NAMESPACE response
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("NAMESPACE")) {
		    if (namespace == null) {
                namespace = new Namespaces(ir);
            }
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return namespace;
    }

    /**
     * GETQUOTAROOT Command.
     *
     * Returns an array of Quota objects, representing the quotas
     * for this mailbox and, indirectly, the quotaroots for this
     * mailbox.
     *
     * @see "RFC2087"
     */
    public Quota[] getQuotaRoot(String mbox1) throws ProtocolException {
	if (!hasCapability("QUOTA")) {
        throw new BadCommandException("GETQUOTAROOT not supported");
    }

	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	final Response[] r = command("GETQUOTAROOT", args);

	final Response response = r[r.length-1];

	final Hashtable<String, Quota> tab = new Hashtable<String, Quota>();

	// Grab all QUOTAROOT and QUOTA responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("QUOTAROOT")) {
		    // quotaroot_response
		    //		       ::= "QUOTAROOT" SP astring *(SP astring)

		    // read name of mailbox and throw away
		    ir.readAtomString();
		    // for each quotaroot add a placeholder quota
		    String root = null;
		    while ((root = ir.readAtomString()) != null) {
                tab.put(root, new Quota(root));
            }
		    r[i] = null;
		} else if (ir.keyEquals("QUOTA")) {
		    final Quota quota = parseQuota(ir);
		    final Quota q = tab.get(quota.quotaRoot);
		    if (q != null && q.resources != null) {
			// XXX - should merge resources
		    }
		    tab.put(quota.quotaRoot, quota);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);

	final Quota[] qa = new Quota[tab.size()];
	final Enumeration<Quota> e = tab.elements();
	for (int i = 0; e.hasMoreElements(); i++) {
        qa[i] = e.nextElement();
    }
	return qa;
    }

    /**
     * GETQUOTA Command.
     *
     * Returns an array of Quota objects, representing the quotas
     * for this quotaroot.
     *
     * @see "RFC2087"
     */
    public Quota[] getQuota(final String root) throws ProtocolException {
	if (!hasCapability("QUOTA")) {
        throw new BadCommandException("QUOTA not supported");
    }

	final Argument args = new Argument();	
	args.writeString(root);

	final Response[] r = command("GETQUOTA", args);

	Quota quota = null;
	final List<Quota> v = new ArrayList<Quota>(r.length);
	final Response response = r[r.length-1];

	// Grab all QUOTA responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("QUOTA")) {
		    quota = parseQuota(ir);
		    v.add(quota);
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return v.toArray(new Quota[v.size()]);
    }

    /**
     * SETQUOTA Command.
     *
     * Set the indicated quota on the corresponding quotaroot.
     *
     * @see "RFC2087"
     */
    public void setQuota(final Quota quota) throws ProtocolException {
	if (!hasCapability("QUOTA")) {
        throw new BadCommandException("QUOTA not supported");
    }

	final Argument args = new Argument();	
	args.writeString(quota.quotaRoot);
	final Argument qargs = new Argument();	
	if (quota.resources != null) {
	    for (int i = 0; i < quota.resources.length; i++) {
		qargs.writeAtom(quota.resources[i].name);
		qargs.writeNumber(quota.resources[i].limit);
	    }
	}
	args.writeArgument(qargs);

	final Response[] r = command("SETQUOTA", args);
	final Response response = r[r.length-1];

	// XXX - It's not clear from the RFC whether the SETQUOTA command
	// will provoke untagged QUOTA responses.  If it does, perhaps
	// we should grab them here and return them?

	/*
	Quota quota = null;
	Vector v = new Vector();

	// Grab all QUOTA responses
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse))
		    continue;

		IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("QUOTA")) {
		    quota = parseQuota(ir);
		    v.addElement(quota);
		    r[i] = null;
		}
	    }
	}
	*/

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	/*
	Quota[] qa = new Quota[v.size()];
	v.copyInto(qa);
	return qa;
	*/
    }

    /**
     * Parse a QUOTA response.
     */
    private Quota parseQuota(final Response r) throws ParsingException {
	// quota_response ::= "QUOTA" SP astring SP quota_list
	final String quotaRoot = r.readAtomString();	// quotaroot ::= astring
	final Quota q = new Quota(quotaRoot);
	r.skipSpaces();
	// quota_list ::= "(" #quota_resource ")"
	if (r.readByte() != '(') {
        throw new ParsingException("parse error in QUOTA");
    }

	final List<Quota.Resource> v = new ArrayList<Quota.Resource>();
	while (r.peekByte() != ')') {
	    // quota_resource ::= atom SP number SP number
	    final String name = r.readAtom();
	    if (name != null) {
		final long usage = r.readLong();
		final long limit = r.readLong();
		final Quota.Resource res = new Quota.Resource(name, usage, limit);
		v.add(res);
	    }
	}
	r.readByte();
	q.resources = v.toArray(new Quota.Resource[v.size()]);
	return q;
    }


    /**
     * SETACL Command.
     *
     * @see "RFC2086"
     */
    public void setACL(String mbox, final char modifier, final ACL acl)
				throws ProtocolException {
	if (!hasCapability("ACL")) {
        throw new BadCommandException("ACL not supported");
    }

	// encode the mbox as per RFC2060
	mbox = BASE64MailboxEncoder.encode(mbox);

	final Argument args = new Argument();	
	args.writeString(mbox);
	args.writeString(acl.getName());
	String rights = acl.getRights().toString();
	if (modifier == '+' || modifier == '-') {
        rights = modifier + rights;
    }
	args.writeString(rights);

	final Response[] r = command("SETACL", args);
	final Response response = r[r.length-1];

	// dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
    }

    /**
     * DELETEACL Command.
     *
     * @see "RFC2086"
     */
    public void deleteACL(String mbox, final String user) throws ProtocolException {
	if (!hasCapability("ACL")) {
        throw new BadCommandException("ACL not supported");
    }

	// encode the mbox as per RFC2060
	mbox = BASE64MailboxEncoder.encode(mbox);

	final Argument args = new Argument();	
	args.writeString(mbox);
	args.writeString(user);

	final Response[] r = command("DELETEACL", args);
	final Response response = r[r.length-1];

	// dispatch untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
    }

    /**
     * GETACL Command.
     *
     * @see "RFC2086"
     */
    public ACL[] getACL(String mbox1) throws ProtocolException {
	if (!hasCapability("ACL")) {
        throw new BadCommandException("ACL not supported");
    }

	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	final Response[] r = command("GETACL", args);
	final Response response = r[r.length-1];

	// Grab all ACL responses
	final List<ACL> v = new ArrayList<ACL>(r.length);
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("ACL")) {
		    // acl_data ::= "ACL" SPACE mailbox
		    //		*(SPACE identifier SPACE rights)
		    // read name of mailbox and throw away
		    ir.readAtomString();
		    String name = null;
		    while ((name = ir.readAtomString()) != null) {
			final String rights = ir.readAtomString();
			if (rights == null) {
                break;
            }
			final ACL acl = new ACL(name, new Rights(rights));
			v.add(acl);
		    }
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return v.toArray(new ACL[v.size()]);
    }

    /**
     * LISTRIGHTS Command.
     *
     * @see "RFC2086"
     */
    public Rights[] listRights(String mbox1, final String user)
				throws ProtocolException {
	if (!hasCapability("ACL")) {
        throw new BadCommandException("ACL not supported");
    }

	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);
	args.writeString(user);

	final Response[] r = command("LISTRIGHTS", args);
	final Response response = r[r.length-1];

	// Grab LISTRIGHTS response
	final List<Rights> v = new ArrayList<Rights>(r.length);
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("LISTRIGHTS")) {
		    // listrights_data ::= "LISTRIGHTS" SPACE mailbox
		    //		SPACE identifier SPACE rights *(SPACE rights)
		    // read name of mailbox and throw away
		    ir.readAtomString();
		    // read identifier and throw away
		    ir.readAtomString();
		    String rights;
		    while ((rights = ir.readAtomString()) != null) {
                v.add(new Rights(rights));
            }
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return v.toArray(new Rights[v.size()]);
    }

    /**
     * MYRIGHTS Command.
     *
     * @see "RFC2086"
     */
    public Rights myRights(String mbox1) throws ProtocolException {
	if (!hasCapability("ACL")) {
        throw new BadCommandException("ACL not supported");
    }

	// encode the mbox as per RFC2060
	String mbox = BASE64MailboxEncoder.encode(mbox1);

	final Argument args = new Argument();	
	args.writeString(mbox);

	final Response[] r = command("MYRIGHTS", args);
	final Response response = r[r.length-1];

	// Grab MYRIGHTS response
	Rights rights = null;
	if (response.isOK()) { // command succesful 
	    for (int i = 0, len = r.length; i < len; i++) {
		if (!(r[i] instanceof IMAPResponse)) {
            continue;
        }

		final IMAPResponse ir = (IMAPResponse)r[i];
		if (ir.keyEquals("MYRIGHTS")) {
		    // myrights_data ::= "MYRIGHTS" SPACE mailbox SPACE rights
		    // read name of mailbox and throw away
		    ir.readAtomString();
		    final String rs = ir.readAtomString();
		    if (rights == null) {
                rights = new Rights(rs);
            }
		    r[i] = null;
		}
	    }
	}

	// dispatch remaining untagged responses
	notifyResponseHandlers(r);
	handleResult(response);
	return rights;
    }

    /*
     * The tag used on the IDLE command.  Set by idleStart() and
     * used in processIdleResponse() to determine if the response
     * is the matching end tag.
     */
    private volatile String idleTag;

    /**
     * IDLE Command. <p>
     *
     * If the server supports the IDLE command extension, the IDLE
     * command is issued and this method blocks until a response has
     * been received.  Once the first response has been received, the
     * IDLE command is terminated and all responses are collected and
     * handled and this method returns. <p>
     *
     * Note that while this method is blocked waiting for a response,
     * no other threads may issue any commands to the server that would
     * use this same connection.
     *
     * @see "RFC2177"
     * @since	JavaMail 1.4.1
     */
    public synchronized void idleStart() throws ProtocolException {
	if (!hasCapability("IDLE")) {
        throw new BadCommandException("IDLE not supported");
    }

	final List<Response> v = new ArrayList<Response>();
	boolean done = false;
	Response r = null;

	// write the command
	try {
	    idleTag = writeCommand("IDLE", null);
	} catch (final LiteralException lex) {
	    v.add(lex.getResponse());
	    done = true;
	} catch (final Exception ex) {
	    // Convert this into a BYE response
	    v.add(Response.byeResponse(ex));
	    done = true;
	}

	while (!done) {
	    try {
		r = readResponse();
	    } catch (final IOException ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
	    } catch (final ProtocolException pex) {
		continue; // skip this response
	    }

	    v.add(r);

	    if (r.isContinuation() || r.isBYE()) {
            done = true;
        }
	}

	final Response[] responses = v.toArray(new Response[v.size()]);
	r = responses[responses.length-1];

	// dispatch remaining untagged responses
	notifyResponseHandlers(responses);
	if (!r.isContinuation()) {
        handleResult(r);
    }
    }

    /**
     * While an IDLE command is in progress, read a response
     * sent from the server.  The response is read with no locks
     * held so that when the read blocks waiting for the response
     * from the server it's not holding locks that would prevent
     * other threads from interrupting the IDLE command.
     *
     * @since	JavaMail 1.4.1
     */
    public synchronized Response readIdleResponse() {
	if (idleTag == null)
     {
        return null;	// IDLE not in progress
    }
	Response r = null;
	while (r == null) {
	    try {
		r = readResponse();
	    } catch (final InterruptedIOException iioex) {
		/*
		 * If a socket timeout was set, the read will timeout
		 * before the IDLE times out.  In that case, just go
		 * back and read some more.  After all, the point of
		 * IDLE is to sit here and wait until something happens.
		 */
		if (iioex.bytesTransferred == 0) {
            r = null;	// keep trying
        } else {
            // convert this into a BYE response
		    r = Response.byeResponse(iioex);
        }
	    } catch (final IOException ioex) {
		// convert this into a BYE response
		r = Response.byeResponse(ioex);
	    } catch (final ProtocolException pex) {
		// convert this into a BYE response
		r = Response.byeResponse(pex);
	    }
	}
	return r;
    }

    /**
     * Process a response returned by readIdleResponse().
     * This method will be called with appropriate locks
     * held so that the processing of the response is safe.
     *
     * @since	JavaMail 1.4.1
     */
    public boolean processIdleResponse(final Response r) throws ProtocolException {
	final Response[] responses = new Response[1];
	responses[0] = r;
	boolean done = false;		// done reading responses?
	notifyResponseHandlers(responses);

	if (r.isBYE()) {
        done = true;
    }

	// If this is a matching command completion response, we are done
	if (r.isTagged() && r.getTag().equals(idleTag)) {
        done = true;
    }

	if (done)
     {
        idleTag = null;	// no longer in IDLE
    }

	handleResult(r);
	return !done;
    }

    // the DONE command to break out of IDLE
    private static final byte[] DONE = { 'D', 'O', 'N', 'E', '\r', '\n' };

    /**
     * Abort an IDLE command.  While one thread is blocked in
     * readIdleResponse(), another thread will use this method
     * to abort the IDLE command, which will cause the server
     * to send the closing tag for the IDLE command, which
     * readIdleResponse() and processIdleResponse() will see
     * and terminate the IDLE state.
     *
     * @since	JavaMail 1.4.1
     */
    public void idleAbort() throws ProtocolException {
	final OutputStream os = getOutputStream();
	try {
	    os.write(DONE);
	    os.flush();
	} catch (final IOException ex) {
	    // nothing to do, hope to detect it again later
	}
    }

    /**
     * Invoked whenever the authenticated status is about being changed.
     *
     * @param authenticated The authenticated status to be set
     * @param u The user identifier or <code>"PREAUTH"</code>
     * @param p The password
     */
    protected void authenticatedStatusChanging(final boolean authenticate, final String u, final String p) throws ProtocolException {
        // Nothing
    }

    /** ASCII-wise to upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }
}

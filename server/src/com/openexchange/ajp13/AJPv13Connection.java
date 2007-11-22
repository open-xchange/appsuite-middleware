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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.ajp13;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AJPv13Connection {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13Connection.class);

	public static final int IDLE_STATE = 1;

	public static final int ASSIGNED_STATE = 2;

	private int state;

	private int packageNumber;
	
	private InputStream inputStream;
	
	private OutputStream outputStream;
	
	private AJPv13Listener listener;

	private AJPv13RequestHandler ajpRequestHandler;
	
	public AJPv13Connection(AJPv13Listener listener) {
		this();
		setAndApplyListener(listener);
	}
	
	public AJPv13Connection() {
		super();
		state = IDLE_STATE;
		packageNumber = 0;
	}

	/**
	 * Resets this connection instance and prepares it for next upcoming ajp
	 * cycle. That is the request handler will be set to <code>null</code>,
	 * its state is set to IDLE and the underlying output stream is going to
	 * be flushed.
	 * 
	 */
	public void resetConnection(final boolean releaseRequestHandler) {
		if (state == IDLE_STATE) {
			return;
		}
		if (ajpRequestHandler != null) {
			resetRequestHandler(releaseRequestHandler);
		}
		if (outputStream != null) {
			try {
				outputStream.flush();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		state = IDLE_STATE;
		packageNumber = 0;
	}
	
	private void resetRequestHandler(final boolean release) {
		/*
		 * Discard request handler's reference to this connection if it
		 * ought to be released from it
		 */
		ajpRequestHandler.reset(release);
		if (release) {
			if (AJPv13RequestHandlerPool.isInitialized()) {
				/*
				 * Put resetted request handler back into pool
				 */
				AJPv13RequestHandlerPool.putRequestHandler(ajpRequestHandler);
			}
			/*
			 * Discard request handler reference
			 */
			ajpRequestHandler = null;
		}
	}

	public void processRequest() throws IOException, Exception, AJPv13SocketClosedException {
		if (listener.getSocket().isClosed()) {
			throw new IOException("Socket is closed");
		}
		if (state == IDLE_STATE) {
			state = ASSIGNED_STATE;
			if (ajpRequestHandler == null) {
				/*
				 * Fetch or create a request handler to this newly assigned
				 * connection
				 */
				if (AJPv13RequestHandlerPool.isInitialized()) {
					ajpRequestHandler = AJPv13RequestHandlerPool.getRequestHandler(this);
				} else {
					ajpRequestHandler = new AJPv13RequestHandler();
					ajpRequestHandler.setAJPConnection(this);
				}
			}
		}
		ajpRequestHandler.processPackage();
	}

	public void createResponse() throws Exception {
		if (state != ASSIGNED_STATE) {
			throw new AJPv13InvalidConnectionStateException();
		}
		ajpRequestHandler.createResponse();
	}

	public AJPv13RequestHandler getAjpRequestHandler() {
		return ajpRequestHandler;
	}

	public InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			return (inputStream = new BufferedInputStream(listener.getSocket().getInputStream()));
		}
		return inputStream;
	}

	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			return (outputStream = new BufferedOutputStream(listener.getSocket().getOutputStream(), AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE));
		}
		return outputStream;
	}
	
	public void setSoTimeout(final int millis) throws SocketException {
		listener.getSocket().setSoTimeout(millis);
	}

	public int getPackageNumber() {
		return this.packageNumber;
	}

	public void increasePackageNumber() {
		this.packageNumber++;
	}

	public int getState() {
		return state;
	}
    
	/**
	 * Removes connection's listener reference and therefore implicitely invokes
	 * the <code>resetConnection(true)</code> method
	 * 
	 */
	public void removeListener() {
		/*
		 * Reset connection
		 */
		resetConnection(true);
		/*
		 * Remove underlying input/output stream
		 */
		if (outputStream != null) {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
			this.outputStream = null;
		}
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
			this.inputStream = null;
		}
		/*
		 * Remove associated listener
		 */
		listener = null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(100);
		sb.append("State: ").append(state == IDLE_STATE ? "IDLE" : "ASSIGNED").append(" | ");
		sb.append("Listener: ").append(listener.getListenerName()).append(" | ");
		return sb.toString();
	}
    
    public boolean isAjpListenerNull() {
    	return (listener.getSocket() == null);
    }
    
    public void markListenerProcessing() {
    	listener.markProcessing();
    }
    
    public void markListenerNonProcessing() {
    	listener.markNonProcessing();
    }

	public void setListener(final AJPv13Listener listener) {
		setAndApplyListener(listener);
	}

	private void setAndApplyListener(final AJPv13Listener listener) {
		this.listener = listener;
		try {
			this.inputStream = new BufferedInputStream(listener.getSocket().getInputStream());
			this.outputStream = new BufferedOutputStream(listener.getSocket().getOutputStream());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
}

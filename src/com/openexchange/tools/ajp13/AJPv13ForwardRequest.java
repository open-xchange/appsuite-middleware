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

package com.openexchange.tools.ajp13;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.tools.ajp13.AJPv13Exception.AJPCode;
import com.openexchange.tools.codec.QuotedPrintable;
import com.openexchange.tools.servlet.OXServletInputStream;
import com.openexchange.tools.servlet.OXServletOutputStream;
import com.openexchange.tools.servlet.http.HttpServletRequestWrapper;
import com.openexchange.tools.servlet.http.HttpServletResponseWrapper;
import com.openexchange.tools.servlet.http.HttpSessionManagement;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13ForwardRequest extends AJPv13Request {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13ForwardRequest.class);

	private static final String methods[] = { "OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "PROPFIND",
			"PROPPATCH", "MKCOL", "COPY", "MOVE", "LOCK", "UNLOCK", "ACL", "REPORT", "VERSION-CONTROL", "CHECKIN",
			"CHECKOUT", "UNCHECKOUT", "SEARCH", "MKWORKSPACE", "UPDATE", "LABEL", "MERGE", "BASELINE_CONTROL",
			"MKACTIVITY" };

	private static final HashMap<Integer, String> httpHeaderMapping = new HashMap<Integer, String>();

	private static final HashMap<Integer, String> attributeMapping = new HashMap<Integer, String>();
	
	private static final String DEFAULT_ENCODING = ServerConfig.getProperty(Property.DefaultEncoding);
	
	private static final String CONTENT_TYPE = "content-type";
	
	private static final String CONTENT_LENGTH = "content-length";

	/**
	 * This byte value indicates termination of request.
	 */
	public static final int REQUEST_TERMINATOR = 0xFF;

	static {
		httpHeaderMapping.put(Integer.valueOf(0x01), "accept");
		httpHeaderMapping.put(Integer.valueOf(0x02), "accept-charset");
		httpHeaderMapping.put(Integer.valueOf(0x03), "accept-encoding");
		httpHeaderMapping.put(Integer.valueOf(0x04), "accept-language");
		httpHeaderMapping.put(Integer.valueOf(0x05), "authorization");
		httpHeaderMapping.put(Integer.valueOf(0x06), "connection");
		httpHeaderMapping.put(Integer.valueOf(0x07), CONTENT_TYPE);
		httpHeaderMapping.put(Integer.valueOf(0x08), CONTENT_LENGTH);
		httpHeaderMapping.put(Integer.valueOf(0x09), "cookie");
		httpHeaderMapping.put(Integer.valueOf(0x0a), "cookie2");
		httpHeaderMapping.put(Integer.valueOf(0x0b), "host");
		httpHeaderMapping.put(Integer.valueOf(0x0c), "pragma");
		httpHeaderMapping.put(Integer.valueOf(0x0d), "referer");
		httpHeaderMapping.put(Integer.valueOf(0x0e), "user-agent");
		attributeMapping.put(Integer.valueOf(0x01), "context");
		attributeMapping.put(Integer.valueOf(0x02), "servlet_path");
		attributeMapping.put(Integer.valueOf(0x03), "remote_user");
		attributeMapping.put(Integer.valueOf(0x04), "auth_type");
		attributeMapping.put(Integer.valueOf(0x05), "query_string");
		attributeMapping.put(Integer.valueOf(0x06), "jvm_route");
		attributeMapping.put(Integer.valueOf(0x07), "ssl_cert");
		attributeMapping.put(Integer.valueOf(0x08), "ssl_cipher");
		attributeMapping.put(Integer.valueOf(0x09), "ssl_session");
		attributeMapping.put(Integer.valueOf(0x0a), "req_attribute");
		attributeMapping.put(Integer.valueOf(0x0b), "ssl_key_size");
		attributeMapping.put(Integer.valueOf(0x0c), "secret_attribute");
		attributeMapping.put(Integer.valueOf(0x0d), "stored_method");
		attributeMapping.put(Integer.valueOf(REQUEST_TERMINATOR), "are_done");
	}

	public AJPv13ForwardRequest(final byte[] payloadData) {
		super(payloadData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.AJPv13Request#processRequest(com.openexchange.tools.ajp13.AJPv13RequestHandler)
	 */
	@Override
	public void processRequest(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException {
		processForwardRequest(ajpRequestHandler);
	}
	
	private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	/**
	 * Processes an incoming AJP Forward Package which contains all header data
	 * that are written into servlet's request object.
	 */
	private void processForwardRequest(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException {
		/*
		 * Create Servlet Request with its InputStream
		 */
		final HttpServletRequestWrapper servletRequest = new HttpServletRequestWrapper(ajpRequestHandler);
		servletRequest.setOXInputStream(new OXServletInputStream(ajpRequestHandler.getAJPConnection()));
		/*
		 * Create Servlet Response with its OutputStream
		 */
		final HttpServletResponseWrapper servletResponse = new HttpServletResponseWrapper(servletRequest);
		servletResponse.setOXOutputStream(new OXServletOutputStream(ajpRequestHandler.getAJPConnection()));
		/*
		 * Determine method: If next byte is equal to 0xff then the method is
		 * given by "stored_method" attribute
		 */
		final byte encodedMethod = nextByte();
		if (encodedMethod != -1) {
			servletRequest.setMethod(methods[encodedMethod - 1]);
		}
		/*
		 * Determine protocol
		 */
		try {
			servletRequest.setProtocol(parseString());
		} catch (AJPv13Exception e) {
			throw new AJPv13Exception(AJPCode.UNPARSEABLE_HEADER_FIELD, e, "protocol");
		}
		/*
		 * Determine req_uri
		 */
		String requestURI = null;
		String jsessionID = null;
		try {
			requestURI = parseString();
			final int pos = requestURI.indexOf(AJPv13RequestHandler.JSESSIONID_URI);
			if (pos > -1) {
				jsessionID = requestURI.substring(pos + 12);
				requestURI = requestURI.substring(0, pos);
			}
			servletRequest.setRequestURI(requestURI);
		} catch (AJPv13Exception e) {
			throw new AJPv13Exception(AJPCode.UNPARSEABLE_HEADER_FIELD, e, "req_uri");
		}
		/*
		 * Determine remote_addr
		 */
		try {
			servletRequest.setRemoteAddr(parseString());
		} catch (AJPv13Exception e) {
			throw new AJPv13Exception(AJPCode.UNPARSEABLE_HEADER_FIELD, e, "remote_addr");
		}
		/*
		 * Determine remote_host
		 */
		try {
			servletRequest.setRemoteHost(parseString());
		} catch (AJPv13Exception e) {
			throw new AJPv13Exception(AJPCode.UNPARSEABLE_HEADER_FIELD, e, "remote_host");
		}
		/*
		 * Determine server_name
		 */
		try {
			servletRequest.setServerName(parseString());
		} catch (AJPv13Exception e) {
			throw new AJPv13Exception(AJPCode.UNPARSEABLE_HEADER_FIELD, e, "server_name");
		}
		/*
		 * Determine server_name
		 */
		servletRequest.setServerPort(parseInt());
		/*
		 * Determine is_ssl
		 */
		servletRequest.setSecure(parseBoolean());
		/*
		 * Determine num_headers
		 */
		final int numHeaders = parseInt();
		/*
		 * Determine Request Headers
		 */
		parseRequestHeaders(servletRequest, numHeaders);
		/*
		 * Set important header CONTENT_TYPE which decides whether to further
		 * process an upcoming body request from web server or to terminate
		 * communication after this forward request.
		 */
		if (servletRequest.containsHeader(CONTENT_LENGTH)) {
			ajpRequestHandler.setContentLength(getContentLength(servletRequest));
		}
		/*
		 * Determine if content type inidicates form data
		 */
		if (servletRequest.containsHeader(CONTENT_TYPE)
				&& CONTENT_TYPE_FORM.regionMatches(0, servletRequest.getHeader(CONTENT_TYPE), 0, 33)) {
			ajpRequestHandler.setFormData(true);
		}
		/*
		 * End of payload data NOT reached
		 */
		if (!compareNextByte(0xFF)) {
			/*
			 * Determine Attributes
			 */
			parseAttributes(servletRequest);
			if (encodedMethod == -1 && servletRequest.containsAttribute("stored_method")) {
				servletRequest.setMethod((String) servletRequest.getAttribute("stored_method"));
			}
			if (servletRequest.containsAttribute("query_string")) {
				final String queryStr = (String) servletRequest.getAttribute("query_string");
				servletRequest.setQueryString(queryStr);
				final String[] paramsNVPs = queryStr.split("&");
                for (int i = 0; i < paramsNVPs.length; i++) {
                    paramsNVPs[i] = paramsNVPs[i].trim();
                }
				for (int i = 0; i < paramsNVPs.length; i++) {
					if (paramsNVPs[i].indexOf('=') > -1) {
						final String[] paramNVP = new String[] {
								paramsNVPs[i].substring(0, paramsNVPs[i].indexOf('=')),
								paramsNVPs[i].substring(paramsNVPs[i].indexOf('=') + 1) };
						servletRequest.setParameter(paramNVP[0], decodeQueryStringValue(servletRequest, paramNVP[1]));
					} else {
						servletRequest.setParameter(paramsNVPs[i], "");
					}
				}
			}
		}
		/*
		 * JSESSIONID
		 */
		if (jsessionID == null) {
			/*
			 * Look for JSESSIONID cookie, if request URI does not contain session id
			 */
			checkJSessionIDCookie(servletRequest, servletResponse, ajpRequestHandler);
		} else {
			final int dot = jsessionID.lastIndexOf('.');
			if (dot != -1 && (!AJPv13Config.getJvmRoute().equals(jsessionID.substring(dot + 1)))) {
				/*
				 * JVM route does not match
				 */
				createJSessionIDCookie(servletResponse, ajpRequestHandler);
			} else {
				addJSessionIDCookie(jsessionID, servletResponse, ajpRequestHandler);
			}
		}
		/*
		 * Apply request/response to ajp request handler
		 */
		ajpRequestHandler.setServletRequestObj(servletRequest);
		ajpRequestHandler.setServletResponseObj(servletResponse);
		/*
		 * Create servlet instance dependent on requested URI
		 */
		ajpRequestHandler.setServletInstance(requestURI);
		/*
		 * Apply the servlet path with leading "/" to the request
		 */
		servletRequest.setServletPath(new StringBuilder().append('/').append(ajpRequestHandler.getServletPath())
				.toString());
	}

	private final void parseRequestHeaders(final HttpServletRequestWrapper servletRequest, final int numHeaders)
			throws AJPv13Exception {
		boolean contentTypeSet = false;
		NextHeader: for (int i = 1; i <= numHeaders; i++) {
			final String headerName;
			final String headerValue;
			final boolean isCookie;
			/*
			 * Header name is encoded as an integer value.
			 */
			final byte firstByte = nextByte();
			final byte secondByte = nextByte();
			if (firstByte == (byte) 0xA0) {
				headerName = httpHeaderMapping.get(Integer.valueOf(secondByte));
				if (!contentTypeSet && secondByte == 0x07) {
					servletRequest.setHeader(headerName, parseString(), true);
					contentTypeSet = true;
					continue NextHeader;
				}
				isCookie = (secondByte == 0x09);
			} else {
				headerName = parseString(firstByte, secondByte);
				if (!contentTypeSet && CONTENT_TYPE.equalsIgnoreCase(headerName)) {
					servletRequest.setHeader(headerName, parseString(), true);
					contentTypeSet = true;
					continue NextHeader;
				}
				isCookie = ("cookie".equalsIgnoreCase(headerName));
			}
			headerValue = parseString();
			if (isCookie) {
				final String[] cookies = headerValue.split(";");
				final List<Cookie> cookieList = new ArrayList<Cookie>(cookies.length);
				/*
				 * Version "0" complies with the original cookie specification
				 * drafted by Netscape
				 */
				int version = 0;
				NextCookie: for (int j = 0; j < cookies.length; j++) {
					final Cookie c;
					final String[] cookieNameValuePair = cookies[j].trim().split("=");
					if (cookieNameValuePair.length == 1) {
						c = new Cookie(cookieNameValuePair[0], "");
						c.setVersion(version);
						cookieList.add(c);
					} else if (cookieNameValuePair[0].length() > 0 && cookieNameValuePair[0].charAt(0) == '$') {
						if ("$Version".equalsIgnoreCase(cookieNameValuePair[0])) {
							try {
								version = Integer.parseInt(cookieNameValuePair[1]);
							} catch (NumberFormatException e) {
								LOG.error(new StringBuilder("Special Cookie could not be parsed: $Version=")
										.append(cookieNameValuePair[1]));
								version = 0;
							}
						} else {
							if (LOG.isInfoEnabled()) {
								LOG.info(new StringBuilder(100).append("Special cookie ")
										.append(cookieNameValuePair[0]).append(" not handled, yet!"));
							}
						}
					} else {
						try {
							c = new Cookie(cookieNameValuePair[0], cookieNameValuePair[1]);
						} catch (IllegalArgumentException e) {
							if (LOG.isWarnEnabled()) {
								LOG.warn("Discarding cookie: " + e.getMessage(), e);
							}
							continue NextCookie;
						}
						c.setVersion(version);
						cookieList.add(c);
					}
				}
				servletRequest.setCookies(cookieList.toArray(new Cookie[cookieList.size()]));
			} else {
				servletRequest.setHeader(headerName, headerValue, false);
			}
		}
	}

	private final void parseAttributes(final HttpServletRequestWrapper servletRequest) throws AJPv13Exception {
		byte nextByte = (byte) REQUEST_TERMINATOR;
		while ((nextByte = nextByte()) != ((byte) REQUEST_TERMINATOR)) {
			String attributeName = null;
			String attributeValue = null;
			attributeName = attributeMapping.containsKey(Integer.valueOf(unsignedByte2Int(nextByte))) ? attributeMapping
					.get(Integer.valueOf(unsignedByte2Int(nextByte)))
					: null;
			if (attributeName == null) {
				throw new AJPv13Exception(AJPCode.NO_ATTRIBUTE_NAME, nextByte);
			}
			attributeValue = parseString();
			servletRequest.setAttribute(attributeName, attributeValue);
		}
	}
	
	private static final void checkJSessionIDCookie(final HttpServletRequestWrapper servletRequest,
			final HttpServletResponse resp, final AJPv13RequestHandler ajpRequestHandler) {
		final Cookie[] cookies = servletRequest.getCookies();
		Cookie jsessionIDCookie = null;
		if (cookies != null) {
			NextCookie: for (int i = 0; i < cookies.length && (jsessionIDCookie == null); i++) {
				final Cookie current = cookies[i];
				if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(current.getName())) {
					/*
					 * Check JVM route
					 */
					final int pos = current.getValue().lastIndexOf('.');
					if (pos > -1) {
						final String currentJvmRoute = current.getValue().substring(pos + 1);
						if (!AJPv13Config.getJvmRoute().equals(currentJvmRoute)) {
							/*
							 * Different JVM route detected -> Discard
							 */
							break NextCookie;
						}
						jsessionIDCookie = current;
						ajpRequestHandler.setHttpSessionId(current.getValue(), true);
					} else {
						/*
						 * Value does not apply to pattern [UID].[JVM-ROUTE],
						 * thus only UID is given through special cookie
						 * JSESSIONID
						 */
						ajpRequestHandler.setHttpSessionId(current.getValue(), true);
						break NextCookie;
					}
				}
			}
		}
		if (jsessionIDCookie == null) {
			createJSessionIDCookie(resp, ajpRequestHandler);
		}
	}

	private static final void createJSessionIDCookie(final HttpServletResponse resp,
			final AJPv13RequestHandler ajpRequestHandler) {
		addJSessionIDCookie(null, resp, ajpRequestHandler);
	}

	private static final void addJSessionIDCookie(final String id, final HttpServletResponse resp,
			final AJPv13RequestHandler ajpRequestHandler) {
		final String jsessionIdVal;
		final boolean join;
		if (id == null) {
			/*
			 * Create a new unique id
			 */
			final StringBuilder jsessionIDVal = new StringBuilder(HttpSessionManagement.getNewUniqueId());
			if (AJPv13Config.getJvmRoute() != null && AJPv13Config.getJvmRoute().length() > 0) {
				jsessionIDVal.append('.').append(AJPv13Config.getJvmRoute());
			}
			jsessionIdVal = jsessionIDVal.toString();
			join = true;
		} else {
			jsessionIdVal = id;
			join = false;
		}
		final Cookie jsessionIDCookie = new Cookie(AJPv13RequestHandler.JSESSIONID_COOKIE, jsessionIdVal);
		jsessionIDCookie.setPath("/");
		jsessionIDCookie.setMaxAge(-1); // session cookie
		ajpRequestHandler.setHttpSessionId(jsessionIdVal, join);
		resp.addCookie(jsessionIDCookie);
	}

	private final String parseString() throws AJPv13Exception {
		return parseString(nextByte(), nextByte());
	}

	/**
	 * First two bytes, which indicate length of string, already consumed.
	 */
	private final String parseString(final byte firstByte, final byte secondByte) throws AJPv13Exception {
		/*
		 * Special Byte 0xFF indicates absence of current string value.
		 */
		if (firstByte == ((byte) REQUEST_TERMINATOR) && secondByte == (byte) REQUEST_TERMINATOR) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		boolean encoded = false;
		final int strLength = (unsignedByte2Int(firstByte) << 8) + unsignedByte2Int(secondByte);
		for (int strIndex = 0; strIndex < strLength; strIndex++) {
			final byte b = nextByte();
			if (b < 0) {
				encoded = true;
				sb.append('=').append(Integer.toHexString(unsignedByte2Int(b)));
			} else {
				sb.append((char) b);
			}
		}
		if (nextByte() != 0x00) {
			throw new AJPv13Exception(AJPCode.UNPARSEABLE_STRING);
		}
		if (encoded) {
			try {
				return QuotedPrintable.decodeString(sb.toString(), DEFAULT_ENCODING);
			} catch (IOException e) {
				throw new AJPv13Exception(AJPCode.IO_ERROR, e, e.getMessage());
			} catch (MessagingException e) {
				throw new AJPv13Exception(AJPCode.MESSAGING_ERROR, e, e.getMessage());
			}
		}
		return sb.toString();
	}

	private final boolean parseBoolean() {
		return (nextByte() > 0);
	}

	private final String decodeQueryStringValue(final HttpServletRequestWrapper servletRequest,
			final String queryStringValue) throws UnsupportedEncodingException {
		return URLDecoder.decode(queryStringValue, servletRequest.getCharacterEncoding());
	}

	private final int getContentLength(final HttpServletRequestWrapper servletRequest) {
		if (servletRequest.containsHeader(CONTENT_LENGTH)) {
			return servletRequest.getIntHeader(CONTENT_LENGTH);
		}
		return -1;
	}
}

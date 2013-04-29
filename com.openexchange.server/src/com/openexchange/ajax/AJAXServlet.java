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

package com.openexchange.ajax;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.groupware.upload.impl.UploadFileImpl;
import com.openexchange.groupware.upload.impl.UploadListener;
import com.openexchange.groupware.upload.impl.UploadRegistry;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.java.Charsets;
import com.openexchange.log.ForceLog;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * This is a super class of all AJAX servlets providing common methods.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXServlet extends HttpServlet implements UploadRegistry {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 718576864014891156L;

    private static final transient Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AJAXServlet.class));

    // Modules
    public static final String MODULE_TASK = "tasks";

    public static final String MODULE_ATTACHMENTS = "attachment";

    public static final String MODULE_CALENDAR = "calendar";

    public static final String MODULE_CONTACT = "contacts";

    public static final String MODULE_UNBOUND = "unbound";

    public static final String MODULE_MAIL = "mail";

    public static final String MODULE_PROJECT = "projects";

    public static final String MODULE_MESSAGING = "messaging";

    public static final String MODULE_INFOSTORE = "infostore";

    public static final String MODULE_SYSTEM = "system";

    // Action Values
    public static final String ACTION_APPEND = "append";

    public static final String ACTION_AUTOSAVE = "autosave";

    public static final String ACTION_NEW = "new";

    public static final String ACTION_EDIT = "edit";

    public static final String ACTION_CONFIG = "config";

    public static final String ACTION_UPLOAD = "upload";

    public static final String ACTION_UPDATE = "update";

    public static final String ACTION_ERROR = "error";

    public static final String ACTION_UPDATES = "updates";

    public static final String ACTION_DELETE = "delete";

    public static final String ACTION_CONFIRM = "confirm";

    public static final String ACTION_LIST = "list";

    public static final String ACTION_VALIDATE = "validate";

    public static final String ACTION_RANGE = "range";

    public static final String ACTION_VIEW = "view";

    public static final String ACTION_SEARCH = "search";

    public static final String ACTION_NEW_APPOINTMENTS = "newappointments";

    public static final String ACTION_SEND = "send";

    public static final String ACTION_GET = "get";

    public static final String ACTION_GET_STRUCTURE = "get_structure";

    public static final String ACTION_IMAGE = "image";

    public static final String ACTION_REPLY = "reply";

    public static final String ACTION_REPLYALL = "replyall";

    public static final String ACTION_FORWARD = "forward";

    public static final String ACTION_MATTACH = "attachment";

    public static final String ACTION_ZIP_MATTACH = "zip_attachments";

    public static final String ACTION_ZIP_MESSAGES = "zip_messages";

    public static final String ACTION_MAIL_RECEIPT_ACK = "receipt_ack";

    public static final String ACTION_NEW_MSGS = "newmsgs";

    public static final String ACTION_COUNT = "count";

    public static final String ACTION_ROOT = "root";

    public static final String ACTION_ALL = "all";

    public static final String ACTION_HAS = "has";

    public static final String ACTION_FREEBUSY = "freebusy";

    protected static final String ACTION_GROUPS = "groups";

    public static final String ACTION_VERSIONS = "versions";

    public static final String ACTION_PATH = "path";

    public static final String ACTION_DOCUMENT = "document";

    public static final String ACTION_DETACH = "detach";

    public static final String ACTION_ATTACH = "attach";

    public static final String ACTION_REVERT = "revert";

    public static final String ACTION_COPY = "copy";

    public static final String ACTION_LOCK = "lock";

    public static final String ACTION_UNLOCK = "unlock";

    public static final String ACTION_SAVE_AS = "saveAs";

    public static final String ACTION_LOGIN = "login";

    public static final String ACTION_OAUTH = "oauth";

    public static final String ACTION_STORE = "store";

    public static final String ACTION_LOGOUT = "logout";

    public static final String ACTION_REDIRECT = "redirect";

    public static final String ACTION_REDEEM = "redeem";

    public static final String ACTION_AUTOLOGIN = "autologin";

    public static final String ACTION_SAVE_VERSIT = "saveVersit";

    public static final String ACTION_CLEAR = "clear";

    public static final String ACTION_KEEPALIVE = "keepalive";

    public static final String ACTION_RESOLVE_UID = "resolveuid";

    public static final String ACTION_IMPORT = "import";

    public static final String ACTION_REFRESH = "refresh";

    public static final String ACTION_REFRESH_SECRET = "refreshSecret";

    public static final String ACTION_TERMSEARCH = "advancedSearch";

    public static final String ACTION_GETCHANGEEXCEPTIONS = "getChangeExceptions";

    /**
     * The parameter 'from' specifies index of starting entry in list of objects dependent on given order criteria and folder id
     */
    public static final String PARAMETER_FROM = "from";

    /**
     * The parameter 'to' specifies the index of excluding ending entry in list of objects dependent on given order criteria and folder id
     */
    public static final String PARAMETER_TO = "to";

    public static final String PARAMETER_START = "start";

    public static final String PARAMETER_END = "end";

    /**
     * The parameter 'id' indicates the id of a certain objects from which certain information must be returned to client
     */
    public static final String PARAMETER_ID = "id";

    public static final String PARAMETER_ATTACHEDID = "attached";

    /**
     * The parameter 'session' represents the id of current active user session
     */
    public static final String PARAMETER_SESSION = "session";

    public static final String PARAMETER_DATA = ResponseFields.DATA;

    /**
     * The parameter 'folder' indicates the current active folder of user
     */
    public static final String PARAMETER_FOLDERID = "folder";

    public static final String PARAMETER_INFOLDER = "folder";

    public static final String PARAMETER_MODULE = "module";

    public static final String PARAMETER_MAIL = "mail";

    /**
     * The parameter 'sort' specifies the field which is used as order source and can be compared to SQL'S 'Order By' statement
     */
    public static final String PARAMETER_SORT = "sort";

    /**
     * The parameter 'dir' specifies the order direction: ASC (ascending) vs. DESC (descending)
     */
    public static final String PARAMETER_ORDER = "order";

    public static final String PARAMETER_RECURRENCE_MASTER = "recurrence_master";

    public static final String LEFT_HAND_LIMIT = "left_hand_limit";

    public static final String RIGHT_HAND_LIMIT = "right_hand_limit";

    public static final String PARAMETER_HARDDELETE = "harddelete";

    public static final String PARAMETER_ACTION = "action";

    /**
     * The parameter 'columns' delivers a comma-sparated list of numbers which encode the fields of a certain object (Mail, Task,
     * Appointment, etc.) that should be transfered to client
     */
    public static final String PARAMETER_COLUMNS = "columns";

    public static final String PARAMETER_SEARCHPATTERN = "pattern";

    public static final String PARAMETER_TIMESTAMP = "timestamp";

    public static final String PARAMETER_TIMEZONE = "timezone";

    public static final String PARAMETER_VERSION = "version";

    public static final String UPLOAD_FORMFIELD_MAIL = "json_0";

    public static final String PARAMETER_IGNORE = "ignore";

    public static final String PARAMETER_ALL = "all";

    public static final String PARAMETER_ATTACHMENT = "attachment";

    public static final String PARAMETER_JSON = "json";

    public static final String PARAMETER_FILE = "file";

    public static final String PARAMETER_CONTENT_TYPE = "content_type";

    public static final String PARAMETER_LIMIT = "limit";

    public static final String PARAMETER_TYPE = "type";

    public static final String PARAMETER_USER = "user";

    public static final String PARAMETER_USER_ID = "user_id";

    public static final String PARAMETER_TEMPLATE = "template";

    public static final String PARAMETER_UID = "uid";

    public static final String PARAMETER_SHOW_PRIVATE_APPOINTMENTS = "showPrivate";

    public static final String PARAMETER_USERNAME = "name";

    public static final String PARAMETER_PASSWORD= "password";

    public static final String PARAMETER_FILTER = "filter";

    public static final String PARAMETER_COLLATION = "collation";

    /**
     * The content type if the response body contains javascript data. Set it with
     * <code>resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT)</code> .
     */
    public static final String CONTENTTYPE_JAVASCRIPT = "text/javascript; charset=UTF-8";

    /**
     * The content type if the reponse body contains the html page include the response for uploads.
     */
    public static final String CONTENTTYPE_HTML = "text/html; charset=UTF-8";

    private static final String STR_EMPTY = "";

    private static final String STR_ERROR = "error";

    private static final String STR_ERROR_PARAMS = "error_params";

	// JavaScript for substituteJS()
	public static final String JS_FRAGMENT = "<!DOCTYPE HTML PUBLIC "
			+ "\"-//W3C//DTD HTML 4.01//EN\" "
			+ "\"http://www.w3.org/TR/html4/strict.dtd\"><html><head>"
			+ "<META http-equiv=\"Content-Type\" "
			+ "content=\"text/html; charset=UTF-8\">"
			+ "<script type=\"text/javascript\">"
			+ "(parent.callback_**action** || window.opener && "
			+ "window.opener.callback_**action**)(**json**)"
			+ "</script></head></html>";

    public static final String SAVE_AS_TYPE = "application/octet-stream";

    protected static final String _doGet = "doGet";

    protected static final String _doPut = "doPut";

    /**
     * Error message if writing the response fails.
     */
    protected static final String RESPONSE_ERROR = "Error while writing response object.";

    /**
     * Initializes a new {@link AJAXServlet}.
     */
    protected AJAXServlet() {
        super();
    }

    /**
     * Gets the locale for given server session
     * 
     * @param session The server session
     * @return The locale
     */
    protected static Locale localeFrom(final ServerSession session) {
        if (null == session) {
            return Locale.US;
        }
        return session.getUser().getLocale();
    }

    /**
     * Gets the locale for given session
     * 
     * @param session The session
     * @return The locale
     */
    protected static Locale localeFrom(final Session session) {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return UserStorage.getStorageUser(session.getUserId(), session.getContextId()).getLocale();
    }

    private static final AtomicLong REQUEST_NUMBER = new AtomicLong(0L);
    private static final String PROP_REQUEST_NUMBER = "com.openexchange.ajax.requestNumber";

    /**
     * The service method of HttpServlet is extended to catch bad exceptions and keep the AJP socket alive. Otherwise Apache thinks in a
     * balancer environment this AJP container is temporarily dead and redirects requests to other AJP containers. This will kill the users
     * session.
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        incrementRequests();
        final Props props = LogProperties.getLogProperties();
        props.put(PROP_REQUEST_NUMBER, ForceLog.valueOf(Long.toString(REQUEST_NUMBER.incrementAndGet())));
        try {
            // create a new HttpSession if missing
            req.getSession(true);

            /*
             * Set 200 OK status code and JSON content by default
             */
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            super.service(new CountingHttpServletRequest(req), resp);
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            final ServletException se = new ServletException(e.getMessage());
            se.initCause(e);
            throw se;
        } finally {
            decrementRequests();
            props.remove(PROP_REQUEST_NUMBER);
        }
    }

    /**
     * Increments the number of requests to path <code>&quot;ajax*&quot;</code> at the very beginning of
     * {@link #service(HttpServletRequest, HttpServletResponse) service} method
     */
    protected void incrementRequests() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.AJAX);
    }

    /**
     * Decrements the number of requests to path <code>&quot;ajax*&quot;</code> at the very end of
     * {@link #service(HttpServletRequest, HttpServletResponse) service} method
     */
    protected void decrementRequests() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.AJAX);
    }

    public static boolean containsParameter(final HttpServletRequest req, final String name) {
        return (req.getParameter(name) != null);
    }

    private static int getMaxBodySize() {
        try {
            return ServerConfig.getInt(ServerConfig.Property.MAX_BODY_SIZE);
        } catch (final OXException e) {
            return Integer.parseInt(ServerConfig.Property.MAX_BODY_SIZE.getDefaultValue());
        }
    }

    private static final boolean BYTE_BASED_READING = true;

    /**
     * 2K buffer
     */
    private static final int BUF_SIZE = 0x800;

    /**
     * Initialize with 8K
     */
    private static final int SB_SIZE = 0x2000;

    /**
     * Returns the complete body as a string. Be careful when getting big request bodies.
     *
     * @param req The HTTP servlet request to read from
     * @return A string with the complete body.
     * @throws IOException If an error occurs while reading the body or body size exceeded configured max. size (see "MAX_BODY_SIZE" property)
     */
    public static String getBody(final HttpServletRequest req) throws IOException {
        return BYTE_BASED_READING ? byteBasedBodyReading(req) : decoderBasedBodyReading(req);
    }

    private static String decoderBasedBodyReading(final HttpServletRequest req) throws IOException {
        String charEnc = req.getCharacterEncoding();
        if (charEnc == null) {
            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        final Reader reader = new InputStreamReader(req.getInputStream(), charEnc);
        try {
            final int buflen = BUF_SIZE;
            final char[] cbuf = new char[buflen];
            final StringBuilder builder = new StringBuilder(SB_SIZE);
            final int maxBodySize = getMaxBodySize();
            if (maxBodySize > 0) {
                int count = 0;
                for (int read = reader.read(cbuf, 0, buflen); read > 0; read = reader.read(cbuf, 0, buflen)) {
                    count += read;
                    if (count > maxBodySize) {
                        throw new IOException("Max. body size (" + UploadUtility.getSize(maxBodySize, 2, false, true) + ") exceeded.");
                    }
                    builder.append(cbuf, 0, read);
                }
            } else {
                for (int read = reader.read(cbuf, 0, buflen); read > 0; read = reader.read(cbuf, 0, buflen)) {
                    builder.append(cbuf, 0, read);
                }
            }
            return builder.toString();
        } catch (final UnsupportedEncodingException e) {
            /*
             * Should never occur
             */
            LOG.error("Unsupported encoding in request", e);
            return STR_EMPTY;
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    private static String byteBasedBodyReading(final HttpServletRequest req) throws IOException {
        final ServletInputStream inputStream = req.getInputStream();
        try {
            final int buflen = BUF_SIZE;
            final byte[] buf = new byte[buflen];
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(SB_SIZE);
            final int maxBodySize = getMaxBodySize();
            if (maxBodySize > 0) {
                int count = 0;
                for (int read = inputStream.read(buf, 0, buflen); read > 0; read = inputStream.read(buf, 0, buflen)) {
                    count += read;
                    if (count > maxBodySize) {
                        throw new IOException("Max. body size (" + UploadUtility.getSize(maxBodySize, 2, false, true) + ") exceeded.");
                    }
                    baos.write(buf, 0, read);
                }
            } else {
                for (int read = inputStream.read(buf, 0, buflen); read > 0; read = inputStream.read(buf, 0, buflen)) {
                    baos.write(buf, 0, read);
                }
            }
            try {
                String charEnc = req.getCharacterEncoding();
                if (charEnc == null) {
                    charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
                }
                return new String(baos.toByteArray(), Charsets.forName(charEnc));
            } catch (final UnsupportedCharsetException e) {
                LOG.error("Unsupported encoding in request", e);
                return new String(baos.toByteArray(), Charsets.ISO_8859_1);
            }
        } finally {
            try {
                inputStream.close();
            } catch (final IOException e) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns the URI part after path to the servlet.
     *
     * @param req the request that url should be parsed
     * @return the URI part after the path to your servlet.
     */
    public static String getServletSpecificURI(final HttpServletRequest req) {
        String uri;
        try {
            String characterEncoding = req.getCharacterEncoding();
            if (null == characterEncoding) {
                characterEncoding = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
                if (null == characterEncoding) {
                    characterEncoding = "ISO-8859-1";
                }
            }
            uri = URLDecoder.decode(req.getRequestURI(), characterEncoding);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding", e);
            uri = req.getRequestURI();
        }
        final String path = new StringBuilder(req.getContextPath()).append(req.getServletPath()).toString();
        final int pos = uri.indexOf(path);
        if (pos >= 0) {
            uri = uri.substring(pos + path.length());
        }
        return uri;
    }

    private static final URLCodec URL_CODEC = new URLCodec(CharEncoding.ISO_8859_1);
    
    /**
     * BitSet of www-form-url safe characters.
     */
    protected static final BitSet WWW_FORM_URL;

    /**
     * BitSet of www-form-url safe characters including safe characters for an anchor.
     */
    protected static final BitSet WWW_FORM_URL_ANCHOR;

    // Static initializer for www_form_url
    static {
        {
            final BitSet bitSet = new BitSet(256);
            // alpha characters
            for (int i = 'a'; i <= 'z'; i++) {
                bitSet.set(i);
            }
            for (int i = 'A'; i <= 'Z'; i++) {
                bitSet.set(i);
            }
            // numeric characters
            for (int i = '0'; i <= '9'; i++) {
                bitSet.set(i);
            }
            // special chars
            bitSet.set('-');
            bitSet.set('_');
            bitSet.set('.');
            bitSet.set('*');
            // blank to be replaced with +
            bitSet.set(' ');
            WWW_FORM_URL = bitSet;
        }
        {
            final BitSet bitSet = new BitSet(256);
            // alpha characters
            for (int i = 'a'; i <= 'z'; i++) {
                bitSet.set(i);
            }
            for (int i = 'A'; i <= 'Z'; i++) {
                bitSet.set(i);
            }
            // numeric characters
            for (int i = '0'; i <= '9'; i++) {
                bitSet.set(i);
            }
            // special chars
            bitSet.set('-');
            bitSet.set('_');
            bitSet.set('.');
            bitSet.set('*');
            // blank to be replaced with +
            bitSet.set(' ');
            // Anchor characters
            bitSet.set('/');
            bitSet.set('#');
            bitSet.set('%');
            bitSet.set('?');
            bitSet.set('&');
            WWW_FORM_URL_ANCHOR = bitSet;
        }
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String encodeUrl(final String s) {
        return encodeUrl(s, false);
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     */
    public static String encodeUrl(final String s, final boolean forAnchor) {
        return encodeUrl(s, forAnchor, false);
    }

    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n|\r|(?:%0[aA])?%0[dD]|%0[aA]");
    private static final Pattern PATTERN_DSLASH = Pattern.compile("(?:/|%2[fF]){2}");

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     */
    public static String encodeUrl(final String s, final boolean forAnchor, final boolean forLocation) {
        if (isEmpty(s)) {
            return s;
        }
        try {
            final String ascii;
            if (!forAnchor) {
                ascii = Charsets.toAsciiString(URLCodec.encodeUrl(WWW_FORM_URL, s.getBytes(Charsets.ISO_8859_1)));
            } else {
                // Prepare for being used as anchor/link
                ascii = Charsets.toAsciiString(URLCodec.encodeUrl(WWW_FORM_URL_ANCHOR, s.getBytes(Charsets.ISO_8859_1)));
            }
            // Strip possible "\r?\n" and/or "%0A?%0D"
            String retval = PATTERN_CRLF.matcher(ascii).replaceAll("");
            // Check for a relative URI
            if (forLocation) {
                try {
                    final java.net.URI uri = new java.net.URI(retval);
                    if (uri.isAbsolute() || null != uri.getScheme() || null != uri.getHost()) {
                        throw new IllegalArgumentException("Illegal Location value: " + s);
                    }
                } catch (final URISyntaxException e) {
                    throw new IllegalArgumentException("Illegal Location value: " + s, e);
                }
            }
            // Replace double slashes with single one
            {
                Matcher matcher = PATTERN_DSLASH.matcher(retval);
                while (matcher.find()) {
                    retval = matcher.replaceAll("/");
                    matcher = PATTERN_DSLASH.matcher(retval);
                }
            }
            return retval;
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final RuntimeException e) {
            LOG.error("A runtime error occurred.", e);
            return s;
        }
    }

    /**
<<<<<<< HEAD
=======
     * Sanitizes specified parameter value.
     */
    public static String sanitizeParam(final String s) {
        if (isEmpty(s)) {
            return s;
        }
        try {
            // Strip possible "\r?\n" and/or "%0A?%0D"
            return PATTERN_CRLF.matcher(s).replaceAll("");
        } catch (final RuntimeException e) {
            LOG.error("A runtime error occurred.", e);
            return s;
        }
    }

    /**
>>>>>>> fb731f9... Special check for "location" parameter
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String decodeUrl(final String s, final String charset) {
        try {
            return isEmpty(s) ? s : (isEmpty(charset) ? URL_CODEC.decode(s) : URL_CODEC.decode(s, charset));
        } catch (final DecoderException e) {
            return s;
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * Gets the action parameter ({@link #PARAMETER_ACTION}) from specified servlet request.
     *
     * @param req The servlet request
     * @return The action parameter's value
     * @throws OXException If action parameter is missing in specified servlet request
     */
    protected static String getAction(final HttpServletRequest req) throws OXException {
        final String action = req.getParameter(PARAMETER_ACTION);
        if (action == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( PARAMETER_ACTION);
        }
        return action;
    }

    /**
     * This method sends the given error message as a java script error object to the client.
     *
     * @param resp This response will be used to send the java script error object.
     * @param errorMessage The error message to send to the client.
     * @throws IOException if writing to the response fails.
     * @throws ServletException if the creation of the java script error object fails.
     * @deprecated use {@link Response}.
     */
    @Deprecated
    protected static void sendErrorAsJS(final HttpServletResponse resp, final String errorMessage) throws IOException, ServletException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            final PrintWriter w = resp.getWriter();
            final JSONWriter jw = new JSONWriter(w);
            jw.object();
            jw.key(STR_ERROR);
            jw.value(errorMessage);
            jw.endObject();
            w.flush();
        } catch (final JSONException e1) {
            final ServletException se = new ServletException(e1.getMessage(), e1);
            se.initCause(e1);
            throw se;
        }
    }

    protected static void sendError(final HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * @deprecated
     */
    @Deprecated
    protected static void sendErrorAsJSHTML(final HttpServletResponse res, final String error, final String action) throws IOException {
        res.setContentType("text/html");
        PrintWriter w = null;
        try {
            w = res.getWriter();
            final JSONObject obj = new JSONObject();
            obj.put(STR_ERROR, error);
            obj.put(STR_ERROR_PARAMS, Collections.emptyList());
			w.write(substituteJS(obj.toString(), action));
        } catch (final JSONException e) {
            LOG.error(e);
        } finally {
            close(w);
        }
    }

    protected void unknownColumn(final HttpServletResponse res, final String parameter, final String columnId, final boolean html, final String action) throws IOException, ServletException {
        final String msg = "Unknown column in " + parameter + " :" + columnId;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    protected void invalidParameter(final HttpServletResponse res, final String parameter, final String value, final boolean html, final String action) throws IOException, ServletException {
        final String msg = "Invalid Parameter " + parameter + " :" + value;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    protected void missingParameter(final String parameter, final HttpServletResponse res, final boolean html, final String action) throws IOException, ServletException {
        final String msg = "Missing Parameter: " + parameter;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    protected void unknownAction(final String method, final String action, final HttpServletResponse res, final boolean html) throws IOException, ServletException {
        final String msg = "The action " + action + " isn't even specified yet. At least not for the method: " + method;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

	public static String substituteJS(final String json, final String action) {
		return JS_FRAGMENT.replace("**json**", json.replaceAll(Pattern.quote("</") , "<\\/")).replace("**action**",
				action);
	}

    /* --------------------- STUFF FOR UPLOAD --------------------- */

    /**
     * Processes specified request's upload provided that request is of content type <code>multipart/*</code>.
     *
     * @param req The request whose upload shall be processed
     * @return The processed instance of {@link UploadEvent}
     * @throws OXException Id processing the upload fails
     */
    @Override
    public UploadEvent processUpload(final HttpServletRequest req) throws OXException {
        return processUploadStatic(req);
    }

    private static volatile ServletFileUpload servletFileUpload;

    /**
     * Gets the {@link ServletFileUpload} instance.
     *
     * @return The {@link ServletFileUpload} instance
     */
    private static ServletFileUpload getFileUploadBase() {
        ServletFileUpload tmp = servletFileUpload;
        if (null == tmp) {
            synchronized (AJAXServlet.class) {
                tmp = servletFileUpload;
                if (null == tmp) {
                    tmp = servletFileUpload = newFileUploadBase();
                }
            }
        }
        return tmp;
    }

    /**
     * 1MB threshold.
     */
    private static final int SIZE_THRESHOLD = 1048576;

    /**
     * The file cleaning tracker.
     */
    private static volatile DeleteOnExitFileCleaningTracker tracker;
    
    /**
     * Exits the file cleaning tracker.
     */
    public static void exitTracker() {
        AJAXServlet.servletFileUpload = null;
        final DeleteOnExitFileCleaningTracker tracker = AJAXServlet.tracker;
        if (null != tracker) {
            tracker.deleteAllTracked();
            AJAXServlet.tracker = null;
        }
    }

    /**
     * Creates a new {@code ServletFileUpload} instance.
     *
     * @return A new {@code ServletFileUpload} instance
     */
    private static ServletFileUpload newFileUploadBase() {
        /*
         * Create the upload event
         */
        final DiskFileItemFactory factory = new DiskFileItemFactory();
        /*
         * Set factory constraints; threshold for single files
         */
        factory.setSizeThreshold(SIZE_THRESHOLD);
        factory.setRepository(new File(ServerConfig.getProperty(Property.UploadDirectory)));
        final DeleteOnExitFileCleaningTracker tracker = new DeleteOnExitFileCleaningTracker(false);
        factory.setFileCleaningTracker(tracker);
        AJAXServlet.tracker = tracker;
        /*
         * Create a new file upload handler
         */
        final ServletFileUpload sfu = new ServletFileUpload(factory);
        /*
         * Set overall request size constraint
         */
        sfu.setSizeMax(-1);
        return sfu;
    }

    private static final Set<String> UPLOAD_ACTIONS =
        new HashSet<String>(Arrays.asList(ACTION_NEW, ACTION_UPLOAD, ACTION_APPEND, ACTION_UPDATE, ACTION_ATTACH, ACTION_COPY, "import"));



    /**
     * (Statically) Processes specified request's upload provided that request is of content type <code>multipart/*</code>.
     *
     * @param req The request whose upload shall be processed
     * @return The processed instance of {@link UploadEvent}
     * @throws OXException Id processing the upload fails
     */
    public static final UploadEvent processUploadStatic(final HttpServletRequest req) throws OXException {
        if (!Tools.isMultipartContent(req)) {
            // No multipart content
            throw UploadException.UploadCode.NO_MULTIPART_CONTENT.create();
        }
        /*
         * Check action parameter existence
         */
        final String action;
        try {
            action = getAction(req);
        } catch (final OXException e) {
            throw UploadException.UploadCode.UPLOAD_FAILED.create(e);
        }
        /*-
         * Check proper action value
         *
         * ###########################################################################################################
         * ######################### ENSURE YOUR ACTION IS CONTAINED IN UPLOAD_ACTIONS ! ! ! #########################
         * ###########################################################################################################
         */
        if (!mayUpload(action)) {
            throw UploadException.UploadCode.UNKNOWN_ACTION_VALUE.create(action);
        }
        /*
         * Create file upload base
         */
        final ServletFileUpload upload = getFileUploadBase();
        final List<FileItem> items;
        try {
            @SuppressWarnings("unchecked") final List<FileItem> tmp = upload.parseRequest(req);
            items = tmp;
        } catch (final FileUploadException e) {
            throw UploadException.UploadCode.UPLOAD_FAILED.create(e, action);
        }
        /*
         * Create the upload event
         */
        final UploadEvent uploadEvent = new UploadEvent();
        uploadEvent.setAction(action);
        /*
         * Set affiliation to mail upload
         */
        uploadEvent.setAffiliationId(UploadEvent.MAIL_UPLOAD);
        /*
         * Fill upload event instance
         */
        final String charEnc;
        {
            final String rce = req.getCharacterEncoding();
            charEnc = null == rce ? ServerConfig.getProperty(Property.DefaultEncoding) : rce;
        }
        for (final FileItem fileItem : items) {
            if (fileItem.isFormField()) {
                try {
                    uploadEvent.addFormField(fileItem.getFieldName(), fileItem.getString(charEnc));
                } catch (final UnsupportedEncodingException e) {
                    throw UploadException.UploadCode.UPLOAD_FAILED.create(e, action);
                }
            } else {
                if (fileItem.getSize() > 0 || !isEmpty(fileItem.getName())) {
                    try {
                        uploadEvent.addUploadFile(processUploadedFile(fileItem, ServerConfig.getProperty(Property.UploadDirectory)));
                    } catch (final Exception e) {
                        throw UploadException.UploadCode.UPLOAD_FAILED.create(e, action);
                    }
                }
            }
        }
        if (uploadEvent.getAffiliationId() < 0) {
            throw UploadException.UploadCode.MISSING_AFFILIATION_ID.create(action);
        }
        return uploadEvent;
    }

    protected static boolean mayUpload(final String action) {
        return UPLOAD_ACTIONS.contains(action) || Arrays.asList("CSV", "VCARD","ICAL", "OUTLOOK_CSV").contains(action); //Boo! Bad hack to get importer/export bundle working
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

 	private static final UploadFile processUploadedFile(final FileItem item, final String uploadDir) throws Exception {
        try {
            final UploadFile retval = new UploadFileImpl();
            retval.setFieldName(item.getFieldName());
            retval.setFileName(item.getName());
            retval.setContentType(item.getContentType());
            retval.setSize(item.getSize());
            final File tmpFile = File.createTempFile("openexchange", null, new File(uploadDir));
            tmpFile.deleteOnExit();
            item.write(tmpFile);
            retval.setTmpFile(tmpFile);
            return retval;
        } finally {
            item.delete();
        }
    }

    @Override
    public void fireUploadEvent(final UploadEvent uploadEvent, final Collection<UploadListener> uploadListeners) throws OXException {
        try {
            for (final UploadListener uploadListener : uploadListeners) {
                try {
                    uploadListener.action(uploadEvent);
                } catch (final OXException e) {
                    LOG.error(new StringBuilder(64).append("Failed upload listener: ").append(uploadListener.getClass()), e);
                }
            }
        } finally {
            uploadEvent.cleanUp();
        }
    }

    public static void startResponse(final JSONWriter jsonwriter) throws JSONException {
        jsonwriter.object();
        jsonwriter.key("data");
    }

    public static void endResponse(final JSONWriter jsonwriter, final Date timestamp, final String error) throws JSONException {
        if (timestamp != null) {
            jsonwriter.key("timestamp");
            jsonwriter.value(timestamp.getTime());
        }

        if (error != null) {
            jsonwriter.key(STR_ERROR);
            jsonwriter.value(error);
            jsonwriter.key(STR_ERROR_PARAMS);
            jsonwriter.value(new JSONArray());
        }

        jsonwriter.endObject();
    }

    protected boolean checkRequired(final HttpServletRequest req, final HttpServletResponse res, final boolean html, final String action, final String... parameters) throws IOException, ServletException {
        if (html) {
            res.setContentType("text/html; charset=UTF-8");
        }
        for (final String param : parameters) {
            if (req.getParameter(param) == null) {
                missingParameter(param, res, html, action);
                return false;
            }
        }
        return true;
    }

    protected static void close(final Writer w) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(new StringBuilder("Called close() with writer").append(w.toString()));
        }
        // return;
        /*
         * if (w == null) { return; } try { w.flush(); // System.out.println("INFOSTORE: Flushed!"); } catch (IOException e) { LOG.error(e);
         * } try { w.close(); // System.out.println("INFOSTORE: Closed!"); } catch (IOException e) { LOG.error(e); }
         */
    }

    protected void writeResponse(final Response response, final HttpServletResponse servletResponse, Session session) throws IOException {
        servletResponse.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, servletResponse.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(servletResponse);
        }
    }

    protected final boolean isIE(final HttpServletRequest req) {
        return req.getHeader("User-Agent").contains("MSIE");
    }

    protected final boolean isIE7(final HttpServletRequest req) {
        return req.getHeader("User-Agent").contains("MSIE 7");
    }

    public static final String getModuleString(final int module, final int objectId) {
        String moduleStr = null;
        switch (module) {
        case FolderObject.TASK:
            moduleStr = MODULE_TASK;
            break;
        case FolderObject.CONTACT:
            moduleStr = MODULE_CONTACT;
            break;
        case FolderObject.CALENDAR:
            moduleStr = MODULE_CALENDAR;
            break;
        case FolderObject.UNBOUND:
            moduleStr = MODULE_UNBOUND;
            break;
        case FolderObject.MAIL:
            moduleStr = MODULE_MAIL;
            break;
        case FolderObject.PROJECT:
            moduleStr = MODULE_PROJECT;
            break;
        case FolderObject.INFOSTORE:
            moduleStr = MODULE_INFOSTORE;
            break;
        case FolderObject.SYSTEM_MODULE:
            if (objectId == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
                moduleStr = MODULE_PROJECT;
            } else if (objectId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                moduleStr = MODULE_INFOSTORE;
            } else {
                moduleStr = MODULE_SYSTEM;
            }
            break;
        case FolderObject.MESSAGING:
            moduleStr = MODULE_MESSAGING;
            break;
        default:
            moduleStr = "";
            break;
        }
        return moduleStr;
    }

    public static final int getModuleInteger(final String moduleStr) {
        final int module;
        if (MODULE_TASK.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.TASK;
        } else if (MODULE_CONTACT.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.CONTACT;
        } else if (MODULE_CALENDAR.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.CALENDAR;
        } else if (MODULE_UNBOUND.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.UNBOUND;
        } else if (MODULE_MAIL.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.MAIL;
        } else if (MODULE_PROJECT.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.PROJECT;
        } else if (MODULE_INFOSTORE.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.INFOSTORE;
        } else {
            module = -1;
        }
        return module;
    }

}

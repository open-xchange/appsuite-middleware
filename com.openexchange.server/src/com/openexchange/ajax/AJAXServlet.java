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

package com.openexchange.ajax;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadListener;
import com.openexchange.groupware.upload.impl.UploadRegistry;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;

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

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AJAXServlet.class);

    // Modules
    public static final String MODULE_TASK = "tasks";

    public static final String MODULE_ATTACHMENTS = "attachment";

    public static final String MODULE_CALENDAR = "calendar";

    public static final String MODULE_CONTACT = "contacts";

    public static final String MODULE_UNBOUND = "unbound";

    public static final String MODULE_MAIL = "mail";

    public static final String MODULE_MESSAGING = "messaging";

    public static final String MODULE_INFOSTORE = "infostore";

    public static final String MODULE_SYSTEM = "system";

    // Action Values
    public static final String ACTION_APPEND = "append";

    public static final String ACTION_AUTOSAVE = "autosave";

    public static final String ACTION_NEW = "new";

    public static final String ACTION_ADDFILE = "addfile";

    public static final String ACTION_EDIT = "edit";

    public static final String ACTION_CONFIG = "config";

    public static final String ACTION_UPLOAD = "upload";

    public static final String ACTION_UPDATE = "update";

    public static final String ACTION_ERROR = "error";

    public static final String ACTION_UPDATES = "updates";

    public static final String ACTION_DELETE = "delete";
    
    public static final String ACTION_EXAMINE = "examine";

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

    public static final String ACTION_STORE = "store";

    public static final String ACTION_RAMPUP = "rampup";

    public static final String ACTION_GUEST = "guest";

    public static final String ACTION_ANONYMOUS = "anonymous";

    public static final String ACTION_REDEEM_RESERVATION = "redeemReservation";

    public static final String ACTION_LOGOUT = "logout";

    public static final String ACTION_REDIRECT = "redirect";

    public static final String ACTION_REDEEM = "redeem";

    public static final String ACTION_AUTOLOGIN = "autologin";

    public static final String ACTION_SSO_LOGOUT = "ssoLogout";

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

    /**
     * The parameter 'public_session' represents the public id of current active user session
     */
    public static final String PARAMETER_PUBLIC_SESSION = "public_session";

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

    /**
     * The "action" parameter.
     */
    public static final String PARAMETER_ACTION = "action";

    /**
     * The "csid" parameter providing the composition space identifier
     */
    public static final String PARAMETER_CSID = "csid";

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

    public static final String PARAMETER_OCCURRENCE = "occurrence";

    public static final String PARAMETER_USERNAME = "name";

    public static final String PARAMETER_PASSWORD= "password";

    public static final String PARAMETER_FILTER = "filter";

    public static final String PARAMETER_COLLATION = "collation";

    public static final String PARAMETER_INCLUDE_STACK_TRACE_ON_ERROR = "includeStackTraceOnError";

    /**
     * The <code><b>&quot;delivery&quot;</b></code> parameter specifies how to deliver binary data; e.g. <code>&quot;view&quot;</code> for
     * inlined display or <code>&quot;download&quot;</code>.
     */
    public static final String PARAMETER_DELIVERY = "delivery".intern();

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

    /**
     * JavaScript for <code>substituteJS()</code>.
     * <pre>
     *      &lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"&gt;
     *      &lt;html&gt;
     *       &lt;head&gt;
     *        &lt;META http-equiv="Content-Type" content="text/html; charset=UTF-8"&gt;
     *        &lt;script type="text/javascript"&gt;
     *          (parent.callback_**action** || window.opener && window.opener.callback_**action**)(**json**)
     *        &lt;/script&gt;
     *       &lt;/head&gt;
     *      &lt;/html&gt;
     * </pre>
     */
    public static final String JS_FRAGMENT = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head>"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
        + "<script type=\"text/javascript\">"
        + "(parent[\"callback_**action**\"] || window.opener && "
        + "window.opener[\"callback_**action**\"])(**json**)"
        + "</script></head></html>";

    public static final String SAVE_AS_TYPE = "application/octet-stream";

    protected static final String _doGet = "doGet";

    protected static final String _doPut = "doPut";

    /**
     * Error message if writing the response fails.
     */
    public static final String RESPONSE_ERROR = "Error while writing response object.";

    /**
     * Initializes a new {@link AJAXServlet}.
     */
    protected AJAXServlet() {
        super();
    }

    // private static final AtomicLong REQUEST_NUMBER = new AtomicLong(0L);

    /**
     * Gets the locale for given server session
     *
     * @param session The server session
     * @return The locale
     */
    protected static Locale localeFrom(ServerSession session) {
        if (null == session) {
            return Locale.US;
        }
        User user = session.getUser();
        if (null != user) {
            return user.getLocale();
        }

        try {
            return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
        } catch (OXException e) {
            LOG.warn("Could not load user to get his locale.", e);
            return Locale.US;
        }
    }

    /**
     * Gets the locale for given session
     *
     * @param session The session
     * @return The locale
     */
    public static Locale localeFrom(Session session) {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }

        try {
            return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
        } catch (OXException e) {
            LOG.warn("Could not load user to get his locale.", e);
            return Locale.US;
        }
    }

    private static final String CONTENTTYPE_UPLOAD = "multipart/form-data";

    /**
     * The service method of HttpServlet is extended to catch bad exceptions and keep the socket alive. Otherwise Apache thinks in a
     * balancer environment this container is temporarily dead and redirects requests to other containers. This will kill the users session.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp, true);
    }

    /**
     * Receives standard HTTP requests from the public <code>service</code> method and dispatches them to the <code>do</code><i>XXX</i>
     * methods defined in this class.
     * <p>
     * This method is an HTTP-specific version of the {@link javax.servlet.Servlet#service} method. There's no need to override this method.
     *
     * @param req The {@link HttpServletRequest} object that contains the request the client made of the servlet
     * @param resp the {@link HttpServletResponse} object that contains the response the servlet returns to the client
     * @param checkRateLimit Whether rate limit check is supposed to be performed
     * @throws IOException If an input or output error occurs while the servlet is handling the HTTP request
     * @throws ServletException If the HTTP request cannot be handled
     * @see javax.servlet.Servlet#service
     */
    protected void doService(HttpServletRequest req, HttpServletResponse resp, boolean checkRateLimit) throws ServletException, IOException {
        incrementRequests();
        // We already have a tracking id...
        // LogProperties.putProperty(LogProperties.Name.AJAX_REQUEST_NUMBER, Long.toString(REQUEST_NUMBER.incrementAndGet()));
        try {
            // create a new HttpSession if missing
            req.getSession(true);

            // Set 200 OK status code and JSON content by default
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);

            if (checkRateLimit) {
                // Enable rate limit on request instance
                super.service(enableRateLimitCheckFor(req), resp);
            } else {
                // No rate limit check
                super.service(req, resp);
            }
        } catch (RateLimitedException e) {
            e.send(resp);
        } catch (RuntimeException e) {
            OXException oxe = new OXException(e);
            LOG.error("", oxe);

            ServletException se = new ServletException(e.getMessage());
            se.initCause(oxe);
            throw se;
        } finally {
            decrementRequests();
            LogProperties.removeProperty(LogProperties.Name.AJAX_REQUEST_NUMBER);
        }
    }

    /**
     * Enables the rate limit check for specified request instance
     *
     * @param req The request instance
     * @return The request instance with rate limit check enabled
     */
    protected HttpServletRequest enableRateLimitCheckFor(HttpServletRequest req) {
        // Check for possible upload
        String contentType = Strings.asciiLowerCase(req.getContentType());
        if (contentType != null && contentType.startsWith(CONTENTTYPE_UPLOAD, 0)) {
            // An upload request; thus do not return an instance of "CountingHttpServletRequest" since uploads have their own quota semantics.
            com.openexchange.tools.servlet.ratelimit.RateLimiter.checkRequest(req);
            return req;
        }

        // Common request
        return new CountingHttpServletRequest(req);
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

    public static boolean containsParameter(HttpServletRequest req, String name) {
        return (req.getParameter(name) != null);
    }

    @Deprecated
    private static int getMaxBodySize() {
        try {
            return ServerConfig.getInt(ServerConfig.Property.MAX_BODY_SIZE);
        } catch (OXException e) {
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
     * Gets the reader for HTTP Servlet request's input stream.
     *
     * @param req The HTTP Servlet request
     * @return The reader
     * @throws IOException If an I/O error occurs
     */
    public static Reader getReaderFor(HttpServletRequest req) throws IOException {
        String charEnc = req.getCharacterEncoding();
        if (charEnc == null) {
            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        return new BufferedReader(new InputStreamReader(req.getInputStream(), Charsets.forName(charEnc)), BUF_SIZE);
    }

    /**
     * Parses specified HTTP Servlet request's input stream content to a JSON value.
     *
     * @param req The HTTP Servlet request to read from
     * @return The parsed JSON value
     * @throws IOException If an I/O error occurs
     * @throws JSONException If a JSON error occurs
     */
    public static JSONValue getBodyAsJsonValue(HttpServletRequest req) throws IOException, JSONException {
        String charEnc = req.getCharacterEncoding();
        if (charEnc == null) {
            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(req.getInputStream(), Charsets.forName(charEnc)), BUF_SIZE);
            return JSONObject.parse(reader);
        } catch (UnsupportedCharsetException e) {
            /*
             * Should never occur
             */
            LOG.error("Unsupported encoding in request", e);
            return new JSONObject();
        } finally {
            Streams.close(reader);
        }
    }

    /**
     * Returns the complete body as a string. Be careful when getting big request bodies.
     *
     * @param req The HTTP Servlet request to read from
     * @return A string with the complete body.
     * @throws IOException If an error occurs while reading the body or body size exceeded configured max. size (see "MAX_BODY_SIZE" property)
     */
    public static String getBody(HttpServletRequest req) throws IOException {
        return BYTE_BASED_READING ? byteBasedBodyReading(req) : decoderBasedBodyReading(req);
    }

    /**
     * Reads the content from given reader.
     *
     * @param reader The reader
     * @return The reader's content
     * @throws IOException If an I/O error occurs
     */
    public static String readFrom(Reader reader) throws IOException {
        if (null == reader) {
            return null;
        }
        int buflen = BUF_SIZE;
        char[] cbuf = new char[buflen];
        StringBuilder builder = new StringBuilder(SB_SIZE);
        int maxBodySize = getMaxBodySize();
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
        if (0 == builder.length()) {
            return null;
        }
        return builder.toString();
    }

    private static String decoderBasedBodyReading(HttpServletRequest req) throws IOException {
        String charEnc = req.getCharacterEncoding();
        if (charEnc == null) {
            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        Reader reader = new InputStreamReader(req.getInputStream(), Charsets.forName(charEnc));
        try {
            int buflen = BUF_SIZE;
            char[] cbuf = new char[buflen];
            StringBuilder builder = new StringBuilder(SB_SIZE);
            int maxBodySize = getMaxBodySize();
            if (maxBodySize > 0) {
                int count = 0;
                for (int read = reader.read(cbuf, 0, buflen); read > 0; read = reader.read(cbuf, 0, buflen)) {
                    count += read;
                    if (count > maxBodySize) {
                        Streams.close(reader);
                        reader = null;
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
        } catch (UnsupportedCharsetException e) {
            /*
             * Should never occur
             */
            LOG.error("Unsupported encoding in request", e);
            return STR_EMPTY;
        } finally {
            Streams.close(reader);
        }
    }

    private static String byteBasedBodyReading(HttpServletRequest req) throws IOException {
        ServletInputStream inputStream = req.getInputStream();
        try {
            int buflen = BUF_SIZE;
            byte[] buf = new byte[buflen];
            ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(SB_SIZE);
            int maxBodySize = getMaxBodySize();
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
                return baos.toString(charEnc);
            } catch (UnsupportedCharsetException e) {
                LOG.error("Unsupported encoding in request", e);
                return baos.toString("ISO-8859-1");
            }
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Returns the URI part after path to the servlet.
     *
     * @param req the request that url should be parsed
     * @return the URI part after the path to your servlet.
     */
    public static String getServletSpecificURI(HttpServletRequest req) {
        String uri;
        {
            String characterEncoding = req.getCharacterEncoding();
            if (null == characterEncoding) {
                characterEncoding = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
                if (null == characterEncoding) {
                    characterEncoding = "ISO-8859-1";
                }
            }
            uri = AJAXUtility.decodeUrl(req.getRequestURI(), characterEncoding);
        }
        String path = new StringBuilder(req.getContextPath()).append(req.getServletPath()).toString();
        int pos = uri.indexOf(path);
        if (pos >= 0) {
            uri = uri.substring(pos + path.length());
        }
        return uri;
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @deprecated Use {@link AJAXUtility#encodeUrl(String)} instead
     */
    @Deprecated
    public static String encodeUrl(String s) {
        return AJAXUtility.encodeUrl(s);
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     * @deprecated Use {@link AJAXUtility#encodeUrl(String,boolean)} instead
     */
    @Deprecated
    public static String encodeUrl(String s, boolean forAnchor) {
        return AJAXUtility.encodeUrl(s, forAnchor);
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     * @deprecated Use {@link AJAXUtility#encodeUrl(String,boolean,boolean)} instead
     */
    @Deprecated
    public static String encodeUrl(String s, boolean forAnchor, boolean forLocation) {
        return AJAXUtility.encodeUrl(s, forAnchor, forLocation);
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     * @deprecated Use {@link AJAXUtility#encodeUrl(String,boolean,boolean,String)} instead
     */
    @Deprecated
    public static String encodeUrl(String s, boolean forAnchor, boolean forLocation, String charsetName) {
        return AJAXUtility.encodeUrl(s, forAnchor, forLocation, charsetName);
    }

    /**
     * Sanitizes specified String input.
     * <ul>
     * <li>Do URL decoding until fully decoded
     * <li>Drop ASCII control characters
     * <li>Escape using HTML entities
     * <li>Replace double slashes with single one
     * </ul>
     *
     * @param sInput The input to sanitize
     * @return The sanitized input
     * @deprecated Use {@link AJAXUtility#sanitizeParam(String)} instead
     */
    @Deprecated
    public static String sanitizeParam(String sInput) {
        return AJAXUtility.sanitizeParam(sInput);
    }

    /**
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @deprecated Use {@link AJAXUtility#decodeUrl(String,String)} instead
     */
    @Deprecated
    public static String decodeUrl(String s, String charset) {
        return AJAXUtility.decodeUrl(s, charset);
    }

    /**
     * Gets the action parameter ({@link #PARAMETER_ACTION}) from specified servlet request.
     *
     * @param req The servlet request
     * @return The action parameter's value
     * @throws OXException If action parameter is missing in specified servlet request
     */
    public static String getAction(HttpServletRequest req) throws OXException {
        String action = req.getParameter(PARAMETER_ACTION);
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
    protected static void sendErrorAsJS(HttpServletResponse resp, String errorMessage) throws IOException, ServletException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            PrintWriter w = resp.getWriter();
            JSONWriter jw = new JSONWriter(w);
            jw.object();
            jw.key(STR_ERROR);
            jw.value(errorMessage);
            jw.endObject();
            w.flush();
        } catch (JSONException e1) {
            ServletException se = new ServletException(e1.getMessage(), e1);
            se.initCause(e1);
            throw se;
        }
    }

    public static void sendError(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * @deprecated
     */
    @Deprecated
    protected static void sendErrorAsJSHTML(HttpServletResponse res, String error, String action) throws IOException {
        res.setContentType("text/html");
        PrintWriter w = null;
        try {
            w = res.getWriter();
            JSONObject obj = new JSONObject();
            obj.put(STR_ERROR, error);
            obj.put(STR_ERROR_PARAMS, Collections.emptyList());
            w.write(substituteJS(obj.toString(), action));
        } catch (JSONException e) {
            LOG.error("", e);
        } finally {
            close(w);
        }
    }

    protected void unknownColumn(HttpServletResponse res, String parameter, String columnId, boolean html, String action) throws IOException, ServletException {
        String msg = "Unknown column in " + parameter + " :" + columnId;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    protected void invalidParameter(HttpServletResponse res, String parameter, String value, boolean html, String action) throws IOException, ServletException {
        String msg = "Invalid Parameter " + parameter + " :" + value;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    protected void missingParameter(String parameter, HttpServletResponse res, boolean html, String action) throws IOException, ServletException {
        String msg = "Missing Parameter: " + parameter;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    protected void unknownAction(String method, String action, HttpServletResponse res, boolean html) throws IOException, ServletException {
        String msg = "The action " + action + " isn't even specified yet. At least not for the method: " + method;
        if (html) {
            sendErrorAsJSHTML(res, msg, action);
            return;
        }
        sendErrorAsJS(res, msg);
    }

    public static String substituteJS(String json, String action) {
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
    public UploadEvent processUpload(HttpServletRequest req) throws OXException {
        return processUpload(req, -1, -1);
    }

    @Override
    public UploadEvent processUpload(HttpServletRequest req, long maxFileSize, long maxOverallSize) throws OXException {
        return processUploadStatic(req, maxFileSize, maxOverallSize);
    }

    /**
     * Creates a new {@code ServletFileUpload} instance.
     *
     * @return A new {@code ServletFileUpload} instance
     */
    public static ServletFileUpload newFileUploadBase() {
        return newFileUploadBase(-1,  -1);
    }

    /**
     * Creates a new {@code ServletFileUpload} instance.
     *
     * @param maxFileSize The maximum allowed size of a single uploaded file
     * @param maxOverallSize The maximum allowed size of a complete request
     * @return A new {@code ServletFileUpload} instance
     */
    public static ServletFileUpload newFileUploadBase(long maxFileSize, long maxOverallSize) {
        return UploadUtility.newFileUploadBase(maxFileSize, maxOverallSize);
    }

    private static final Set<String> UPLOAD_ACTIONS = new HashSet<String>(Arrays.asList(ACTION_NEW, ACTION_ADDFILE, ACTION_UPLOAD, ACTION_APPEND, ACTION_UPDATE, ACTION_ATTACH, ACTION_COPY, "import", "CSV", "VCARD","ICAL", "OUTLOOK_CSV"));

    /**
     * Checks if given action is supposed to pass an incoming upload request.
     *
     * @param action The action to check
     * @return <code>true</code> if upload is allowed; otherwise <code>false</code>
     */
    protected static boolean mayUpload(String action) {
        return UPLOAD_ACTIONS.contains(action);
    }

    /**
     * (Statically) Processes specified request's upload provided that request is of content type <code>multipart/*</code>.
     *
     * @param req The request whose upload shall be processed
     * @return The processed instance of {@link UploadEvent}
     * @throws OXException Id processing the upload fails
     */
    public static final UploadEvent processUploadStatic(HttpServletRequest req) throws OXException {
        return processUploadStatic(req, -1, -1);
    }

    /**
     * (Statically) Processes specified request's upload provided that request is of content type <code>multipart/*</code>.
     *
     * @param req The request whose upload shall be processed
     * @param maxFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxOverallSize The maximum allowed size of a complete request or <code>-1</code>
     * @return The processed instance of {@link UploadEvent}
     * @throws OXException Id processing the upload fails
     */
    public static final UploadEvent processUploadStatic(HttpServletRequest req, long maxFileSize, long maxOverallSize) throws OXException {
        return UploadUtility.processUpload(req, maxFileSize, maxOverallSize);
    }

    @Override
    public void fireUploadEvent(UploadEvent uploadEvent, Collection<UploadListener> uploadListeners) throws OXException {
        try {
            for (UploadListener uploadListener : uploadListeners) {
                try {
                    uploadListener.action(uploadEvent);
                } catch (Exception e) {
                    LOG.error("Failed upload listener: {}", uploadListener.getClass(), e);
                }
            }
        } finally {
            uploadEvent.cleanUp();
        }
    }

    public static void startResponse(JSONWriter jsonwriter) throws JSONException {
        jsonwriter.object();
        jsonwriter.key("data");
    }

    public static void endResponse(JSONWriter jsonwriter, Date timestamp, String error) throws JSONException {
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

    protected boolean checkRequired(HttpServletRequest req, HttpServletResponse res, boolean html, String action, String... parameters) throws IOException, ServletException {
        if (html) {
            res.setContentType("text/html; charset=UTF-8");
        }
        for (String param : parameters) {
            if (req.getParameter(param) == null) {
                missingParameter(param, res, html, action);
                return false;
            }
        }
        return true;
    }

    protected static void close(Writer w) {
        LOG.trace("Called close() with writer{}", w);
    }

    /**
     * Writes specified response.
     *
     * @param response The response to write
     * @param servletResponse The HTTP Servlet response to write to
     * @param optSession The optional session; pass <code>null</code> if not appropriate
     * @throws IOException If an I/O error occurs
     */
    protected void writeResponse(Response response, HttpServletResponse servletResponse, Session optSession) throws IOException {
        servletResponse.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, servletResponse.getWriter(), localeFrom(optSession));
        } catch (JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(servletResponse);
        }
    }

    /**
     * Checks if specified HTTP Servlet request indicates Internet Explorer as <code>User-Agent</code>.
     *
     * @param req The HTTP Servlet request
     * @return <code>true</code> if Internet Explorer; otherwise <code>false</code>
     */
    protected final boolean isIE(HttpServletRequest req) {
        return req.getHeader("User-Agent").contains("MSIE");
    }

    /**
     * Checks if specified HTTP Servlet request indicates Internet Explorer 7 as <code>User-Agent</code>.
     *
     * @param req The HTTP Servlet request
     * @return <code>true</code> if Internet Explorer 7; otherwise <code>false</code>
     */
    protected final boolean isIE7(HttpServletRequest req) {
        return req.getHeader("User-Agent").contains("MSIE 7");
    }

    /**
     * Gets specified module's string representation.
     *
     * @param module The module
     * @param objectId The identifier of associated object
     * @return The module's string representation
     */
    public static final String getModuleString(int module, int objectId) {
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
        case FolderObject.INFOSTORE:
            moduleStr = MODULE_INFOSTORE;
            break;
        case FolderObject.SYSTEM_MODULE:
            if (objectId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
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

    /**
     * Gets specified module's <code>int</code> representation.
     *
     * @param moduleStr The module's string representation
     * @return The module's <code>int</code> representation
     */
    public static final int getModuleInteger(String moduleStr) {
        int module;
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
        } else if (MODULE_INFOSTORE.equalsIgnoreCase(moduleStr)) {
            module = FolderObject.INFOSTORE;
        } else {
            module = -1;
        }
        return module;
    }

}

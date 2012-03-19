package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

public class AJAXRequestDataTools {
	/**
     * Parses an appropriate {@link AJAXRequestData} instance from specified arguments.
     *
     * @param req The HTTP Servlet request
     * @param preferStream Whether to prefer request's stream instead of parsing its body data to an appropriate (JSON) object
     * @param isFileUpload Whether passed request is considered as a file upload
     * @param session The associated session
     * @return An appropriate {@link AJAXRequestData} instance
     * @throws IOException If an I/O error occurs
     * @throws OXException If an OX error occurs
     */
    public static AJAXRequestData parseRequest(final HttpServletRequest req, final boolean preferStream, final boolean isFileUpload, final ServerSession session, String prefix) throws IOException, OXException {
        final AJAXRequestData retval = new AJAXRequestData();
        parseHostName(retval, req, session);
        /*
         * Set the module
         */
        {
            String pathInfo = req.getRequestURI();
            final int lastIndex = pathInfo.lastIndexOf(';');
            if (lastIndex > 0) {
                pathInfo = pathInfo.substring(0, lastIndex);
            }
            retval.setModule(pathInfo.substring(prefix.length()));
        }
        /*
         * Set request URI
         */
        retval.setServletRequestURI(AJAXServlet.getServletSpecificURI(req));
        /*
         * Set the action
         */
        {
            final String action = req.getParameter("action");
            if (null == action) {
                retval.setAction(req.getMethod().toUpperCase(Locale.US));
            } else {
                retval.setAction(action);
            }
        }
        /*
         * Set the format
         */
        retval.setFormat(req.getParameter("format"));
        /*
         * Pass all parameters to AJAX request object
         */
        {
            @SuppressWarnings("unchecked") final Set<Entry<String, String[]>> entrySet = req.getParameterMap().entrySet();
            for (final Entry<String, String[]> entry : entrySet) {
                retval.putParameter(entry.getKey(), entry.getValue()[0]);
            }
        }
        /*
         * Check for ETag header to support client caching
         */
        {
            final String eTag = req.getHeader("If-None-Match");
            if (null != eTag) {
                retval.setETag(eTag);
            }
        }
        /*
         * Set request body
         */
        if (isFileUpload) {
            final UploadEvent upload = AJAXServlet.processUploadStatic(req);
            final Iterator<UploadFile> iterator = upload.getUploadFilesIterator();
            while (iterator.hasNext()) {
                retval.addFile(iterator.next());
            }
            final Iterator<String> names = upload.getFormFieldNames();
            while (names.hasNext()) {
                final String name = names.next();
                retval.putParameter(name, upload.getFormField(name));
            }
            retval.setUploadEvent(upload);
        } else if (preferStream || parseBoolParameter("binary", req)) {
            /*
             * Pass request's stream
             */
            retval.setUploadStreamProvider(new HTTPRequestInputStreamProvider(req));
        } else {
            /*
             * Guess an appropriate body object
             */
            final String body = AJAXServlet.getBody(req);
            if (startsWith('{', body)) {
                /*
                 * Expect the body to be a JSON object
                 */
                try {
                    retval.setData(new JSONObject(body));
                } catch (final JSONException e) {
                    retval.setData(body);
                }
            } else if (startsWith('[', body)) {
                /*
                 * Expect the body to be a JSON array
                 */
                try {
                    retval.setData(new JSONArray(body));
                } catch (final JSONException e) {
                    retval.setData(body);
                }
            } else {
                retval.setData(0 == body.length() ? null : body);
            }
        }
        return retval;
    }

    private static final Set<String> BOOL_VALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("true", "1", "yes")));

    private static boolean parseBoolParameter(final String name, final HttpServletRequest req) {
        final String parameter = req.getParameter(name);
        if (null == parameter) {
            return false;
        }
        return BOOL_VALS.contains(parameter.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Parses host name, secure and AJP route.
     *
     * @param request The AJAX request data
     * @param req The HTTP Servlet request
     * @param session The associated session
     */
    private static void parseHostName(final AJAXRequestData request, final HttpServletRequest req, final ServerSession session) {
        request.setSecure(Tools.considerSecure(req));
        {
            final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
            if (null == hostnameService) {
                request.setHostname(req.getServerName());
            } else {
                final String hn = hostnameService.getHostname(session.getUserId(), session.getContextId());
                request.setHostname(null == hn ? req.getServerName() : hn);
            }
        }
        request.setRemoteAddress(req.getRemoteAddr());
        request.setRoute(Tools.getRoute(req.getSession(true).getId()));
    }
    
    private static boolean startsWith(final char startingChar, final String toCheck) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        int i = 0;
        if (Character.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Character.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }
}

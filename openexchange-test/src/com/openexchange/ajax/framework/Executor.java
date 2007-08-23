package com.openexchange.ajax.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.servlet.AjaxException;

public class Executor extends Assert {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Executor.class);

    /**
     * To use character encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Prevent instanciation.
     */
    private Executor() {
        super();
    }

    public static MultipleResponse multiple(final AJAXClient client,
        final MultipleRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (MultipleResponse) execute(client, request);
    }

    public static AbstractAJAXResponse execute(final AJAXClient client,
        final AJAXRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return execute(client.getSession(), request);
    }

    public static AbstractAJAXResponse execute(final AJAXSession session,
        final AJAXRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        final String urlString = AJAXConfig.getProperty(AJAXConfig.Property
            .PROTOCOL) + "://" + AJAXConfig.getProperty(AJAXConfig.Property
                .HOSTNAME) + request.getServletPath();
        final WebRequest req;
        switch (request.getMethod()) {
        case GET:
            req = new GetMethodWebRequest(urlString);
            addParameter(req, session, request);
            break;
        case POST:
            req = new PostMethodWebRequest(urlString);
            addParameter(req, session, request);
            break;
        case PUT:
            req = new PutMethodWebRequest(urlString + getPUTParameter(session,
                request), createBody(request.getBody()), AJAXServlet
                .CONTENTTYPE_JAVASCRIPT);
            break;
        default:
            throw new AjaxException(AjaxException.Code.InvalidParameter, request
                .getMethod().name());
        }
        final WebResponse resp = session.getConversation().getResponse(req);
        final AbstractAJAXParser parser = request.getParser();
        parser.checkResponse(resp);
        return parser.parse(resp.getText());
    }

    private static void addParameter(final WebRequest req,
        final AJAXSession session, final AJAXRequest request) {
        if (null != session.getId()) {
            req.setParameter(AJAXServlet.PARAMETER_SESSION, session.getId());
        }
        for (Parameter parameter : request.getParameters()) {
            req.setParameter(parameter.getName(), parameter.getValue());
        }
    }

    private static String getPUTParameter(final AJAXSession session,
        final AJAXRequest request) throws UnsupportedEncodingException {
        final URLParameter parameter = new URLParameter();
        if (null != session.getId()) {
            parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session
                .getId());
        }
        for (Parameter param : request.getParameters()) {
            parameter.setParameter(param.getName(), param.getValue());
        }
        return parameter.getURLParameters();
    }

    private static InputStream createBody(final Object body)
        throws JSONException, UnsupportedEncodingException {
        return new ByteArrayInputStream(body.toString().getBytes(ENCODING));
    }
}

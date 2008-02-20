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
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest.FieldParameter;
import com.openexchange.ajax.framework.AJAXRequest.FileParameter;
import com.openexchange.ajax.framework.AJAXRequest.Method;
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
     * Prevent instantiation
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
		return execute(session, request,
            AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL),
            AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
	}

    public static AbstractAJAXResponse execute(final AJAXSession session,
        final AJAXRequest request, final boolean trackDuration) throws AjaxException, IOException,
        SAXException, JSONException {
		return execute(session, request,
            AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL),
            AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME), trackDuration);
	}
	
	public static AbstractAJAXResponse execute(final AJAXSession session,
        final AJAXRequest request, final String protocol, final String hostname) throws AjaxException, IOException,
        SAXException, JSONException {
		return execute(session, request, protocol, hostname, false);
    }

	public static AbstractAJAXResponse execute(final AJAXSession session,
	        final AJAXRequest request, final String protocol, final String hostname,
	        final boolean trackDuration) throws AjaxException, IOException,
	        SAXException, JSONException {
			
			final String urlString = protocol + "://" + hostname + request.getServletPath();
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
	        case UPLOAD:
	            final PostMethodWebRequest post = new PostMethodWebRequest(urlString
	                + getPUTParameter(session, request));
	            post.setMimeEncoded(true);
	            req = post;
	            addFieldParameter(post, request);
	            addFileParameter(post, request);
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
	        final WebConversation conv = session.getConversation();
	        final WebResponse resp;
	        // The upload returns a web page that should not be interpreted.
	        final long startRequest = System.currentTimeMillis();
	        if (Method.UPLOAD == request.getMethod()) {
	            resp = conv.getResource(req);
	        } else {
	            resp = conv.getResponse(req);
	        }
	        final long duration = System.currentTimeMillis() - startRequest;
	        final AbstractAJAXParser<?> parser = request.getParser();
	        parser.checkResponse(resp);
	        final AbstractAJAXResponse retval = parser.parse(resp.getText());
	        if (trackDuration) {
	        	retval.setDuration(duration);
	        }
	        return retval;
	    }

    private static void addParameter(final WebRequest req,
        final AJAXSession session, final AJAXRequest request) {
        if (null != session.getId()) {
            req.setParameter(AJAXServlet.PARAMETER_SESSION, session.getId());
        }
        for (final Parameter param : request.getParameters()) {
            if (!(param instanceof FileParameter)) {
                req.setParameter(param.getName(), param.getValue());
            }
        }
    }

    private static void addFieldParameter(final PostMethodWebRequest post, final AJAXRequest request) {
		for (final Parameter param : request.getParameters()) {
			if (param instanceof FieldParameter) {
				final FieldParameter fparam = (FieldParameter) param;
				post.setParameter(fparam.getFieldName(), fparam.getFieldContent());
			}
		}
	}

    private static void addFileParameter(final PostMethodWebRequest post,
        final AJAXRequest request) {
        for (final Parameter param : request.getParameters()) {
            if (param instanceof FileParameter) {
                final FileParameter fparam = (FileParameter) param;
                post.selectFile(fparam.getName(), fparam.getFileName(),
                    fparam.getInputStream(), fparam.getMimeType());
            }
        }
    }

    private static String getPUTParameter(final AJAXSession session,
        final AJAXRequest request) throws UnsupportedEncodingException {
        final URLParameter parameter = new URLParameter();
        if (null != session.getId()) {
            parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session
                .getId());
        }
        for (final Parameter param : request.getParameters()) {
            if (!(param instanceof FileParameter) && !(param instanceof FieldParameter)) {
                parameter.setParameter(param.getName(), param.getValue());
            }
        }
        return parameter.getURLParameters();
    }

    private static InputStream createBody(final Object body)
        throws JSONException, UnsupportedEncodingException {
        return new ByteArrayInputStream(body.toString().getBytes(ENCODING));
    }
}

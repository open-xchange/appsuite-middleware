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

package com.openexchange.rest.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.annotations.DELETE;
import com.openexchange.rest.services.annotations.PATCH;
import com.openexchange.rest.services.annotations.GET;
import com.openexchange.rest.services.annotations.LINK;
import com.openexchange.rest.services.annotations.OPTIONS;
import com.openexchange.rest.services.annotations.POST;
import com.openexchange.rest.services.annotations.PUT;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.rest.services.annotations.UNLINK;
import com.openexchange.rest.services.internal.Services;
import com.openexchange.rest.services.osgiservice.OXRESTActivator;
import com.openexchange.server.ServiceLookup;


/**
 * A {@link OXRESTService} is the entry class for defining a RESTful service. Subclass this class, annotate it with annotations from com.openexchange.rest.services.annotations and
 * publish it in a subclass of {@link OXRESTActivator}.
 *
 * Consider this example:
 *
 * <pre>
 * @ROOT("/myservice")
 * public class MyService extends OXRestService<Void> {
 *
 *   ...
 *
 * }
 * </pre>
 *
 * Every service class declares its root URL with the {@link ROOT} annotation. The service will then be reachable under /rest/myservice. In order to implement concrete calls,
 * declare methods in the service class and annotate them with a route specifying how these methods should be accessed.
 *
 * <pre>
 * @ROOT("/myservice")
 * public class MyService extends OXRestService<Void> {
 *
 *   // e.g. /rest/myservide/bookmarks/1
 *   @GET("bookmarks/:id")
 *   public Object getBookmark(int bookmarkId) {
 *     return "http://www.open-xchange.com"
 *   }
 *
 * }
 * </pre>
 *
 * The method annotations {@link GET}, {@link PUT}, {@link POST}, {@link DELETE}, {@link PATCH}, {@link OPTIONS}, {@link LINK}, {@link UNLINK} expect as their parameter the
 * subpath that triggers the method. A path can contain variables market by the colon sign (:), which denote arbitrary path elements. These elements are passed to the method in
 * the same order as they appear in the path. The system tries to turn them into the types declared as method parameters (say the int above). The values are also available under
 * their name via the {@link #param(String)} method. e.g. param("id") or param("id", int.class).
 *
 * A method can return any object. The system tries to convert this into a String, if the object is not already one, to send back to the client.
 * Maps and Lists are turned into their respective JSON representations (see {@link JSONCoercion} ),
 * other objects are issued a #toString call to turn them into the response. Instead of returning the response, a method can also call the {@link #respond(String)} and {@link #halt()} methods
 * to set a response and optionally halt further execution. These methods can also be used to set a status code or headers. Client headers are available through the {@link #request} Object.
 *
 * The methods {@link #before()} and {@link #after()} are called before and after processing respectively.
 *
 * Every request instantiates a new instance of this class, so feel free to set member variables in before and after methods during processing.
 *
 * e.g:
 * <pre>
 * @ROOT("/bookmarks")
 * public class MyService extends OXRestService<VOID> {
 *
 *   private Bookmark bookmark;
 *
 *   public void before() {
 *      contentType("application/json");
 *      if (isSet("id")) {
 *         this.bookmark = services.getService(BookmarkService.class).loadBookmark(param("id", int.class));
 *         if (this.bookmark == null) {
 *           halt(404);
 *         }
 *      }
 *   }
 *   // e.g. GET /rest/bookmarks/1
 *   @GET("/:id")
 *   public Object getBookmark() {
 *     return bookmark.getURL(); // Populated in #before
 *   }
 *
 *   @PATCH("/:id")
 *   public void updateBookmark() {
 *     bookmark.setURL(param("url")); // Populated in #before
 *     context.save(bookmark);
 *     respond(200);
 *   }
 *
 * }
 * </pre>
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXRESTService<T> {

    /**
     * Used internally for control flow.
     */
    public static class HALT extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /**
     * The response that should be constructed by the action methods
     */
    protected Response response = new Response();

    /**
     * The client request
     */
    protected AJAXRequestData request;

    /**
     * The route matching object
     */
    protected OXRESTMatch match;

    /**
     * Services from the activator that created this instance
     */
    protected ServiceLookup services;

    /**
     * An optional context object that can be passed from the activator to instances.
     */
    protected T context;

    /**
     * Send the string back to the client
     */
    public void respond(String data) {
        status(200);
        body(data);
    }

    /**
     * Send the String to the client and set the status code to status
     */
    public void respond(int status, String data) {
        status(status);
        body(data);
    }

    /**
     * Send this status code, headers and data to the client
     */
    public void respond(int status, Map<String, String> headers, String data) {
        status(status);
        headers(headers);
        body(data);
    }

    /**
     * The system will consume the iterator provided by your iterable. A poor mans streaming support.
     */
    public void respond(Iterable<String> stream) {
        body(stream);
    }

    /**
     * Uses the {@link SimpleConverter} service to render the object as JSON and returns it to the client.
     */
    public void respond(Object object, String format) throws OXException {
        body(json(object, format));
    }

    /**
     * Uses the {@link SimpleConverter} to turn an object into JSON.
     */
    public String json(Object object, String format) throws OXException {
        return Services.getService(SimpleConverter.class).convert(format, "json", object, null).toString();
    }

    /**
     * Uses {@link JSONCoercion} to turn this object into a String to be sent back to the client
     */
    public void respond(Object object) {
        try {
            respond(JSONCoercion.coerceToJSON(object).toString());
        } catch (JSONException e) {
            respond(object.toString());
        }
    }

    /**
     * Stops further processing
     */
    public void halt() {
        throw new HALT();
    }

    /**
     * Responds with the data and stops further processing.
     */
    public void halt(String data) {
        respond(data);
        throw new HALT();
    }

    /**
     * Sets the status code and halts further processing
     */
    public void halt(int status) {
        status(status);
        throw new HALT();
    }

    /**
     * Sets the status code and response data and halts further processing
     */
    public void halt(int status, String data) {
        respond(status, data);
        throw new HALT();
    }

    /**
     * Sends the status, headers and data to the client and halts further processing
     */
    public void halt(int status, Map<String, String> headers, String data) {
        respond(status, headers, data);
        throw new HALT();
    }

    /**
     * Sends the strings produced by the iterator to the client and halts further processing
     */
    public void halt(Iterable<String> stream) {
        respond(stream);
        throw new HALT();
    }

    /**
     * Uses the {@link SimpleConverter} service to render the object as JSON and returns it to the client and halts further processing.
     */
    public void halt(Object object, String format) throws OXException {
        respond(object, format);
        throw new HALT();
    }

    /**
     * Uses {@link JSONCoercion} to turn this object into a String to be sent back to the client and then halts further processing.
     */
    public void halt(Object object) {
        respond(object);
        throw new HALT();
    }

    /**
     * Set this content type
     */
    public void contentType(String cType) {
        header("Content-Type", cType);
    }

    /**
     * Sets a header in the response
     */
    public void header(String name, String value) {
        this.response.getHeaders().put(name, value);
    }

    /**
     * Determines the headers to be sent back
     */
    public void headers(Map<String, String> headers) {
        this.response.setHeaders(new HashMap<String, String>(headers));
    }

    /**
     * Sets the status code
     */
    public void status(int status) {
        this.response.setStatus(status);
    }

    /**
     * Sets the body for the response
     */
    public void body(String body) {
        this.response.setBody(Arrays.asList(body));
    }

    /**
     * The response contains of all Strings produced by this iterator.
     */
    public void body(Iterable<String> body) {
        this.response.setBody(body);
    }

    /**
     * Retrieves a parameter from the request or from the route pattern.
     */
    public String param(String name) {
        return request.getParameter(name);
    }

    /**
     * Retrieves a parameter and tries to turn it into the given type.
     */
    public <T> T param(String name, Class<T> type) throws OXException {
        return request.getParameter(name, type);
    }

    /**
     * Determines whether a parameter has been sent by the client.
     */
    public boolean isSet(String name) {
        return request.isSet(name);
    }

    /**
     * Builds a suburl to this controller
     */
    public String url(String path) {
        return request.constructURL(path, true).toString();
    }

    /**
     * Builds a suburl to this controller, optionally include the routing information for this specific backend
     * @param path
     * @param withRoute
     */
    public String url(String path, boolean withRoute) {
        return request.constructURL(path, withRoute).toString();
    }

    /**
     * Builds a suburl to this controller, optionally with routing information for this backend and a query string
     * @param path
     * @param withRoute
     * @param query
     * @return
     */
    public String url(String path, boolean withRoute, String query) {
        return request.constructURL(null, path, withRoute, query).toString();
    }

    /**
     * Builds a suburl to this controller.
     * @param path
     * @param query
     */
    public String url(String path, String query) {
        return request.constructURL(null, path, true, query).toString();
    }

    /**
     * Alias for {@link #url(String)}
     */
    public String to(String path) {
        return url(path);
    }

    /**
     * Alias for {@link #url(String, boolean)}
     */
    public String to(String path, boolean withRoute) {
        return url(path, withRoute);
    }

    /**
     * Alias for {@link #url(String, boolean, String)}
     */
    public String to(String path, boolean withRoute, String query) {
        return url(path, withRoute, query);
    }

    /**
     * Alias for {@link #url(String, String)}
     */
    public String to(String path, String query) {
        return url(path, query);
    }

    /**
     * Sends a redirect to the given URL. Usage:
     * <code>
     *   redirect(to("/myController/otherAction"));
     * </code>
     */
    public void redirect(String to) {
        header("Location", to);
        status(301);
        halt();
    }

    /**
     * Retrieves the response constructed during an action call
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the request that is under consideration for this action
     */
    public void setRequest(AJAXRequestData request) {
        this.request = request;
    }

    /**
     * Sets the context object.
     */
    public void setContext(T context) {
        this.context = context;
    }

    /**
     * Sets the match object that led to this controller and method being chosen.
     */
    public void setMatch(OXRESTMatch match) {
        this.match = match;
    }

    /**
     * Sets the service lookup instance.
     */
    public void setServices(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Called before the action method is called
     */
    public void before() throws OXException {

    }

    /**
     * Called after the action method has finished. It is guaranteed that this method will always be called.
     * @throws OXException
     */
    public void after() throws OXException {

    }

}

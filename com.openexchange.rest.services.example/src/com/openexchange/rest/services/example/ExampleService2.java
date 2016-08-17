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

package com.openexchange.rest.services.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.RequestContext;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ExampleService2} implements the same functionality as {@link ExampleService},
 * but without extending {@link JAXRSService}. This service uses only plain JAX-RS annotations
 * and custom helper functions.
 *
 * @see ExampleService
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Path("/preliminary/example2/")
public class ExampleService2 {

    private final ServiceLookup serviceLookup;

    public ExampleService2(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @PUT
    @Path("/shuffle/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String shuffleText(String input) throws OXException {
        /*
         * You can use a method parameter to get the converted request body.
         */
        return shuffle(input);
    }

    @PUT
    @Path("/shuffle/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONValue shuffleText(JSONValue value) throws OXException {
        /*
         * You can use a method parameter to get the converted request body.
         * We already have a converter for JSON based on our org.json
         * implementation. See com.openexchange.jaxrs.jersey.JSONReaderWriter.
         */
        if (value.isArray()) {
            JSONArray input = (JSONArray) value;
            JSONArray result = new JSONArray();
            try {
                for (int i = 0; i < input.length(); i++) {
                    result.put(shuffle(input.getString(i)));
                }

                return result;
            } catch (JSONException e) {
                /*
                 * OXExceptions are automatically catched, logged and
                 * converted to according HTTP status codes. See com.openexchange.jaxrs.jersey.OXExceptionMapper
                 * for details.
                 */
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        } else {
            try {
                String shuffled = shuffle(((JSONObject) value).getString("text"));
                return new JSONObject().put("text", shuffled);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }

    @GET
    @Path("/randomString/")
    public Response randomString(@BeanParam RequestContext context) throws IOException {
        /*
         * You can aggregate multiple context injections within one object,
         * by passing it as javax.ws.rs.BeanParam. com.openexchange.jaxrs.RequestContext
         * is a convenient container that gives you access to the same data as
         * inheriting com.openexchange.jaxrs.JAXRSService does.
         */

        long seed = 0L;
        /*
         * The super class com.openexchange.jaxrs.JAXRSService gives you access
         * to all request-related objects in the correct scope (i.e. request-related
         * objects are accessed in a per-request scope, although this service is a
         * singleton).
         */
        for (Collection<String> values : context.getUriInfo().getPathParameters().values()) {
            for (String value : values) {
                seed += value.hashCode();
            }
        }

        for (Collection<String> values : context.getHttpHeaders().getRequestHeaders().values()) {
            for (String value : values) {
                seed += value.hashCode();
            }
        }

        Random r = new Random(seed);
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(r.nextInt(128));
        }

        /*
         * You can always respond with a javax.ws.rs.core.Response object, which
         * allows to define status code, entity etc. on your own.
         */
        return Response.ok(sb.toString(), MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("/zip/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Object zipFile(@FormDataParam("file") List<FormDataBodyPart> bodyParts, @BeanParam RequestContext requestContext) throws IOException {
        /*
         * You can also refer to a concrete entity of a multipart/form-data
         * request body by using the org.glassfish.jersey.media.multipart.FormDataParam
         * annotation and defining the entities name.
         */
        if (bodyParts == null || bodyParts.isEmpty()) {
            /*
             * Returning null results in HTTP status 204 No Content
             */
            return null;
        }

        File zipFile = File.createTempFile("oxrestzipexample", ".zip");
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
        for (BodyPart bodyPart : bodyParts) {
            String fileName = "unknown";
            ContentDisposition contentDisposition = bodyPart.getContentDisposition();
            if (contentDisposition != null) {
                String tmp = contentDisposition.getFileName();
                if (tmp != null) {
                    fileName = tmp;
                }
            }

            InputStream inputStream = bodyPart.getEntityAs(InputStream.class);
            ZipEntry ze = new ZipEntry(fileName);
            zout.putNextEntry(ze);

            int len = 0;
            byte[] buf = new byte[4096];
            while (len >= 0) {
                len = inputStream.read(buf, 0, buf.length);
                if (len > 0) {
                    zout.write(buf, 0, len);
                }
            }
            zout.closeEntry();
        }

        zout.close();

        /*
         * Returning the zip file writes it onto the servlet response. In this case
         * we need the @Produces annotation to set the correct content type. Of course
         * this is not sufficient to trigger a download, so we need to set the Content-Disposition
         * header manually. Via the RequestContext as MBeanParam we can set it directly on the
         * servlet response.
         *
         * As you might guess, this method doesn't show the ideal way of processing multiparts and
         * returning file downloads. It's merely meant to show how the JAX-RS concepts can be combined.
         * The method com.openexchange.jaxrs.example.ExampleService.zipFile(FormDataMultiPart) shows a
         * way better approach.
         */
        requestContext.getServletResponse().addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archive.zip");
        return zipFile;
    }

    private String shuffle(String input) {
        Shuffler shuffler = serviceLookup.getService(Shuffler.class);
        if (shuffler == null) {
            throw new ServiceUnavailableException();
        }

        return shuffler.shuffle(input);
    }

}

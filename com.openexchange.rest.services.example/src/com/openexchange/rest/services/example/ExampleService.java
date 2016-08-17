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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ExampleService} implements some example functions to demonstrate
 * the capabilities of JAX-RS and our custom extensions. It extends {@link JAXRSService},
 * which provides some useful helper methods.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Path("/preliminary/example/")
public class ExampleService extends JAXRSService {

    /**
     * Initializes a new {@link ExampleService}.
     * @param services
     */
    public ExampleService(ServiceLookup services) {
        super(services);
    }

    @PUT
    @Path("/shuffle/")
    public Object shuffleText() throws OXException {
        /*
         * You can return arbitrary objects. The framework will try
         * to find an appropriate javax.ws.rs.ext.MessageBodyWriter<T>.
         * Currently you may use our org.json objects and primitive types.
         */
        AJAXRequestData requestData = getAJAXRequestData();
        Object data = requestData.getData();
        if (data == null) {
            throw new BadRequestException();
        }

        if (data instanceof String) {
            return shuffle((String) data);
        }

        if (data instanceof JSONObject) {
            try {
                String shuffled = shuffle(((JSONObject) data).getString("text"));
                return new JSONObject().put("text", shuffled);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }

        if (data instanceof JSONArray) {
            JSONArray input = (JSONArray) data;
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
        }

        /*
         * JAX-RS provides runtime exceptions that result in according HTTP error responses.
         */
        throw new BadRequestException();
    }

    @GET
    @Path("/randomString/")
    public void randomString() throws IOException {
        long seed = 0L;
        /*
         * The super class com.openexchange.jaxrs.JAXRSService gives you access
         * to all request-related objects in the correct scope (i.e. request-related
         * objects are accessed in a per-request scope, although this service is a
         * singleton).
         */
        for (Collection<String> values : uriInfo.getPathParameters().values()) {
            for (String value : values) {
                seed += value.hashCode();
            }
        }

        for (Collection<String> values : httpHeaders.getRequestHeaders().values()) {
            for (String value : values) {
                seed += value.hashCode();
            }
        }

        Random r = new Random(seed);
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(r.nextInt(128));
        }

        String output = sb.toString();
        servletResponse.setContentType("text/plain");
        servletResponse.setContentLength(output.getBytes().length);
        servletResponse.setStatus(Status.OK.getStatusCode());
        PrintWriter w = servletResponse.getWriter();
        w.append(output);
        w.flush();
    }

    @POST
    @Path("/zip/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response zipFile(FormDataMultiPart multipart) throws IOException {
        /*
         * Working with multipart/form-data POST requests is easy, simply
         * use org.glassfish.jersey.media.multipart.FormDataMultiPart as
         * method parameter and annotate your method with @Consumes(MediaType.MULTIPART_FORM_DATA).
         */
        if (multipart == null || multipart.getBodyParts().isEmpty()) {
            return Response.noContent().build();
        }

        List<BodyPart> bodyParts = multipart.getBodyParts();
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
         * Returning a file as response entity simply writes it onto the
         * servlet response.
         */
        return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM_TYPE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archive.zip")
            .build();
    }

    private String shuffle(String input) {
        /*
         * Services can be got via getService() of the super class.
         * If the service is not available a javax.ws.rs.ServiceUnavailableException
         * is thrown, which returns a 503 error. Optional services can be got
         * via optService().
         */
        return getService(Shuffler.class).shuffle(input);
    }

}

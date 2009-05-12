
package com.openexchange.publish.microformats;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.publish.Path;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.Site;
import com.openexchange.publish.microformats.internal.ContactLoader;
import com.openexchange.publish.microformats.internal.ContactWriter;
import com.openexchange.publish.microformats.internal.InfostoreTemplateLoader;

public class PublishMicroformatsServlet extends HttpServlet {

    private static final long serialVersionUID = 12L;

    private static final String HTML = "text/html;charset=utf-8";

    private static PublicationService publicationService;

    private static InfostoreTemplateLoader templateLoader = new InfostoreTemplateLoader();

    private static ItemLoaderRegistry itemLoaders = new ItemLoaderRegistry();
    static {
        itemLoaders.addItemLoader(new ContactLoader(), Types.CONTACT);
    }

    private static ItemWriterRegistry itemWriters = new ItemWriterRegistry();
    static {
        itemWriters.addWriter(new ContactWriter(), Types.CONTACT);
    }

    public static void setPublicationService(PublicationService service) {
        publicationService = service;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Site site = loadSite(req);
        String content = writeSite(site);

        resp.setContentType(HTML);
        resp.getWriter().println(content);

    }

    private Site loadSite(HttpServletRequest req) {
        Path path = new Path();

        String[] components = req.getPathInfo().split("/");
        path.setContextId(Integer.parseInt(components[3]));
        path.setOwnerId(Integer.parseInt(components[4]));
        path.setSiteName(components[5]);

        return null;//publicationService.getSite(path);
    }

    private String writeSite(Site site) {
        String rendered = renderWithTemplate(site);
        if (rendered != null) {
            return rendered;
        }
        return writePlainSite(site);
    }

    private String renderWithTemplate(Site site) {
        String template = loadTemplate(site);
        if (template == null) {
            return null;
        }

        return mergeTemplate(template, site);
    }

    private String loadTemplate(Site site) {
        return templateLoader.loadTemplate(site);
    }

    private String mergeTemplate(String template, Site site) {
        Context context = createContext(site);
        StringWriter writer = new StringWriter();

        try {
            Velocity.evaluate(context, writer, getClass().getName(), template);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (ParseErrorException e) {
            e.printStackTrace();
        } catch (MethodInvocationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    private Context createContext(Site site) {

        Context context = new VelocityContext();
        
        ContactLoader contactLoader = new ContactLoader();
        List<ContactObject> contacts = contactLoader.load(site.getFolderId(), site.getPath());
        
        context.put("contacts", contacts);

        return context;
    }

    private String writePlainSite(Site site) {
        StringBuilder builder = new StringBuilder();
        writeHeader(builder);
        
        ContactLoader contactLoader = new ContactLoader();
        List<ContactObject> contacts = contactLoader.load(site.getFolderId(), site.getPath());

        ContactWriter writer = new ContactWriter();
        
        for(ContactObject contact : contacts) {
            builder.append(writer.write(contact));
        }
   
        writeFooter(builder);

        return builder.toString();
    }

    private void writeHeader(StringBuilder builder) {
        builder.append("<html><head><title>My Contacts</title></head><body>");
    }

    private void writeFooter(StringBuilder builder) {
        builder.append("</body></html>");
    }

}

package com.openexchange.mobileconfig;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mobileconfig.configuration.ConfigurationException;
import com.openexchange.mobileconfig.configuration.MobileConfigProperties;
import com.openexchange.mobileconfig.configuration.Property;
import com.openexchange.mobileconfig.osgi.Activator;
import com.openexchange.mobileconfig.services.MobileConfigServiceRegistry;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateException;
import com.openexchange.templating.TemplateService;


public class MobileConfigServlet extends HttpServlet {

    
    private enum Device {
        iPhone,
        winMob;
    }

    private static class ErrorMessage {
        
        private final String english;
        
        private final String german;

        public ErrorMessage(String german, String english) {
            super();
            this.german = german;
            this.english = english;
        }

        
        public String getEnglish() {
            return english;
        }

        
        public String getGerman() {
            return german;
        }
        
        
    }

    private static final transient Log LOG = LogFactory.getLog(MobileConfigServlet.class);

    private static final ErrorMessage MSG_INTERNAL_ERROR = new ErrorMessage("Ein interner Fehler ist aufgetreten, bitte versuchen Sie es später noch einmal.", "An internal error occurred. Please try again later.");

    private static final ErrorMessage MSG_NO_SUPPORTED_DEVICE_FOUND = new ErrorMessage("Ihr Gerät wird nicht unterstützt", "Your device is not supported.");

    private static final ErrorMessage MSG_PARAMETER_LOGIN_IS_MISSING = new ErrorMessage("Der Parameter \"l\" fehlt", "The \"l\" parameter is missing");
    
    private static final ErrorMessage MSG_UNSECURE_ACCESS = new ErrorMessage("Unsicherer Zugriff mit http ist nicht erlaubt. Bitte https benutzen.", "Unsecured http access is not allowed. Use https instead.");

    /**
     * 
     */
    private static final long serialVersionUID = 7913468326542861986L;
    
    public static String write(final String email, final String host, final String username, final String domain) throws ConfigurationException, TemplateException {
            final TemplateService service = MobileConfigServiceRegistry.getServiceRegistry().getService(TemplateService.class);
            final OXTemplate loadTemplate = service.loadTemplate("winMobileTemplate.tmpl");
            final StringWriter writer = new StringWriter();
            loadTemplate.process(generateHashMap(email, host, username, domain), writer);
            return writer.toString();
    }

    /**
     * Splits the given login into a username and a domain part
     * @param username
     * @return An array. Index 0 is the username. Index 1 is the domain
     * @throws ConfigurationException
     */
    protected static String[] splitUsernameAndDomain(String username) throws ConfigurationException {
        final String domain_user = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry(), Property.DomainUser);
        final String separator = domain_user.replaceAll("\\$USER|\\$DOMAIN", "");
        final String[] split = username.split(Pattern.quote(separator));
        if (split.length != 2) {
            throw new ConfigurationException("Splitting of login returned wrong length. Array is " + Arrays.toString(split));
        }
        if (domain_user.indexOf("$USER") < domain_user.indexOf("$DOMAIN")) {
            return split;
        } else {
            // change position in array...
            return new String[]{split[1], split[0]};
        }
    }

    private static HashMap<String, String> generateHashMap(final String email, final String host, final String username, final String domain) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("email", email);
        hashMap.put("host", host);
        hashMap.put("username", username);
        hashMap.put("domain", domain);
        return hashMap;
    }

    private static void writeMobileConfigWinMob(final OutputStream out, final String email, final String host, final String username, final String domain) throws IOException, ConfigurationException, TemplateException {
        CabUtil.writeCabFile(new DataOutputStream(new BufferedOutputStream(out)), write(email, host, username, domain));
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        final ConfigurationService service = MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (null == service) {
            LOG.error("A configuration exception occurred, which should not happen: No configuration service found");
            printError(req, resp, MSG_INTERNAL_ERROR);
            return;
        }

        final String parameter = req.getParameter("error");
        if (null != parameter && parameter.length() != 0) {
            // Error output
            errorOutput(req, resp, parameter);
            return;
        }
        final String iphoneRegEx;
        final String winMobRegEx;
        try {
            iphoneRegEx = MobileConfigProperties.getProperty(service, Property.iPhoneRegex);
            winMobRegEx = MobileConfigProperties.getProperty(service, Property.WinMobRegex);
            final Boolean secureConnect = MobileConfigProperties.getProperty(service, Property.OnlySecureConnect);
            if (secureConnect) {
                if (!req.isSecure()) {
                    printError(req, resp, MSG_UNSECURE_ACCESS);
                    return;
                }
            }
        } catch (final ConfigurationException e) {
            LOG.error("A configuration exception occurred, which should not happen: " + e.getMessage(), e);
            printError(req, resp, MSG_INTERNAL_ERROR);
            return;
        }
        
        final Device device = detectDevice(req);
        final String login = req.getParameter("l");
        if (null == device) {
            if (null == login) {
                printError(req, resp, MSG_PARAMETER_LOGIN_IS_MISSING);
                return;
            }
            String mailpart = "";
            final String mail = req.getParameter("m");
            if (null != mail) {
                mailpart = "&m=" + URLEncoder.encode(mail, "UTF-8");
            }

            final String header = req.getHeader("user-agent");
            if (null != header) {
                if (header.matches(iphoneRegEx)) {
                    // iPhone part
                    resp.sendRedirect(Activator.ALIAS + "/eas.mobileconfig?l=" + URLEncoder.encode(login,"UTF-8") + mailpart);
                    return;
                } else if (header.matches(winMobRegEx)) {
                    // WinMob part
                    resp.sendRedirect(Activator.ALIAS + "/ms.cab?l=" + URLEncoder.encode(login,"UTF-8") + mailpart);
                    return;
                } else {
                    printError(req, resp, MSG_NO_SUPPORTED_DEVICE_FOUND);
                    LOG.info("Unsupported device header: \"" + header + "\"");
                    return;
                }
            }
        } else {
            try {
                generateConfig(req, resp, login, device);
            } catch (final ConfigurationException e) {
                LOG.error("A configuration exception occurred, which should not happen: " + e.getMessage(), e);
                printError(req, resp, MSG_INTERNAL_ERROR);
                return;
            } catch (final TemplateException e) {
                LOG.error("A template exception occurred, which should not happen: " + e.getMessage(), e);
                printError(req, resp, MSG_INTERNAL_ERROR);
                return;
            } catch (final IOException e) {
                LOG.error("A template exception occurred, which should not happen: " + e.getMessage(), e);
                printError(req, resp, MSG_INTERNAL_ERROR);
                return;
            }
        }
    }

    private Device detectDevice(HttpServletRequest req) {
        final String pathInfo = req.getPathInfo();
        if ("/eas.mobileconfig".equals(pathInfo)) {
            return Device.iPhone;
        } else if ("/ms.cab".equals(pathInfo)) {
            return Device.winMob;
        } else {
            return null;
        }
    }

    /**
     * Reads the language from the header, returns either ENGLISH or GERMAN. No other value can be returned
     * @param req
     * @return
     */
    private Locale detectLanguage(HttpServletRequest req) {
        final String parameter = req.getHeader("Accept-Language");
        if (null == parameter) {
            return Locale.ENGLISH;
        } else {
            if (parameter.startsWith("de")) {
                return Locale.GERMAN;
            } else {
                return Locale.ENGLISH;
            }
        }
    }

    private void errorOutput(final HttpServletRequest req, final HttpServletResponse resp, final String string) {
        resp.setContentType("text/html");
        PrintWriter writer;
        try {
            writer = getWriterFromOutputStream(resp.getOutputStream());
        } catch (final IOException e) {
            LOG.error("Unable to get output stream to write error message: " + e.getMessage(), e);
            return;
        }
        writer.println("<html><head>");
        writer.println("<meta name=\"viewport\" content=\"width=320\" />");
        writer.println("<meta name=\"mobileoptimized\" content=\"0\" />");
        writer.println("<title>Error</title>");
        writer.println("<style type=\"text/css\">");
        writer.println("table { height: 100%; width:100% }");
        writer.println("td { text-align:center; vertical-align:middle; }");
        writer.println("</style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<td><h1>" + string + "</h1></td>");
        writer.println("</tr>");
        writer.println("</table>");
        writer.println("</body></html>");
        writer.close();
        
    }

    private void generateConfig(HttpServletRequest req, HttpServletResponse resp, final String login, final Device device) throws IOException, ConfigurationException, TemplateException {
        String mail = login;
        final String parameter = req.getParameter("m");
        if (null != parameter) {
            mail = parameter;
        }
        final String[] usernameAndDomain = splitUsernameAndDomain(login);
        if (Device.iPhone.equals(device)) {
            resp.setContentType("application/x-apple-aspen-config");
            final ServletOutputStream outputStream = resp.getOutputStream();
            final PrintWriter writer = getWriterFromOutputStream(outputStream);
            writeMobileConfig(writer, outputStream, mail, getHostname(req), usernameAndDomain[0], usernameAndDomain[1]);
            writer.close();
        } else if (Device.winMob.equals(device)) {
            final ServletOutputStream outputStream = resp.getOutputStream();
            writeMobileConfigWinMob(outputStream, mail, getHostname(req), usernameAndDomain[0], usernameAndDomain[1]);
            outputStream.close();
        }
    }

    private String getHostname(final HttpServletRequest req) {
        final String canonicalHostName = req.getServerName();
        return canonicalHostName;
    }

    private PrintWriter getWriterFromOutputStream(ServletOutputStream outputStream) {
        try {
            return new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(outputStream), Charset.forName("UTF-8")));
        } catch (final IllegalCharsetNameException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final UnsupportedCharsetException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void printError(HttpServletRequest req, final HttpServletResponse resp, final ErrorMessage string) throws IOException {
        Locale locale = detectLanguage(req);
        if (Locale.ENGLISH.equals(locale)) {
            resp.sendRedirect(Activator.ALIAS + "?error=" + URLEncoder.encode(string.getEnglish(),"UTF-8"));
        } else if (Locale.GERMAN.equals(locale)) {
            resp.sendRedirect(Activator.ALIAS + "?error=" + URLEncoder.encode(string.getGerman(),"UTF-8"));
        }
    }

    private void writeMobileConfig(final PrintWriter printWriter, OutputStream outStream, final String email, final String host, final String username, final String domain) throws IOException, TemplateException, ConfigurationException {
        try {
            final TemplateService service = MobileConfigServiceRegistry.getServiceRegistry().getService(TemplateService.class);
            final OXTemplate loadTemplate = service.loadTemplate("iPhoneTemplate.tmpl");
            final Boolean property = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class), Property.SignConfig);
            if (property) {
                final MobileConfigSigner writer = new MobileConfigSigner(outStream);
                try {
                    loadTemplate.process(generateHashMap(email, host, username, domain), writer);
                } finally {
                    writer.close();
                }
            } else {
                loadTemplate.process(generateHashMap(email, host, username, domain), printWriter);
            }
        } finally {
            printWriter.close();
        }
    }

}


package com.openexchange.ui7;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UI7Servlet extends FileServlet {

    private static final long serialVersionUID = -87595969177959884L;

    private File zoneinfo;

    private static String ZONEINFO = "apps/io.ox/core/date/tz/zoneinfo/";

    public UI7Servlet(File root, File zoneinfo) {
        super(root);
        this.zoneinfo = zoneinfo;
    }

    private static Pattern versionRE = Pattern.compile("/v=[^/]+/");

    @Override
    protected File getFile(HttpServletRequest req, HttpServletResponse resp, String path) {
        if (path == null) {
            return new File(root, "core");
        }
        Matcher m = versionRE.matcher(path);
        if (m.lookingAt()) {
            path = path.substring(m.end());
        }
        if (path.startsWith(ZONEINFO)) {
            resp.setContentType("text/plain;charset=ISO-8859-1");
            return new File(zoneinfo, path.substring(ZONEINFO.length()));
        }
        return new File(root, path);
    }

    @Override
    protected void writeHeaders(HttpServletRequest req, HttpServletResponse resp, File file, String path) {
        if (path == null || path.startsWith("/core") || path.startsWith("/signin")) {
            resp.setContentType("text/html");
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
            resp.setHeader("Expires", "0");
            resp.setHeader("Pragma", "no-cache");
            return;
        }
        if ("/src/online.js".equals(path)) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
            return;
        }
        resp.setDateHeader("Expires", (new Date()).getTime() + (long) 3e10); // in almost a year
        super.writeHeaders(req, resp, file, path);
    }
}

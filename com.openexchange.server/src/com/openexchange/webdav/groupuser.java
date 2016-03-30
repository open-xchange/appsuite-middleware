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

package com.openexchange.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.resource.Resource;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.version.Version;
import com.openexchange.webdav.xml.DataWriter;
import com.openexchange.webdav.xml.GroupUserWriter;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * groupuser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class groupuser extends PermissionServlet {

    private static final String STR_OBJECTSTATUS_OLD = "objectstatus";

    private static final String STR_CREATE = "CREATE";

    private static final String STR_DELETE = "DELETE";

    private static final long serialVersionUID = -2041907156379627530L;

    private static final String STR_OBJECTSTATUS = "object_status";

    private static String DELETED_GROUP_SQL = "SELECT id, lastmodified FROM del_groups WHERE cid=? AND lastmodified > ?";

    private static String DELETED_RESOURCE_SQL = "SELECT id, lastmodified FROM del_resource WHERE cid=? AND lastmodified > ?";

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(groupuser.class);

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_XML;
    }

    @Override
    public void doPropFind(final HttpServletRequest req, final HttpServletResponse resp) {
        LOG.debug("PROPFIND");

        String s_user = null;
        String s_group = null;
        String s_resource = null;

        Document input_doc = null;

        Date lastsync = null;

        final XMLOutputter xo = new XMLOutputter();

        Session sessionObj = null;

        OutputStream os = null;

        try {
            sessionObj = getSession(req);
            final Context ctx = ContextStorage.getInstance().getContext(sessionObj.getContextId());

            input_doc = getJDOMDocument(req);

            resp.setContentType("text/xml; charset=UTF-8");
            os = resp.getOutputStream();

            resp.setStatus(207);

            if (input_doc == null) {
                doError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
                return;
            }

            final Element el = input_doc.getRootElement();
            final Element prop = el.getChild("prop", Namespace.getNamespace("D", "DAV:"));

            final Element eUser = prop.getChild("user", XmlServlet.NS);
            if (eUser != null) {
                s_user = eUser.getText();
            }

            final Element eGroup = prop.getChild("group", XmlServlet.NS);
            if (eGroup != null) {
                s_group = eGroup.getText();
            }

            final Element eResource = prop.getChild("resource", XmlServlet.NS);
            if (eResource != null) {
                s_resource = eResource.getText();
            }

            final Element eLastsync = prop.getChild("lastsync", XmlServlet.NS);

            if (eLastsync != null) {
                try {
                    lastsync = new Date(Long.parseLong(eLastsync.getText()));
                } catch (final NumberFormatException exc) {
                    LOG.debug("lastsync is not an integer", exc);
                }
            }

            os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n").getBytes());
            os.write(("<D:multistatus xmlns:D=\"DAV:\" version=\"" + Version.getInstance().getVersionString() + "\" buildname=\"" + Version.NAME + "\">").getBytes());

            os.write(("<D:response xmlns:ox=\"http://www.open-xchange.org\">").getBytes());
            os.write(("<D:propstat><D:prop>").getBytes());

            if (s_user != null) {
                os.write(("<ox:users>").getBytes());
                os.flush();
                final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
                final GroupUserWriter groupuserwriter = new GroupUserWriter(userObj, ctx, sessionObj, new Element("user", XmlServlet.NS));

                if ("*".equals(s_user)) {
                    groupuserwriter.startWriter(true, true, lastsync, os);
                } else {
                    groupuserwriter.startWriter(s_user, os);
                }

                os.write(("</ox:users>").getBytes());
            }

            if (s_group != null) {
                writeElementGroups(s_group, lastsync, os, xo, ctx);
            }

            if (s_resource != null) {
                writeElementResources(s_resource, lastsync, os, xo, ctx);
            }

            os.write(("</D:prop></D:propstat>").getBytes());

            final Element status = new Element("status", "D", "DAV:");
            status.addContent("HTTP/1.1 200 OK");
            xo.output(status, os);

            final Element descr = new Element("responsedescription", "D", "DAV:");
            descr.addContent("HTTP/1.1 200 OK");
            xo.output(descr, os);

            os.write(("</D:response>").getBytes());
            os.write(("</D:multistatus>").getBytes());
            os.flush();
        } catch (final Exception exc) {
            LOG.error("doPropFind", exc);
            doError(resp);
        }
    }

    private void writeElementGroups(final String s_groups, final Date lastsync, final OutputStream os, final XMLOutputter xo, final Context ctx) throws IOException, OXException, SQLException {
        os.write(("<ox:groups>").getBytes());

        final GroupStorage groupstorage = GroupStorage.getInstance();
        Group[] group = null;

        if (lastsync == null) {
            if (s_groups == null || s_groups.equals("*")) {
                group = groupstorage.getGroups(true, ctx);
            } else {
                group = groupstorage.searchGroups(s_groups, true, ctx);
            }
        } else {
            group = groupstorage.listModifiedGroups(lastsync, ctx);
        }

        for (int a = 0; a < group.length; a++) {
            writeElementGroup(group[a], xo, os, false);
        }

        if (lastsync != null) {
            Connection readCon = null;
            PreparedStatement ps = null;
            try {
                readCon = DBPool.pickup(ctx);
                ps = readCon.prepareStatement(DELETED_GROUP_SQL);
                ps.setInt(1, ctx.getContextId());
                ps.setTimestamp(2, new Timestamp(lastsync.getTime()));

                final ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    final Group g = new Group();
                    g.setIdentifier(rs.getInt(1));
                    g.setLastModified(new Date(rs.getLong(2)));

                    writeElementGroup(g, xo, os, true);
                }
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (final SQLException sqle) {
                        LOG.warn("Error closing PreparedStatement");
                    }
                }
                if (readCon != null) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            }
        }

        os.write(("</ox:groups>").getBytes());
        os.flush();
    }

    private void writeElementGroup(final Group group, final XMLOutputter xo, final OutputStream os, final boolean delete) throws IOException {
        final Element e_group = new Element("group", XmlServlet.NS);

        if (delete) {
            DataWriter.addElement(STR_OBJECTSTATUS, STR_DELETE, e_group);
            // Following line is for compatibility with older OXtenders
            // element name changed 2008-07-02
            DataWriter.addElement(STR_OBJECTSTATUS_OLD, STR_DELETE, e_group);
        } else {
            DataWriter.addElement(STR_OBJECTSTATUS, STR_CREATE, e_group);
            // Following line is for compatibility with older OXtenders
            // element name changed 2008-07-02
            DataWriter.addElement(STR_OBJECTSTATUS_OLD, STR_CREATE, e_group);
        }

        DataWriter.addElement("uid", group.getIdentifier(), e_group);
        DataWriter.addElement(DataFields.LAST_MODIFIED, group.getLastModified(), e_group);

        final String displayName = group.getDisplayName();
        if (displayName != null) {
            DataWriter.addElement("displayname", displayName, e_group);
        }

        final Element e_members = new Element("members", XmlServlet.NS);
        final int[] members = group.getMember();
        if (members != null) {
            for (int a = 0; a < members.length; a++) {
                writeElementMember(members[a], e_members);
            }
        }

        e_group.addContent(e_members);

        xo.output(e_group, os);
        os.flush();
    }

    private void writeElementMember(final int member, final Element e_members) {
        DataWriter.addElement("memberuid", member, e_members);
    }

    private void writeElementResources(final String s_resources, final Date lastsync, final OutputStream os, final XMLOutputter xo, final Context ctx) throws IOException, OXException, SQLException {
        os.write(("<ox:resources>").getBytes());

        final ResourceStorage resourcestorage = ResourceStorage.getInstance();
        Resource[] resource = null;

        if (lastsync == null) {
            resource = resourcestorage.searchResources(s_resources, ctx);
        } else {
            resource = resourcestorage.listModified(lastsync, ctx);
        }

        for (int a = 0; a < resource.length; a++) {
            writeElementResource(resource[a], xo, os, false);
        }

        if (lastsync != null) {
            Connection readCon = null;
            PreparedStatement ps = null;
            try {
                readCon = DBPool.pickup(ctx);
                ps = readCon.prepareStatement(DELETED_RESOURCE_SQL);
                ps.setInt(1, ctx.getContextId());
                ps.setLong(2, lastsync.getTime());

                final ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    final Resource r = new Resource();
                    r.setIdentifier(rs.getInt(1));
                    r.setLastModified(new Date(rs.getLong(2)));

                    writeElementResource(r, xo, os, true);
                }
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (final SQLException sqle) {
                        LOG.warn("Error closing PreparedStatement");
                    }
                }
                if (readCon != null) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            }
        }

        os.write(("</ox:resources>").getBytes());
        os.flush();
    }

    private void writeElementResource(final Resource resource, final XMLOutputter xo, final OutputStream os, final boolean delete) throws IOException {
        final Element eResource = new Element("resource", XmlServlet.NS);

        if (delete) {
            DataWriter.addElement(STR_OBJECTSTATUS, STR_DELETE, eResource);
            // Following line is for compatibility with older OXtenders
            // element name changed 2008-07-02
            DataWriter.addElement(STR_OBJECTSTATUS_OLD, STR_DELETE, eResource);
        } else {
            DataWriter.addElement(STR_OBJECTSTATUS, STR_CREATE, eResource);
            // Following line is for compatibility with older OXtenders
            // element name changed 2008-07-02
            DataWriter.addElement(STR_OBJECTSTATUS_OLD, STR_CREATE, eResource);
        }

        DataWriter.addElement("uid", resource.getIdentifier(), eResource);
        DataWriter.addElement(DataFields.LAST_MODIFIED, resource.getLastModified(), eResource);

        final String displayName = resource.getDisplayName();
        if (displayName != null) {
            DataWriter.addElement("displayname", displayName, eResource);
        }

        final String mail = resource.getMail();
        if (mail != null) {
            DataWriter.addElement("email1", mail, eResource);
        }

        xo.output(eResource, os);
        os.flush();
    }

    public void doError(final HttpServletResponse resp) {
        doError(resp, 500, "Server Error");
    }

    public void doError(final HttpServletResponse resp, final int code, final String msg) {
        try {
            LOG.debug("status: {} message: {}", code, msg);

            resp.setStatus(code);
            resp.setContentType("text/html");
        } catch (final Exception exc) {
            LOG.error("doError", exc);
        }
    }

    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx).hasWebDAVXML();
    }

    @Override
    protected void decrementRequests() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }

    @Override
    protected void incrementRequests() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }
}

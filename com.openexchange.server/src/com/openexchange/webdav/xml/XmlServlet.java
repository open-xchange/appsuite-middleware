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

package com.openexchange.webdav.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.version.Version;
import com.openexchange.webdav.LastModifiedCache;
import com.openexchange.webdav.PendingInvocations;
import com.openexchange.webdav.PermissionServlet;
import com.openexchange.webdav.QueuedAction;
import com.openexchange.webdav.WebdavExceptionCode;
import com.openexchange.webdav.xml.fields.CalendarFields;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.fields.FolderChildFields;

/**
 * {@link XmlServlet} - The XML servlet.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public abstract class XmlServlet<I> extends PermissionServlet {

    private static final long serialVersionUID = -2484534357141516623L;

    public static final AttachmentBase attachmentBase = Attachments.getInstance();

    public static final int MODIFICATION_STATUS = 1000;

    public static final int OBJECT_NOT_FOUND_STATUS = 1001;

    public static final int PERMISSION_STATUS = 1002;

    public static final int CONFLICT_STATUS = 1003;

    public static final int MANDATORY_FIELD_STATUS = 1004;

    public static final int APPOINTMENT_CONFLICT_STATUS = 1006;

    public static final int BAD_REQUEST_STATUS = 1400;

    public static final int SERVER_ERROR_STATUS = 1500;

    public static final int OK_STATUS = 1200;

    public static final String MODIFICATION_EXCEPTION = "[" + MODIFICATION_STATUS
            + "] This object was modified on the server";

    public static final String OBJECT_NOT_FOUND_EXCEPTION = "[" + OBJECT_NOT_FOUND_STATUS + "] Object not found";

    public static final String PERMISSION_EXCEPTION = "[%s] No permission";

    public static final String CONFLICT_EXCEPTION = "[%s] Conflict";

    public static final String USER_INPUT_EXCEPTION = "[%s] invalid user input";

    public static final String APPOINTMENT_CONFLICT_EXCEPTION = "[" + APPOINTMENT_CONFLICT_STATUS
            + "] Appointments Conflicted";

    public static final String MANDATORY_FIELD_EXCEPTION = "[%s] Missing field";

    public static final String BAD_REQUEST_EXCEPTION = "[" + BAD_REQUEST_STATUS + "] bad xml request";

    public static final String SERVER_ERROR_EXCEPTION = "[%s] Server Error - ";

    public static final String OK = "[" + OK_STATUS + "] OK";

    public static final String INDIVIDUAL_ERROR = "[%s] [%s]";

    public static final String _contentType = "text/xml; charset=UTF-8";

    public static final String NAMESPACE = "http://www.open-xchange.org";

    public static final String PREFIX = "ox";

    protected static final String _parsePropChilds = "parsePropChilds";

    public static final Namespace NS = Namespace.getNamespace(PREFIX, NAMESPACE);

    private static final String prop = "prop";

    private static final String davUri = "DAV:";

    private static final Namespace dav = Namespace.getNamespace("D", davUri);

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XmlServlet.class);

    @Override
    public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        doPropPatch(req, resp);
    }

    @Override
    public void doPropPatch(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        LOG.debug("PROPPATCH");

        XmlPullParser parser = null;
        final PendingInvocations<I> pendingInvocations = new PendingInvocations<I>(new LinkedList<QueuedAction<I>>(), new LastModifiedCache());
        try {
            parser = new KXmlParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(req.getInputStream(), "UTF-8");

            resp.setStatus(SC_MULTISTATUS);
            resp.setContentType(_contentType);

            if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
                parser.next();
                if (isTag(parser, "multistatus", davUri)) {
                    parser.nextTag();
                    parser.require(XmlPullParser.START_TAG, davUri, "propertyupdate");

                    parsePropertyUpdate(req, resp, parser, pendingInvocations);

                    commit(resp.getOutputStream(), getSession(req), pendingInvocations);
                } else if (isTag(parser, "propertyupdate", davUri)) {
                    parsePropertyUpdate(req, resp, parser, pendingInvocations);

                    commit(resp.getOutputStream(), getSession(req), pendingInvocations);
                } else {
                    doOXError(req, resp, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, parser);
                    return;
                }
            } else {
                doOXError(req, resp, HttpServletResponse.SC_BAD_REQUEST, BAD_REQUEST_EXCEPTION, parser);
                return;
            }
        } catch (final IOException e) {
            LOG.error("doPropPatch", e);
            doOXError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, parser);
        } catch (final XmlPullParserException e) {
            LOG.error("doPropPatch", e);
            doOXError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, parser);
        } catch (final OXException e) {
            LOG.error("doPropPatch", e);
            doOXError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, parser);
        }
    }

    @Override
    public void doPropFind(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        LOG.debug("PROPFIND");

        Document input_doc = null;

        OutputStream os = null;

        Date lastsync = null;
        int folder_id = 0;
        int object_id = 0;

        boolean bModified = true;
        boolean bDeleted = false;
        boolean bList = false;

        try {
            final Session sessionObj = getSession(req);
            final Context ctx = ContextStorage.getStorageContext(sessionObj.getContextId());

            input_doc = getJDOMDocument(req);
            os = resp.getOutputStream();

            resp.setStatus(207);
            resp.setContentType(_contentType);

            boolean hasObjectId = false;
            boolean hasObjectMode = false;

            if (input_doc != null) {
                final Element el = input_doc.getRootElement();
                final Element eProp = el.getChild(prop, dav);

                if (eProp == null) {
                    doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "expected element: prop");
                    return;
                }

                final Element eLastSync = eProp.getChild("lastsync", Namespace.getNamespace(PREFIX, NAMESPACE));
                final Element eObjectMode = eProp.getChild("objectmode", Namespace.getNamespace(PREFIX, NAMESPACE));

                if (eObjectMode != null) {
                    hasObjectMode = true;
                }

                final Element eObjectId = eProp.getChild(DataFields.OBJECT_ID, Namespace
                        .getNamespace(PREFIX, NAMESPACE));
                if (eObjectId != null) {
                    hasObjectId = true;
                }

                if (hasObjectMode) {
                    try {
                        if (eLastSync != null) {
                            lastsync = new Date(Long.parseLong(eLastSync.getText()));
                        }
                    } catch (final NumberFormatException exc) {
                        System.out.println("invalid value in element lastsync");
                    }

                    final Element eFolderId = eProp.getChild(FolderChildFields.FOLDER_ID, Namespace.getNamespace(PREFIX,
                            NAMESPACE));
                    if (eFolderId != null) {
                        try {
                            folder_id = Integer.parseInt(eFolderId.getText());
                        } catch (final NumberFormatException exc) {
                            throw WebdavExceptionCode.INVALID_VALUE.create(exc, FolderChildFields.FOLDER_ID, eFolderId.getText());
                        }
                    }

                    if (eObjectMode == null) {
                        bModified = true;
                    } else {
                        bModified = false;
                        bDeleted = false;
                        bList = false;
                        final String[] value = eObjectMode.getText().trim().toUpperCase().split(",");

                        for (final String element : value) {
                            if (element.trim().equals("MODIFIED") || element.trim().equals("NEW_AND_MODIFIED")) {
                                bModified = true;
                            } else if (element.trim().equals("DELETED")) {
                                bDeleted = true;
                            } else if (element.trim().equals("LIST")) {
                                bList = true;
                            } else {
                                throw WebdavExceptionCode.INVALID_VALUE.create("objectmode", element);
                            }
                        }
                    }
                } else if (hasObjectId) {
                    try {
                        object_id = Integer.parseInt(eObjectId.getText());
                    } catch (final NumberFormatException exc) {
                        throw WebdavExceptionCode.INVALID_VALUE.create(exc, DataFields.OBJECT_ID, eObjectId.getText());
                    }

                    final Element eFolderId = eProp.getChild(FolderChildFields.FOLDER_ID, Namespace.getNamespace(PREFIX,
                            NAMESPACE));
                    if (eFolderId != null) {
                        try {
                            folder_id = Integer.parseInt(eFolderId.getText());
                        } catch (final NumberFormatException exc) {
                            throw WebdavExceptionCode.INVALID_VALUE.create(exc, FolderChildFields.FOLDER_ID, eFolderId.getText());
                        }
                    }
                } else {
                    doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "expected element: object_id or lastsync");
                    return;
                }
            }

            // SEND FIRST XML LINE
            os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n").getBytes());
            os.write(("<D:multistatus xmlns:D=\"DAV:\" version=\"" + Version.getInstance().getVersionString() + "\" buildname=\""
                    + Version.NAME + "\">").getBytes());

            if (hasObjectMode) {
                if (lastsync == null) {
                    lastsync = new Date(0);
                }
                startWriter(sessionObj, ctx, folder_id, bModified, bDeleted, bList, lastsync, resp.getOutputStream());
            } else {
                startWriter(sessionObj, ctx, object_id, folder_id, resp.getOutputStream());
            }

            os.write(("</D:multistatus>").getBytes());
            os.flush();
        } catch (final org.jdom2.JDOMException exc) {
            LOG.error("doPropFind", exc);
            doError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "XML ERROR");
        } catch (final OXException exc) {
            if (exc.isGeneric(Generic.NO_PERMISSION)) {
                doError(req, resp, HttpServletResponse.SC_FORBIDDEN, exc.getMessage());
            } else if (exc.isGeneric(Generic.CONFLICT)) {
                LOG.error("", exc);
                doError(req, resp, HttpServletResponse.SC_CONFLICT, "Conflict: " + exc.getMessage());
            } else if (OXExceptionConstants.CATEGORY_PERMISSION_DENIED.equals(exc.getCategory())) {
                doError(req, resp, HttpServletResponse.SC_FORBIDDEN, exc.getMessage());
            } else if (Category.CATEGORY_CONFLICT.equals(exc.getCategory())) {
                LOG.error("doPropFind", exc);
                doError(req, resp, HttpServletResponse.SC_CONFLICT, "Conflict: " + exc.getMessage());
            } else {
                LOG.error("doPropFind", exc);
                doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
            }
        } catch (final Exception exc) {
            LOG.error("doPropFind", exc);
            doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
        }
    }

    public void doError(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        doError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
    }

    public void doError(final HttpServletRequest req, final HttpServletResponse resp, final int code, final String msg)
            throws ServletException {
        try {
            LOG.debug("STATUS: {}: ({})", msg, code);

            resp.sendError(code, msg);
            resp.setContentType("text/html");
        } catch (final Exception exc) {
            LOG.error("doError", exc);
        }
    }

    private void doOXError(final HttpServletRequest req, final HttpServletResponse resp, final int httpErrorCode, final String description, final XmlPullParser parser) throws ServletException {
        final DataObject dataObject = new DataObject() {
            @Override
            public int getObjectID() {
                return 0;
            }
        };
        try {
            String clientId = "not found";

            while (!(parser.getEventType() == XmlPullParser.END_DOCUMENT)) {
                if (isTag(parser, "client_id", "ox")) {
                    clientId = parser.nextText();
                    break;
                } else {
                    parser.next();
                }
            }

            final OutputStream os = resp.getOutputStream();
            os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n").getBytes());
            os.write(("<D:multistatus xmlns:D=\"DAV:\" version=\"" + Version.getInstance().getVersionString() + "\" buildname=\"" + Version.NAME + "\">").getBytes());
            os.flush();

            writeResponse(dataObject, httpErrorCode, description, clientId, resp.getOutputStream(), new XMLOutputter());

            os.write(("</D:multistatus>").getBytes());
        } catch (final IOException e) {
            LOG.error("doOXError", e);
            doError(req, resp, httpErrorCode, description);
        } catch (final XmlPullParserException e) {
            LOG.error("doOXError", e);
            doError(req, resp, httpErrorCode, description);
        }
    }

    private void doOXError(final HttpServletRequest req, final HttpServletResponse resp, final int httpErrorCode, final Exception exception, final XmlPullParser parser) throws ServletException {
        doOXError(req, resp, httpErrorCode, createResponseErrorMessage(exception), parser);
    }

    private String createResponseErrorMessage(final Exception e) {
        final int descriptionCode;
        final String message;
        if (e instanceof OXException) {
            final OXException o = (OXException) e;
            if (ContactExceptionCodes.INVALID_EMAIL.equals(o)) {
                descriptionCode = 1500;
                message = o.getMessage();
            } else {
                descriptionCode = 1400;
                message = o.getMessage();
            }
        } else {
            descriptionCode = 1400;
            message = e.getMessage();
        }

        return String.format(INDIVIDUAL_ERROR, Integer.valueOf(descriptionCode), message);
    }

    protected void parsePropertyUpdate(final HttpServletRequest req, final HttpServletResponse resp,
            final XmlPullParser parser, final PendingInvocations<I> pendingInvocations) throws XmlPullParserException, IOException, OXException {

        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (isTag(parser, "set", davUri)) {
                openSet(req, resp, parser, pendingInvocations);
            } else {
                parser.next();
            }
        }

    }

    protected void openSet(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser, final PendingInvocations<I> pendingInvocations)
            throws XmlPullParserException, IOException, OXException {
        openProp(req, resp, parser, pendingInvocations);
    }

    protected void openProp(final HttpServletRequest req, final HttpServletResponse resp, final XmlPullParser parser, final PendingInvocations<I> pendingInvocations)
            throws XmlPullParserException, IOException, OXException {
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, davUri, prop);

        parsePropChilds(req, resp, parser, pendingInvocations);

        closeProp(parser);
    }

    protected void closeProp(final XmlPullParser parser) throws XmlPullParserException, IOException {
        // parser.nextTag();
        if (!isTag(parser, prop, davUri)) {
            boolean isProp = true;

            while (isProp) {
                if (endTag(parser, prop, davUri) || DataParser.isEnd(parser)) {
                    isProp = false;
                    break;
                }

                parser.next();
            }
        }

        closeSet(parser);
    }

    protected void closeSet(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, davUri, "set");
    }

    public boolean isTag(final XmlPullParser parser, final String name, final String namespace)
            throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.START_TAG && (name == null || name.equals(parser.getName()));
    }

    public boolean endTag(final XmlPullParser parser, final String name, final String namespace)
            throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.END_TAG && (name == null || name.equals(parser.getName()));
    }

    public void writeResponse(final DataObject dataobject, final int status, final String message,
            final String client_id, final OutputStream os, final XMLOutputter xo) throws IOException {
        writeResponse(dataobject, status, message, client_id, os, xo, null);
    }

    public void writeResponse(final DataObject dataobject, final int status, final String message,
            final String client_id, final OutputStream os, final XMLOutputter xo, final Appointment[] conflicts)
            throws IOException {
        LOG.debug("{}:{}", message, status);

        final Element e_response = new Element("response", dav);
        e_response.addNamespaceDeclaration(NS);

        final Element e_href = new Element("href", dav);
        e_href.addContent(Integer.toString(dataobject.getObjectID()));

        e_response.addContent(e_href);

        final Element e_propstat = new Element("propstat", dav);
        final Element e_prop = new Element(prop, dav);

        final Element e_object_id = new Element(DataFields.OBJECT_ID, NS);
        e_object_id.addContent(Integer.toString(dataobject.getObjectID()));
        e_prop.addContent(e_object_id);

        final Date lastModified = dataobject.getLastModified();
        if (lastModified != null) {
            final Element eLastModified = new Element(DataFields.LAST_MODIFIED, NS);
            eLastModified.addContent(Long.toString(lastModified.getTime()));
            e_prop.addContent(eLastModified);
        }

        if (dataobject instanceof CalendarObject) {
            final Element e_recurrence_id = new Element(CalendarFields.RECURRENCE_ID, NS);
            e_recurrence_id.addContent(Integer.toString(((CalendarObject) dataobject).getRecurrenceID()));
            e_prop.addContent(e_recurrence_id);
        }

        if (client_id != null && client_id.length() > 0) {
            final Element e_client_id = new Element("client_id", NS);
            e_client_id.addContent(DataWriter.correctCharacterData(client_id));

            e_prop.addContent(e_client_id);
        }

        e_propstat.addContent(e_prop);

        final Element e_status = new Element("status", "D", davUri);
        e_status.addContent(Integer.toString(status));

        e_propstat.addContent(e_status);

        final Element e_descr = new Element("responsedescription", "D", davUri);
        e_descr.addContent(message);

        e_propstat.addContent(e_descr);

        if (conflicts != null) {
            final Element eConflictItems = new Element("conflictitems", Namespace.getNamespace("D", davUri));
            final StringBuilder textBuilder = new StringBuilder(50);
            for (final Appointment conflict : conflicts) {
                final Element eConflictItem = new Element("conflictitem", Namespace.getNamespace("D", davUri));
                if (conflict.getTitle() == null) {
                    eConflictItem.setAttribute("subject", "", NS);
                } else {
                    eConflictItem.setAttribute("subject", conflict.getTitle(), NS);
                }

                eConflictItem.setText(textBuilder.append(conflict.getStartDate().getTime()).append(',').append(
                        conflict.getEndDate().getTime()).append(',').append(conflict.getFullTime()).toString());
                textBuilder.setLength(0);

                eConflictItems.addContent(eConflictItem);
            }

            e_propstat.addContent(eConflictItems);
        }

        e_response.addContent(e_propstat);

        xo.output(e_response, os);
        os.flush();
    }

    /**
     * This method is invoked when XML input could be successfully parsed and
     * pending actions are supposed to be performed
     *
     * @param os
     *            The output stream to write response to
     * @param session
     *            The session providing needed user data
     * @throws IOException
     *             If writing response fails
     * @throws OXException
     *             If an OX exception occurs
     */
    private final void commit(final OutputStream os, final Session session, final PendingInvocations<I> pendingInvocations) throws IOException, OXException {
        os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n").getBytes());
        os.write(("<D:multistatus xmlns:D=\"DAV:\" version=\"" + Version.getInstance().getVersionString() + "\" buildname=\""
                + Version.NAME + "\">").getBytes());
        os.flush();
        performActions(os, session, pendingInvocations);

        os.write(("</D:multistatus>").getBytes());
    }

    /**
     * Performs pending actions gathered through parsing XML input
     *
     * @param os
     *            The output stream to write response to
     * @param session
     *            The session providing needed user data
     * @param pendingInvocations queues the objects to process.
     * @throws IOException
     *             If writing response fails
     * @throws OXException
     *             If an OX exception occurs
     */
    protected abstract void performActions(final OutputStream os, final Session session, final PendingInvocations<I> pendingInvocations) throws IOException,
            OXException;

    protected abstract void parsePropChilds(HttpServletRequest req, HttpServletResponse resp, XmlPullParser parser,
            PendingInvocations<I> pendingInvocations) throws XmlPullParserException, IOException, OXException;

    protected abstract void startWriter(Session sessionObj, Context ctx, int objectId, int folderId, OutputStream os)
            throws Exception;

    protected abstract void startWriter(Session sessionObj, Context ctx, int folderId, boolean bModified,
            boolean bDelete, Date lastsync, OutputStream os) throws Exception;

    protected abstract void startWriter(Session sessionObj, Context ctx, int folderId, boolean bModified,
            boolean bDelete, boolean bList, Date lastsync, OutputStream os) throws Exception;

    public String getErrorMessage(final OXException exc, final String message) {
        return getErrorMessage(message, exc.getErrorCode());
    }

    protected String getErrorMessage(final OXException exc, final String message, final boolean withExceptionMessage) {
        final String err = getErrorMessage(message, exc.getErrorCode());
        if (!withExceptionMessage) {
            return err;
        }
        final String excMsg = exc.getMessage();
        return new StringBuilder(err.length() + excMsg.length() + 2).append(err).append(": ").append(excMsg).toString();
    }

    public String getErrorMessage(final String message, final String errorCode) {
        return String.format(message, errorCode);
    }
}

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

package com.openexchange.caldav.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link CalDAVResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CalDAVResource<T extends CalendarObject> extends CommonResource<T> {

    public static final String EXTENSION_ICS = ".ics";
    public static final String CONTENT_TYPE = "text/calendar";

    private String iCalFile = null;
    private TimeZone timeZone = null;
    protected final GroupwareCaldavFactory factory;
    protected CalDAVFolderCollection<T> parent;

    public CalDAVResource(GroupwareCaldavFactory factory, CalDAVFolderCollection<T> parent, T object, WebdavPath url) throws OXException {
        super(parent, object, url);
        this.factory = factory;
        this.parent = parent;
    }

    protected abstract String generateICal() throws OXException;

    protected abstract void move(CalDAVFolderCollection<T> target) throws OXException;

    protected String getICalFile() throws WebdavProtocolException {
        if (null == this.iCalFile) {
            try {
                this.iCalFile = this.generateICal();
            } catch (OXException e) {
                throw protocolException(e);
            }
        }
        return iCalFile;
    }

    protected ICalParser getICalParser() {
        return factory.getIcalParser();
    }

    protected ICalEmitter getICalEmitter() {
        return factory.getIcalEmitter();
    }

    protected TimeZone getTimeZone() {
        if (null == this.timeZone) {
            String timeZoneID = factory.getUser().getTimeZone();
            this.timeZone = TimeZone.getTimeZone(null != timeZoneID ? timeZoneID : "UTC");
        }
        return timeZone;
    }

    @Override
    protected String getFileExtension() {
        return EXTENSION_ICS;
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        return new Long(null != getICalFile() ? getICalFile().getBytes().length : 0);
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return CONTENT_TYPE;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        String body = this.getICalFile();
        if (LOG.isTraceEnabled()) {
            LOG.trace(body);
        }
        return null != body ? new ByteArrayInputStream(body.getBytes()) : null;
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        if (CaldavProtocol.CAL_NS.getURI().equals(namespace) && "calendar-data".equals(name)) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            property.setValue(getICalFile());
            return property;
        }
        return null;
    }

    @Override
    public CalDAVResource<T> move(WebdavPath dest, boolean noroot, boolean overwrite) throws WebdavProtocolException {
        WebdavResource destinationResource = factory.getState().resolveResource(dest);
        CommonCollection destinationCollection = destinationResource.isCollection() ?
            (CommonCollection)destinationResource : factory.getState().resolveCollection(dest.parent());
        if (false == parent.getClass().isInstance(destinationCollection)) {
            throw protocolException(HttpServletResponse.SC_FORBIDDEN);
        }
        CalDAVFolderCollection<T> targetCollection = null;
        try {
            targetCollection = (CalDAVFolderCollection<T>)destinationCollection;
        } catch (ClassCastException e) {
            throw protocolException(e, HttpServletResponse.SC_FORBIDDEN);
        }
        try {
            this.move(targetCollection);
        } catch (OXException e) {
            if (handle(e)) {
                return move(dest, noroot, overwrite);
            } else {
                throw protocolException(e);
            }
        }
        this.parent = targetCollection;
        return this;
    }

}

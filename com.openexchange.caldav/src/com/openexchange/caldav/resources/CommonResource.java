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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.Tools;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.IncorrectString;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CommonResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CommonResource<T extends CommonObject> extends AbstractResource {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonResource.class);
    private static final int MAX_RETRIES = 3;

    private int retryCount = 0;
    protected final WebdavFactory factory;
    protected final WebdavPath url;
    protected final CommonFolderCollection<T> parent;

    protected T object;
    protected boolean exists;
    protected int parentFolderID;

    /**
     * Initializes a new {@link CommonResource}.
     *
     * @param parent the parent folder collection.
     * @param object an existing groupware object represented by this resource,
     * or <code>null</code> if a placeholder resource should be created
     * @param url the resource url
     * @throws OXException
     */
    public CommonResource(CommonFolderCollection<T> parent, T object, WebdavPath url) throws OXException {
        super();
        this.parent = parent;
        this.factory = parent.factory;
        this.url = url;
        this.object = object;
        this.exists = null != object;
        this.parentFolderID = Tools.parse(parent.getFolder().getID());
    }

    // --------------------------------------------     ERROR HANDLING      ----------------------------------------------------------

    /**
     * Accepts an {@link OXException} instance signaling default status code (500).
     *
     * @param e The exception
     * @return The appropriate {@link WebdavProtocolException} instance
     */
    protected WebdavProtocolException protocolException(OXException e) {
        return protocolException(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Accepts an {@link OXException} instance signaling specified status code (<code>statusCode</code>).
     *
     * @param e The exception
     * @param statusCode The HTTP status code
     * @return The appropriate {@link WebdavProtocolException} instance
     */
    protected WebdavProtocolException protocolException(OXException e, int statusCode) {
        if (Category.CATEGORY_USER_INPUT.equals(e.getCategory())) {
            LOG.debug("{}", this.getUrl(), e);
        } else {
            LOG.error("{}", this.getUrl(), e);
        }
        return WebdavProtocolException.Code.GENERAL_ERROR.create(this.getUrl(), statusCode, e);
    }

    /**
     * Accepts a {@link Throwable} instance signaling default status code (500).
     *
     * @param e The exception
     * @return The appropriate {@link WebdavProtocolException} instance
     */
    protected WebdavProtocolException protocolException(Throwable t) {
        return protocolException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Accepts a {@link Throwable} instance signaling specified status code (<code>statusCode</code>).
     *
     * @param t The exception
     * @param statusCode The HTTP status code
     * @return The appropriate {@link WebdavProtocolException} instance
     */
    protected WebdavProtocolException protocolException(Throwable t, int statusCode) {
        LOG.error("{}", this.getUrl(), t);
        return WebdavProtocolException.Code.GENERAL_ERROR.create(this.getUrl(), statusCode, t);
    }

    /**
     * Yields a {@link Throwable} instance with default error message signaling specified status code (<code>statusCode</code>).
     *
     * @param statusCode The HTTP status code
     * @return The appropriate {@link WebdavProtocolException} instance
     */
    protected WebdavProtocolException protocolException(int statusCode) {
        return protocolException(new Throwable("A WebDAV error occurred."), statusCode);
    }

    /**
     * Handles given {@link OXException} instance and either throws an appropriate {@link WebdavProtocolException} instance or checks if a
     * retry attempt is supposed to be performed.
     *
     * @param e The exception to handle
     * @return <code>true</code> to signal that the operation should be retried; otherwise <code>false</code> if no retry should be performed
     * @throws WebdavProtocolException The appropriate {@link WebdavProtocolException} instance in case no retry is feasible
     */
    protected boolean handle(OXException e) throws WebdavProtocolException {
        boolean retry = false;
        if (Tools.isDataTruncation(e)) {
            /*
             * handle by trimming truncated fields
             */
            if (this.trimTruncatedAttributes(e)) {
                LOG.warn("{}: {} - trimming fields and trying again.", this.getUrl(), e.getMessage());
                retry = true;
            }
        } else if (Tools.isIncorrectString(e)) {
            /*
             * handle by removing problematic characters
             */
            if (replaceIncorrectStrings(e, "")) {
                LOG.warn("{}: {} - removing problematic characters and trying again.", this.getUrl(), e.getMessage());
                retry = true;
            }
        } else if (e.equalsCode(93, "APP")) { // APP-0093
            /*
             * 'Moving a recurring appointment to another folder is not supported.'
             */
            throw protocolException(e, HttpServletResponse.SC_CONFLICT);
        } else if (e.equalsCode(100, "APP")) { // APP-0100
            /*
             * 'Cannot insert appointment ABC. An appointment with the unique identifier (123) already exists.'
             */
            throw protocolException(e, HttpServletResponse.SC_CONFLICT);
        } else if (e.equalsCode(70, "APP")) { // APP-0070
            /*
             * 'You can not use the private flag in a non private folder.'
             */
            throw protocolException(e, HttpServletResponse.SC_FORBIDDEN);
        } else if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            /*
             * throw appropriate protocol exception
             */
            throw protocolException(e, HttpServletResponse.SC_FORBIDDEN);
        } else if (Category.CATEGORY_CONFLICT.equals(e.getCategory())) {
            /*
             * throw appropriate protocol exception
             */
            throw protocolException(e, HttpServletResponse.SC_CONFLICT);
        } else {
            throw protocolException(e);
        }

        if (!retry) {
            return false;
        }

        return ++retryCount <= MAX_RETRIES;
    }

    protected abstract boolean trimTruncatedAttribute(Truncated truncated);

    private boolean trimTruncatedAttributes(OXException e) {
        boolean hasTrimmed = false;
        if (null != e.getProblematics()) {
            for (ProblematicAttribute problematic : e.getProblematics()) {
                if (Truncated.class.isInstance(problematic)) {
                    hasTrimmed |= this.trimTruncatedAttribute((Truncated)problematic);
                }
            }
        }
        return hasTrimmed;
    }

    protected abstract boolean replaceIncorrectStrings(IncorrectString incorrectString, String replacement);

    private boolean replaceIncorrectStrings(OXException e, String replacement) {
        boolean hasReplaced = false;
        if (null != e.getProblematics()) {
            for (ProblematicAttribute problematic : e.getProblematics()) {
                if (IncorrectString.class.isInstance(problematic)) {
                    hasReplaced |= this.replaceIncorrectStrings((IncorrectString) problematic, replacement);
                }
            }
        }
        return hasReplaced;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    protected abstract String getFileExtension();

    protected abstract void saveObject() throws OXException;

    protected abstract void deleteObject() throws OXException;

    protected abstract void createObject() throws OXException;

    protected abstract void deserialize(InputStream inputStream) throws OXException, IOException;

    @Override
    public void create() throws WebdavProtocolException {
        if (this.exists()) {
            throw protocolException(HttpServletResponse.SC_CONFLICT);
        }
        try {
            this.createObject();
        } catch (OXException e) {
            if (handle(e)) {
                create();
            } else {
                throw protocolException(e);
            }
        }
    }

    @Override
    public void delete() throws WebdavProtocolException {
        if (false == this.exists()) {
            throw protocolException(HttpServletResponse.SC_NOT_FOUND);
        }
        try {
            this.deleteObject();
        } catch (OXException e) {
            if (handle(e)) {
                delete();
            } else {
                throw protocolException(e);
            }
        }
    }

    @Override
    public void save() throws WebdavProtocolException {
        if (false == this.exists()) {
            throw protocolException(HttpServletResponse.SC_NOT_FOUND);
        }
        try {
            this.saveObject();
        } catch (OXException e) {
            if (handle(e)) {
                save();
            } else {
                throw protocolException(e);
            }
        }
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        try {
            this.deserialize(body);
        } catch (IOException e) {
            throw protocolException(e, HttpServletResponse.SC_BAD_REQUEST);
        } catch (ConversionError e) {
            throw protocolException(e, HttpServletResponse.SC_BAD_REQUEST);
        } catch (OXException e) {
            throw protocolException(e);
        }
    }

    protected String extractResourceName() {
        return Tools.extractResourceName(this.url, getFileExtension());
    }

    @Override
    public WebdavPath getUrl() {
        return this.url;
    }

    @Override
    public void setSource(String source) throws WebdavProtocolException {
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return null != object ? object.getCreationDate() : null;
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        if (false == this.exists() || null == this.object || null == this.object.getLastModified()) {
            return "";
        } else {
            return "http://www.open-xchange.com/etags/" + object.getObjectID() + "-" + object.getLastModified().getTime();
        }
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return this.exists;
    }

    @Override
    public String getLanguage() throws WebdavProtocolException {
        return null;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null != object ? object.getLastModified() : null;
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
    }

    @Override
    public WebdavLock getLock(String token) throws WebdavProtocolException {
        return null;
    }

    @Override
    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public void unlock(String token) throws WebdavProtocolException {
    }

    @Override
    public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
        return null;
    }

    @Override
    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return null;
    }

    @Override
    public String getSource() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void lock(WebdavLock lock) throws WebdavProtocolException {
    }

    @Override
    public void setContentType(String type) throws WebdavProtocolException {
    }

    @Override
    public void setLanguage(String language) throws WebdavProtocolException {
    }

    @Override
    public void setLength(Long length) throws WebdavProtocolException {
    }

}

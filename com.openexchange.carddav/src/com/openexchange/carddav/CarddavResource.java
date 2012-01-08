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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.carddav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CarddavResource}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CarddavResource extends AbstractResource {

    private final GroupwareCarddavFactory factory;

    private final AggregatedCollection parent;

    private WebdavPath url;

    private Contact contact;

    private final OXContainerConverter converter = new OXContainerConverter((TimeZone) null, (String) null);

    private final boolean exists;

    public static final Log LOG = LogFactory.getLog(CarddavResource.class);

    public CarddavResource(AggregatedCollection parent, Contact contact, GroupwareCarddavFactory factory) {
        super();
        this.factory = factory;
        this.parent = parent;
        this.url = parent.getUrl().dup().append(contact.getObjectID() + ".vcf");
        this.contact = contact;
        this.exists = true;
    }

    public CarddavResource(AggregatedCollection parent, GroupwareCarddavFactory factory) {
        super();
        this.factory = factory;
        this.parent = parent;
        this.exists = false;
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        if (namespace.equals(CarddavProtocol.CARD_NS.getURI()) && name.equals("address-data")) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            property.setValue(generateVCard());
            return property;
        }
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
        // Empty
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
        // Empty
    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        // parse vcard
        try {
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(8192);
            for (int read = body.read(buf, 0, buflen); read > 0; read = body.read(buf, 0, buflen)) {
                baos.write(buf, 0, read);
            }
            final byte[] vcard = baos.toByteArray();
            final VersitDefinition def = Versit.getDefinition("text/x-vcard");
            VersitDefinition.Reader versitReader;
            versitReader = def.getReader(new UnsynchronizedByteArrayInputStream(vcard), "UTF-8");
            final VersitObject versitObject = def.parse(versitReader);
            Contact newContact = converter.convertContact(versitObject);
            if (exists) {
                newContact.setParentFolderID(contact.getParentFolderID());
                newContact.setContextId(contact.getContextId());
                newContact.setLastModified(contact.getLastModified());
                newContact.setObjectID(contact.getObjectID());
            } else {
                newContact.setParentFolderID(parent.getStandardFolder());
                newContact.setContextId(factory.getSession().getContextId());
            }
            contact = newContact;
        } catch (final VersitException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), 500);
        } catch (final ConverterException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), 500);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), 500);
        } finally {
            try {
                body.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {
        // Empty
    }

    @Override
    public void create() throws WebdavProtocolException {
        // save Contact
        try {
            factory.getContactInterface().insertContactObject(contact);
            this.url = parent.getUrl().dup().append(contact.getObjectID()+".vcf");
        } catch (OXException e) {
            LOG.error(e);
        }
    }

    @Override
    public void delete() throws WebdavProtocolException {
        // delete contact
        try {
            factory.getContactInterface().deleteContactObject(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified());
        } catch (OXException e) {
            LOG.error(e);
        }
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return exists;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        // generate VCard File
        String outputString = generateVCard();

        return new ByteArrayInputStream(outputString.getBytes());
    }

    /**
     * @return
     */
    private String generateVCard() {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final VersitDefinition contactDef = Versit.getDefinition("text/vcard");
        VersitDefinition.Writer versitWriter;
        try {
            versitWriter = contactDef.getWriter(byteArrayOutputStream, "UTF-8");
            final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);

            final VersitObject versitObject = oxContainerConverter.convertContact(contact, "3.0");
            contactDef.write(versitWriter, versitObject);
            versitWriter.flush();

            String outputString = new String(byteArrayOutputStream.toByteArray(), com.openexchange.java.Charsets.UTF_8);
            outputString = removeXOPENXCHANGEAttributes(outputString);
            return outputString;
        } catch (IOException e) {
            LOG.error(e);
        } catch (ConverterException e) {
            LOG.error(e);
        }
        return "";
    }

    Pattern customAttributes = Pattern.compile("^X-OPEN-XCHANGE.*?\\r?\\n", Pattern.MULTILINE);
    private String removeXOPENXCHANGEAttributes(String outputString) {
        return customAttributes.matcher(outputString).replaceAll("");
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return "text/vcard";
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return contact.getCreationDate();
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return contact.getDisplayName();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        if (!exists) {
            return "";
        }
        return "http://www.open-xchange.com/carddav/" + contact.getObjectID() + "_" + contact.getLastModified().getTime();
    }

    @Override
    public String getLanguage() throws WebdavProtocolException {
        return null;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return contact.getLastModified();
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        // generate vcard file and count bytes
        String outputString = generateVCard();
        return new Long(outputString.getBytes().length);
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
    public WebdavPath getUrl() {
        if (url == null) {
            return new WebdavPath("UNSET");
        }
        return url;
    }

    @Override
    public void lock(WebdavLock lock) throws WebdavProtocolException {

    }

    @Override
    public void save() throws WebdavProtocolException {
        try {
            factory.getContactInterface().updateContactObject(contact, parent.getStandardFolder(), contact.getLastModified());
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(getUrl(), 500);
        }
    }

    @Override
    public void setContentType(String type) throws WebdavProtocolException {

    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        contact.setDisplayName(displayName);
    }

    @Override
    public void setLanguage(String language) throws WebdavProtocolException {

    }

    @Override
    public void setLength(Long length) throws WebdavProtocolException {

    }

    @Override
    public void setSource(String source) throws WebdavProtocolException {

    }

    @Override
    public void unlock(String token) throws WebdavProtocolException {

    }

}

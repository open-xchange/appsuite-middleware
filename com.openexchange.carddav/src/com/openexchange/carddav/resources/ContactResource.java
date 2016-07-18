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

package com.openexchange.carddav.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.resources.CommonResource;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ContactResource2}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactResource extends CommonResource<Contact> {

    /**
     * The file extension used for vCard resources.
     */
    public static final String EXTENSION_VCF = ".vcf";

    /**
     * The content type used for vCard resources.
     */
    public static final String CONTENT_TYPE = "text/vcard; charset=utf-8";

    private static final int MAX_RETRIES = 3;

    private final GroupwareCarddavFactory factory;
    private final CardDAVCollection parent;
    private VCardImport vCardImport;
    private VCardExport vCardExport;

    /**
     * Initializes a new {@link ContactResource}.
     *
     * @param factory The CardDAV factory
     * @param parent The parent folder collection
     * @param object An existing groupware object represented by this resource, or <code>null</code> if a placeholder resource should be created
     * @param url The resource url
     */
    public ContactResource(GroupwareCarddavFactory factory, CardDAVCollection parent, Contact object, WebdavPath url) throws OXException {
        super(parent, object, url);
        this.object = object;
        this.parent = parent;
        this.factory = factory;
    }

    /**
     * Creates a new contact resource from a vCard import.
     *
     * @param factory The CardDAV factory
     * @param parent The parent folder collection
     * @param url The target resource URL
     * @param vCardImport The vCard import to apply
     * @return The new contact resource
     */
    static ContactResource fromImport(GroupwareCarddavFactory factory, CardDAVCollection parent, WebdavPath url, VCardImport vCardImport) throws OXException {
        ContactResource contactResource = new ContactResource(factory, parent, null, url);
        contactResource.vCardImport = vCardImport;
        return contactResource;
    }

    @Override
    protected String getFileExtension() {
        return EXTENSION_VCF;
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return CONTENT_TYPE;
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        if (exists()) {
            VCardExport vCardResource = getVCardResource(false);
            if (null != vCardResource && null != vCardResource.getVCard()) {
                return Long.valueOf(vCardResource.getVCard().getLength());
            }
        }
        return 0L;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        if (exists()) {
            VCardExport vCardResource = getVCardResource(true);
            if (null != vCardResource) {
                try {
                    return vCardResource.getClosingStream();
                } catch (OXException e) {
                    throw protocolException(e);
                }
            }
        }
        throw protocolException(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public void save() throws WebdavProtocolException {
        IFileHolder vCardFileHolder = null;
        String vCardID = null;
        String previousVCardID = null;
        boolean saved = false;
        try {
            if (false == exists()) {
                throw protocolException(HttpServletResponse.SC_CONFLICT);
            } else if (null == vCardImport || null == vCardImport.getContact()) {
                throw protocolException(HttpServletResponse.SC_NOT_FOUND);
            }
            /*
             * store original vCard if possible
             */
            ContactService contactService = factory.requireService(ContactService.class);
            previousVCardID = object.getVCardId();
            Contact contact = vCardImport.getContact();
            vCardFileHolder = vCardImport.getVCard();
            if (null != vCardFileHolder) {
                int contextId = factory.getSession().getContextId();
                VCardStorageService vCardStorageService = factory.getVCardStorageService(contextId);
                if (vCardStorageService != null) {
                    vCardID = vCardStorageService.saveVCard(vCardFileHolder.getStream(), contextId);
                    LOG.debug("{}: saved original vCard in '{}'.", getUrl(), vCardID);
                    contact.setVCardId(vCardID);
                }
            }
            /*
             * update contact, trying again in case of recoverable errors
             */
            for (int i = 0; i < MAX_RETRIES && false == saved; i++) {
                try {
                    contactService.updateContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), Integer.toString(contact.getObjectID()), contact, contact.getLastModified());
                    LOG.debug("{}: saved.", getUrl());
                    saved = true;
                } catch (OXException e) {
                    if (false == handle(e)) {
                        break;
                    }
                }
            }
            /*
             * process attachments
             */
            handleAttachments(object, contact);
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            Streams.close(vCardFileHolder);
            closeVCardImport();
            if (saved) {
                deleteVCard(previousVCardID);
            } else if (null != vCardID) {
                deleteVCard(vCardID);
            }
        }
    }

    @Override
    public void delete() throws WebdavProtocolException {
        boolean deleted = false;
        String vCardID = null != object ? object.getVCardId() : null;
        try {
            if (false == exists()) {
                throw protocolException(HttpServletResponse.SC_NOT_FOUND);
            }
            /*
             * delete contact, trying again in case of recoverable errors
             */
            for (int i = 0; i < MAX_RETRIES && false == deleted; i++) {
                try {
                    factory.requireService(ContactService.class).deleteContact(factory.getSession(),
                        Integer.toString(object.getParentFolderID()), Integer.toString(object.getObjectID()), object.getLastModified());
                    LOG.debug("{}: deleted.", getUrl());
                    deleted = true;
                    object = null;
                } catch (OXException e) {
                    if (false == handle(e)) {
                        break;
                    }
                }
            }
        } finally {
            if (null != vCardID && deleted) {
                deleteVCard(vCardID);
            }
        }
    }

    @Override
    public void create() throws WebdavProtocolException {
        String vCardID = null;
        IFileHolder vCardFileHolder = null;
        boolean created = false;
        try {
            if (exists()) {
                throw protocolException(HttpServletResponse.SC_CONFLICT);
            } else if (null == vCardImport || null == vCardImport.getContact()) {
                throw protocolException(HttpServletResponse.SC_NOT_FOUND);
            }
            /*
             * import vCard as new contact
             */
            ContactService contactService = factory.requireService(ContactService.class);
            Contact contact = vCardImport.getContact();
            if (null != url) {
                String extractedUID = Tools.extractUID(url);
                if (null != extractedUID && false == extractedUID.equals(contact.getUid())) {
                    /*
                     * Always extract the UID from the URL; the Addressbook client in MacOS 10.6 uses different UIDs in
                     * the WebDAV path and the UID field in the vCard, so we need to store this UID in the contact
                     * resource, too, to recognize later updates on the resource.
                     */
                    LOG.debug("{}: Storing WebDAV resource name in filename.", getUrl());
                    contact.setFilename(extractedUID);
                }
            }
            /*
             * set initial parent folder to the default contacts folder in case of an iOS client
             */
            contact.setContextId(factory.getSession().getContextId());
            String parentFolderID = isIOSClient() ? factory.getState().getDefaultFolder().getID() : String.valueOf(this.parentFolderID);
            if (contact.getMarkAsDistribtuionlist()) {
                /*
                 * insert & delete not supported contact group (next sync cleans up the client)
                 */
                try {
                    LOG.warn("{}: contact groups not supported, performing immediate deletion of this resource.", this.getUrl());
                    contact.removeDistributionLists();
                    contact.removeNumberOfDistributionLists();
                    contactService.createContact(factory.getSession(), parentFolderID, contact);
                    contactService.deleteContact(factory.getSession(), parentFolderID, Integer.toString(contact.getObjectID()), contact.getLastModified());
                } catch (OXException e) {
                    throw protocolException(e);
                }
                return;
            }
            /*
             * store original vCard if possible
             */
            vCardFileHolder = vCardImport.getVCard();
            if (null != vCardFileHolder) {
                VCardStorageService vCardStorageService = factory.getVCardStorageService(factory.getSession().getContextId());
                if (null != vCardStorageService) {
                    vCardID = vCardStorageService.saveVCard(vCardFileHolder.getStream(), factory.getSession().getContextId());
                    LOG.debug("{}: saved original vCard in '{}'.", getUrl(), vCardID);
                    contact.setVCardId(vCardID);
                }
            }
            /*
             * save contact, trying again in case of recoverable errors
             */
            object = contact;
            for (int i = 0; i <= MAX_RETRIES && false == created; i++) {
                try {
                    contactService.createContact(factory.getSession(), parentFolderID, object);
                    LOG.debug("{}: created.", getUrl());
                    created = true;
                } catch (OXException e) {
                    if (false == handle(e)) {
                        break;
                    }
                }
            }
            /*
             * process indicated attachments
             */
            handleAttachments(null, object);
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            Streams.close(vCardFileHolder);
            closeVCardImport();
            if (null != vCardID && false == created) {
                deleteVCard(vCardID);
            }
        }
    }

    @Override
    protected void deserialize(InputStream inputStream) throws OXException, IOException {
        VCardService vCardService = factory.requireService(VCardService.class);
        VCardParameters parameters = vCardService.createParameters(factory.getSession()).setKeepOriginalVCard(parent.isStoreOriginalVCard())
            .setImportAttachments(true).setRemoveAttachmentsFromKeptVCard(true);
        if (false == exists()) {
            /*
             * import vCard as new contact
             */
            vCardImport = vCardService.importVCard(inputStream, null, parameters);
        } else {
            /*
             * import vCard and merge with existing contact, ensuring that some important properties don't change
             */
            String uid = object.getUid();
            int parentFolderID = object.getParentFolderID();
            int contextID = object.getContextId();
            Date lastModified = object.getLastModified();
            int objectID = object.getObjectID();
            String vCardID = object.getVCardId();
            vCardImport = factory.requireService(VCardService.class).importVCard(inputStream, object, parameters);
            vCardImport.getContact().setUid(uid);
            vCardImport.getContact().setParentFolderID(parentFolderID);;
            vCardImport.getContact().setContextId(contextID);;
            vCardImport.getContact().setLastModified(lastModified);
            vCardImport.getContact().setObjectID(objectID);
            vCardImport.getContact().setVCardId(vCardID);
        }
        if (null == vCardImport || null == vCardImport.getContact()) {
            throw new PreconditionException(DAVProtocol.CARD_NS.getURI(), "valid-address-data", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    protected WebdavProperty internalGetProperty(WebdavProperty property) throws WebdavProtocolException {
        if (DAVProtocol.CARD_NS.getURI().equals(property.getNamespace()) && "address-data".equals(property.getName())) {
            if (false == exists()) {
                return null;
            }
            Set<String> propertyNames = new HashSet<String>();
            List<Element> children = property.getChildren();
            if (null != children && 0 < children.size()) {
                propertyNames = new HashSet<String>();
                for (Element childElement : children) {
                    if (DAVProtocol.CARD_NS.equals(childElement.getNamespace()) && "prop".equals(childElement.getName())) {
                        String name = childElement.getAttributeValue("name");
                        if (null != name) {
                            propertyNames.add(name);
                        }
                    }
                }
            }
            WebdavProperty result = new WebdavProperty(property.getNamespace(), property.getName());
            if (null != propertyNames && 0 < propertyNames.size()) {
                try (VCardExport vCardExport = generateVCardResource(propertyNames); InputStream inputStream = vCardExport.getClosingStream()) {
                    result.setValue(Streams.stream2string(inputStream, Charsets.UTF_8_NAME));
                } catch (IOException | OXException e) {
                    throw protocolException(e);
                }
            } else {
                try (InputStream inputStream = getBody()) {
                    result.setValue(Streams.stream2string(inputStream, Charsets.UTF_8_NAME));
                } catch (IOException e) {
                    throw protocolException(e);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Silently closes the body file holder if set.
     */
    private void closeVCardImport() {
        if (null != vCardImport) {
            Streams.close(vCardImport);
            vCardImport = null;
        }
    }

    /**
     * Deletes a vCard silently.
     *
     * @param vCardID The identifier of the vCard to delete, or <code>null</code> to do nothing
     */
    private void deleteVCard(String vCardID) {
        if (null != vCardID) {
            int contextId = factory.getSession().getContextId();
            VCardStorageService vCardStorage = factory.getVCardStorageService(contextId);
            if (null != vCardStorage) {
                try {
                    vCardStorage.deleteVCard(vCardID, contextId);
                } catch (OXException e) {
                    if ("FLS-0017".equals(e.getErrorCode())) {
                        LOG.debug("vCard file with id {} in context {} no longer found in storage.", vCardID, contextId, e);
                    } else {
                        LOG.warn("Error while deleting vCard with id {} in context {} from storage.", vCardID, contextId, e);
                    }
                }
            }
        }
    }

    /**
     * Gets a value indicating whether the request's user agent is assumed to represent an iOS client or not.
     *
     * @return <code>true</code> if the request originates in an iOS client, <code>false</code>, otherwise
     */
    private boolean isIOSClient() {
        String userAgent = (String) factory.getSession().getParameter("user-agent");
        return false == Strings.isEmpty(userAgent) &&
            Pattern.matches(".*iOS.*dataaccessd.*", userAgent) && false == userAgent.contains("Android");
    }

    /**
     * Tries to handle an exception.
     *
     * @param e the exception to handle
     * @return <code>true</code>, if the operation should be retried, <code>false</code>, otherwise.
     */
    private boolean handle(OXException e) throws WebdavProtocolException {
        LOG.debug("Trying to handle exception: {}", e.getMessage(), e);
        if (Tools.isImageProblem(e)) {
            /*
             * image problem, handle by create without image
             */
            LOG.warn("{}: {} - removing image and trying again.", getUrl(), e.getMessage());
            object.removeImage1();
            return true;
        } else if (Tools.isDataTruncation(e)) {
            /*
             * handle by trimming truncated fields
             */
            if (trimTruncatedAttributes(e)) {
                LOG.warn("{}: {} - trimming fields and trying again.", getUrl(), e.getMessage());
                return true;
            }
        } else if (Tools.isIncorrectString(e)) {
            /*
             * handle by removing incorrect characters
             */
            if (replaceIncorrectStrings(e, "")) {
                LOG.warn("{}: {} - removing incorrect characters and trying again.", getUrl(), e.getMessage());
                return true;
            }
        } else if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            /*
             * handle by overriding sync-token
             */
            LOG.debug("{}: {}", this.getUrl(), e.getMessage());
            LOG.debug("{}: overriding next sync token for client recovery.", this.getUrl());
            this.factory.overrideNextSyncToken();
        } else if (Category.CATEGORY_CONFLICT.equals(e.getCategory())) {
            throw protocolException(e, HttpServletResponse.SC_CONFLICT);
        } else if (Category.CATEGORY_SERVICE_DOWN.equals(e.getCategory())) {
            /*
             * throw appropriate protocol exception
             */
            throw protocolException(e, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            throw protocolException(e);
        }
        return false;
    }

    private boolean trimTruncatedAttributes(OXException e) {
        try {
            return MappedTruncation.truncate(e.getProblematics(), object);
        } catch (OXException x) {
            LOG.warn("{}: error trying to handle truncated attributes", getUrl(), x);
            return false;
        }
    }

    private boolean replaceIncorrectStrings(OXException e, String replacement) {
        try {
            return MappedIncorrectString.replace(e.getProblematics(), object, replacement);
        } catch (OXException x) {
            LOG.warn("{}: error trying to handle truncated attributes", getUrl(), x);
            return false;
        }
    }

    private VCardExport getVCardResource(boolean reset) throws WebdavProtocolException {
        VCardExport vCardResource = this.vCardExport;
        if (null == vCardResource) {
            try {
                vCardResource = generateVCardResource(null);
            } catch (OXException e) {
                throw protocolException(e);
            }
        }
        this.vCardExport = reset ? null : vCardResource;
        return vCardResource;
    }

    private VCardExport generateVCardResource(Set<String> propertyNames) throws OXException {
        /*
         * load required contact data from storage
         */
        Contact contact = factory.getContactService().getContact(
            factory.getSession(), String.valueOf(object.getParentFolderID()), String.valueOf(object.getObjectID()));
        /*
         * retrieve an original vCard if available
         */
        InputStream originalVCard = null;
        try {
            String vCardID = object.getVCardId();
            if (null != vCardID) {
                int contextId = factory.getSession().getContextId();
                VCardStorageService vCardStorage = factory.getVCardStorageService(factory.getSession().getContextId());
                if (null != vCardStorage) {
                    try {
                        originalVCard = vCardStorage.getVCard(vCardID, contextId);
                    } catch (OXException e) {
                        LOG.warn("Error retrieving vCard with id {} in context {} from storage.", vCardID, contextId, e);
                    }
                }
            }
            /*
             * export current contact data & return resulting vCard stream
             */
            applyAttachments(contact);
            VCardService vCardService = factory.requireService(VCardService.class);
            VCardParameters parameters = vCardService.createParameters(factory.getSession());
            if (null != propertyNames && 0 < propertyNames.size()) {
                parameters.setPropertyNames(propertyNames);
            }
            return vCardService.exportContact(contact, originalVCard, parameters);
        } finally {
            Streams.close(originalVCard);
        }
    }

}

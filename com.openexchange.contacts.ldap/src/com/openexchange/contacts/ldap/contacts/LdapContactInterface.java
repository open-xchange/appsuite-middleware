package com.openexchange.contacts.ldap.contacts;

import java.util.ArrayList;
import java.util.Date;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.contact.LdapServer;
import com.openexchange.contacts.ldap.exceptions.LdapException;
import com.openexchange.contacts.ldap.exceptions.LdapException.Code;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;


public class LdapContactInterface implements ContactInterface {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapContactInterface.class);
    
    private final String context;
    
    private Session session;
    
    public LdapContactInterface(final String context) {
        this.context = context;
    }
    
    
    public void deleteContactObject(int oid, int fuid, Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
        throw new LdapException(Code.DELETE_NOT_POSSIBLE);
    }

    public SearchIterator<ContactObject> getContactsByExtendedSearch(ContactSearchObject searchobject, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called getContactsByExtendedSearch");
        return null;
    }

    // The all request...
    public SearchIterator<ContactObject> getContactsInFolder(int folderId, int from, int to, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called getContactsInFolder");
        final ArrayList<ContactObject> arrayList = new ArrayList<ContactObject>();
        arrayList.add(getDummyContact(folderId));
        final SearchIterator<ContactObject> searchIterator = new ArrayIterator<ContactObject>(arrayList.toArray(new ContactObject[arrayList.size()]));
        return searchIterator;
    }


    public SearchIterator<ContactObject> getDeletedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
        LOG.info("Called getDeletedContactsInFolder");
        return null;
    }

    public int getFolderId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public LdapServer getLdapServer() {
        final LdapServer ldapServer = new LdapServer();
        ldapServer.setContext(context);
        return ldapServer;
    }

    public SearchIterator<ContactObject> getModifiedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
        LOG.info("Called getModifiedContactsInFolder");
        return new ArrayIterator<ContactObject>(new ContactObject[0]);
    }

    public int getNumberOfContacts(int folderId) throws OXException {
        LOG.info("Called getNumberOfContacts");
        return 0;
    }

    public ContactObject getObjectById(int objectId, int inFolder) throws OXException {
        LOG.info("Called getObjectById");
        return null;
    }

    public SearchIterator<ContactObject> getObjectsById(int[][] objectIdAndInFolder, int[] cols) throws OXException {
        LOG.info("Called getObjectsById");
        final ArrayList<ContactObject> arrayList = new ArrayList<ContactObject>();
        for (final int[] object : objectIdAndInFolder) {
            final int object_id = object[0];
            final int folder_id = object[1];
            if (1 == object_id) {
                arrayList.add(getDummyContact(folder_id));
            }
        }
        return new ArrayIterator<ContactObject>(arrayList.toArray(new ContactObject[arrayList.size()]));
    }

    public ContactObject getUserById(int userId) throws OXException {
        LOG.info("Called getUserById");
        return null;
    }

    public void insertContactObject(ContactObject co) throws OXException {
        // TODO Auto-generated method stub

    }

    public SearchIterator<ContactObject> searchContacts(String searchpattern, int folderId, int orderBy, String orderDir, int[] cols) throws OXException {
        LOG.info("Called searchContacts");
        return null;
    }

    public void setSession(Session s) throws OXException {
        this.session = session;
    }

    public void updateContactObject(ContactObject co, int fid, Date d) throws OXException, OXConcurrentModificationException, ContactException {
        LOG.info("Called updateContactObject");
    }


    private ContactObject getDummyContact(int folderId) {
        final ContactObject contactObject = new ContactObject();
        contactObject.setContextId(111);
        contactObject.setMiddleName("test1");
        contactObject.setParentFolderID(folderId);
        contactObject.setObjectID(1);
        contactObject.setInternalUserId(1);
        contactObject.setNickname("test1");
        contactObject.setDisplayName("test1");
        contactObject.setGivenName("test1");
        // TODO: Put Admin user here
        contactObject.setCreatedBy(2);
        contactObject.setNumberOfAttachments(0);
        contactObject.setLastModified(new Date(System.currentTimeMillis()));
        return contactObject;
    }

}

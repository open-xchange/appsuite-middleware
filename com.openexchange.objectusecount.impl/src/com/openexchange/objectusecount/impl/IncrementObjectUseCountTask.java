package com.openexchange.objectusecount.impl;

import java.util.Collection;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.objectusecount.BatchIncrementArguments;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * {@link IncrementObjectUseCountTask} - The task to execute in order to increment use-counts according to specified {@link IncrementArguments arguments}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
class IncrementObjectUseCountTask extends AbstractTask<Void> {

    /** The fields to fill in found contacts */
    private static final ContactField[] FIELDS = new ContactField[] { ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.LAST_MODIFIED };

    private final IncrementArguments arguments;
    private final Session session;
    private final ObjectUseCountServiceImpl serviceImpl;

    /**
     * Initializes a new {@link IncrementObjectUseCountTask}.
     */
    IncrementObjectUseCountTask(IncrementArguments arguments, Session session, ObjectUseCountServiceImpl serviceImpl) {
        super();
        this.arguments = arguments;
        this.session = session;
        this.serviceImpl = serviceImpl;
    }

    @Override
    public Void call() throws OXException {
        int userId = arguments.getUserId();
        if (userId > 0) {
            // By user identifier
            UserService userService = serviceImpl.services.getService(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class);
            }
            User user = userService.getUser(userId, session.getContextId());
            TIntIntMap object2folder = new TIntIntHashMap(2);
            object2folder.put(user.getContactId(), FolderObject.SYSTEM_LDAP_FOLDER_ID);
            serviceImpl.incrementObjectUseCount(object2folder, session.getUserId(), session.getContextId(), arguments.getCon());
        }

        Collection<String> mailAddresses = arguments.getMailAddresses();
        if (null != mailAddresses && !mailAddresses.isEmpty()) {
            // By mail address(es)
            ContactService contactService = serviceImpl.services.getService(ContactService.class);
            if (null == contactService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContactService.class);
            }

            TIntIntMap object2folder = new TIntIntHashMap(mailAddresses.size());
            for (String mail : mailAddresses) {
                ContactSearchObject search = new ContactSearchObject();
                search.setAllEmail(mail);
                search.setOrSearch(true);
                SearchIterator<Contact> it = contactService.searchContacts(session, search, FIELDS);
                try {
                    while (it.hasNext()) {
                        Contact c = it.next();
                        object2folder.put(c.getObjectID(), c.getParentFolderID());
                    }
                } finally {
                    SearchIterators.close(it);
                }
            }
            serviceImpl.incrementObjectUseCount(object2folder, session.getUserId(), session.getContextId(), arguments.getCon());
        }

        if (arguments instanceof BatchIncrementArguments) {
            BatchIncrementArguments batchArguments = (BatchIncrementArguments) arguments;
            serviceImpl.batchIncrementObjectUseCount(batchArguments.getCounts(), session.getUserId(), session.getContextId(), arguments.getCon());
        } else {
            int objectId = arguments.getObjectId();
            int folderId = arguments.getFolderId();
            if (objectId > 0 && folderId > 0) {
                // By object/folder identifier
                TIntIntMap object2folder = new TIntIntHashMap(2);
                object2folder.put(objectId, folderId);
                serviceImpl.incrementObjectUseCount(object2folder, session.getUserId(), session.getContextId(), arguments.getCon());
            }
        }

        return null;
    }
}
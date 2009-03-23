
package com.openexchange.publish.microformats.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.publish.Path;
import com.openexchange.publish.microformats.ItemLoader;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

public class ContactLoader implements ItemLoader<ContactObject> {

    private static final Log LOG = LogFactory.getLog(ContactLoader.class);

    public List<ContactObject> load(int folderId, Path path) {
        Session session = createSession(path);
        Context ctx = loadContext(path);
        if (ctx == null) {
            return Collections.emptyList();
        }
        ContactInterface contacts = new RdbContactSQLInterface(session, ctx);
        try {
            SearchIterator<ContactObject> inFolder = contacts.getModifiedContactsInFolder(folderId, ContactObject.ALL_COLUMNS, new Date(0));
            List<ContactObject> allContacts = new ArrayList<ContactObject>();
            while(inFolder.hasNext()) {
                allContacts.add(inFolder.next());
            }
            return allContacts;
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        } catch (SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Session createSession(Path path) {
        return new PathSession(path);
    }

    private Context loadContext(Path path) {
        try {
            return Contexts.load(path.getContextId());
        } catch (ContextException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

}


package com.openexchange.publish.microformats.internal;

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
import com.openexchange.tagging.Tagged;

public class ContactLoader implements ItemLoader<ContactObject> {

    private static final Log LOG = LogFactory.getLog(ContactLoader.class);

    public ContactObject load(Tagged tagged, Path path) {
        Session session = createSession(path);
        Context ctx = loadContext(path);
        if (ctx == null) {
            return null;
        }
        ContactInterface contacts = new RdbContactSQLInterface(session, ctx);

        try {
            return contacts.getObjectById(tagged.getObjectId(), tagged.getFolderId());
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            return null;
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


package com.openexchange.groupware.attach.actions;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.FireAttachedEventAction;
import com.openexchange.tx.UndoableAction;

public class FireAttachedEventActionTest extends AbstractAttachmentEventActionTest {

    private final MockAttachmentListener listener = new MockAttachmentListener();

    private MockDBProvider provider = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        provider = new MockDBProvider(new DBPoolProvider());
    }

    @Override
    protected UndoableAction getAction() throws Exception {
        final FireAttachedEventAction fireAttached = new FireAttachedEventAction();
        fireAttached.setAttachments(getAttachments());
        fireAttached.setSession(getSession());
        fireAttached.setContext(getContext());
        fireAttached.setUser(getUser());
        fireAttached.setUserConfiguration(null);
        fireAttached.setProvider(provider);
        final List<AttachmentListener> listeners = new ArrayList<AttachmentListener>();
        listeners.add(listener);
        fireAttached.setAttachmentListeners(listeners);
        fireAttached.setSource(getAttachmentBase());
        return fireAttached;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        final List<AttachmentMetadata> m = listener.getAttached();
        final Map<Integer, AttachmentMetadata> attachmentMap = new HashMap<Integer, AttachmentMetadata>();
        final Set<AttachmentMetadata> attachmentSet = new HashSet<AttachmentMetadata>();

        for (final AttachmentMetadata att : getAttachments()) {
            attachmentMap.put(att.getId(), att);
            attachmentSet.add(att);
        }

        for (final AttachmentMetadata attached : m) {
            final AttachmentMetadata orig = attachmentMap.get(attached.getId());
            assertEquals(orig, attached);
            assertTrue(attachmentSet.remove(attached));
        }
        assertTrue(attachmentSet.isEmpty());

        listener.clear();

        assertTrue(provider.getStatus(), provider.allOK());
        assertTrue(provider.getStatus(), provider.called());
    }

    @Override
    protected void verifyUndone() throws Exception {
        final Set<Integer> ids = new HashSet<Integer>();
        for (final AttachmentMetadata m : getAttachments()) {
            ids.add(m.getId());
        }
        for (final int id : listener.getDetached()) {
            assertTrue(ids.remove(id));
        }
        assertTrue(ids.isEmpty());
        assertTrue(provider.allOK());
        assertTrue(provider.called());
    }

}

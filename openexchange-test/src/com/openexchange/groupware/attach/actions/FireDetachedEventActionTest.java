package com.openexchange.groupware.attach.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.actions.AbstractAttachmentEventActionTest.MockAttachmentListener;
import com.openexchange.groupware.attach.impl.FireAttachedEventAction;
import com.openexchange.groupware.attach.impl.FireDetachedEventAction;
import com.openexchange.groupware.tx.AbstractActionTest;
import com.openexchange.groupware.tx.UndoableAction;

public class FireDetachedEventActionTest extends AbstractAttachmentEventActionTest {

	private MockAttachmentListener listener = new MockAttachmentListener();
	private MockDBProvider provider = new MockDBProvider();
	
	@Override
	protected UndoableAction getAction() throws Exception {
		FireDetachedEventAction fireDetached = new FireDetachedEventAction();
		fireDetached.setAttachments(getAttachments());
		fireDetached.setContext(getContext());
		fireDetached.setUser(getUser());
		fireDetached.setUserConfiguration(null);
		fireDetached.setProvider(provider);
		List<AttachmentListener> listeners = new ArrayList<AttachmentListener>();
		listeners.add(listener);
		fireDetached.setAttachmentListeners(listeners);
		fireDetached.setSource(getAttachmentBase());
		return fireDetached;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		Set<Integer> ids = new HashSet<Integer>();
		for(AttachmentMetadata m : getAttachments()) {
			ids.add(m.getId());
		}
		for(int id : listener.getDetached()) {
			assertTrue(ids.remove(id));
		}
		assertTrue(ids.isEmpty());
		listener.clear();
		assertTrue(provider.getStatus(), provider.allOK());
		assertTrue(provider.getStatus(), provider.called());
	}

	@Override
	protected void verifyUndone() throws Exception {
		
		List<AttachmentMetadata> m = listener.getAttached();
		Map<Integer, AttachmentMetadata> attachmentMap = new HashMap<Integer, AttachmentMetadata>();
		Set<AttachmentMetadata> attachmentSet = new HashSet<AttachmentMetadata>();
		
		for(AttachmentMetadata att : getAttachments()) {
			attachmentMap.put(att.getId(),att);
			attachmentSet.add(att);
		}
		
		for(AttachmentMetadata attached : m) {
			AttachmentMetadata orig = attachmentMap.get(attached.getId());
			assertEquals(orig, attached);
			assertTrue(attachmentSet.remove(attached));
		}
		assertTrue(attachmentSet.isEmpty());
		
		listener.clear();
		assertTrue(provider.allOK());
		assertTrue(provider.called());
	}	

}

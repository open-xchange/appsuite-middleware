package com.openexchange.groupware.attach.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;

public abstract class AbstractAttachmentEventActionTest extends
		AbstractAttachmentActionTest {
	
	
	protected static final class MockAttachmentListener implements AttachmentListener {
		
		List<AttachmentMetadata> attached = new ArrayList<AttachmentMetadata>();
		Set<Integer> detached = new HashSet<Integer>();
		
		
		public void attached(AttachmentEvent e) throws Exception{
			attached.add(e.getAttachment());
		}
		
		public void detached(AttachmentEvent e) throws Exception{
			for(int id : e.getDetached()) {
				detached.add(id);
			}
		}
		
		public List<AttachmentMetadata> getAttached(){
			return attached;
		}
		
		public Set<Integer> getDetached(){
			return detached;
		}
		
		public void clear(){
			attached.clear();
			detached.clear();
		}
	}
}

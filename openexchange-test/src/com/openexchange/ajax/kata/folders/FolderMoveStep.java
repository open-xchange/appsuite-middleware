package com.openexchange.ajax.kata.folders;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.folder.actions.UpdateResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * @author <a href="mailto:karsten.will@open-xchange">Karsten Will</a>
 *
 */
public class FolderMoveStep extends NeedExistingStep<FolderObject> {
	
	private int destinationFolder;

	/**
	 * @param name
	 * @param expectedError
	 */
	public FolderMoveStep(int destinationFolder, String name, String expectedError) {
        super(name, expectedError);
        this.destinationFolder = destinationFolder;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.ajax.kata.Step#cleanUp()
	 */
	public void cleanUp() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.openexchange.ajax.kata.Step#perform(com.openexchange.ajax.framework.AJAXClient)
	 */
	public void perform(AJAXClient client) throws Exception {
this.client = client;
        
        FolderObject entry = new FolderObject();
        assumeIdentity(entry);
        entry.setParentFolderID(destinationFolder);
        
        UpdateRequest updateRequest = new UpdateRequest(entry, false);
        CommonInsertResponse updateResponse = execute(updateRequest);
        
        if(!updateResponse.hasError()) {
            entry.setLastModified(updateResponse.getTimestamp());
            rememberIdentityValues(entry);
        }
        checkError(updateResponse);
		
	}

}

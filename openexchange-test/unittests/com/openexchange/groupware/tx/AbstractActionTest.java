package com.openexchange.groupware.tx;

import junit.framework.TestCase;

import com.openexchange.groupware.tx.UndoableAction;

public abstract class AbstractActionTest extends TestCase {
	public void testAction() throws Exception {
		UndoableAction action = getAction();
		action.perform();
		verifyPerformed();
		action.undo();
		verifyUndone();
	}

	protected abstract void verifyPerformed() throws Exception;
	protected abstract void verifyUndone() throws Exception;
	protected abstract UndoableAction getAction() throws Exception;
	
}

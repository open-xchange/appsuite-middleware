package com.openexchange.groupware.tx;

import com.openexchange.tx.UndoableAction;
import junit.framework.TestCase;

public abstract class AbstractActionTest extends TestCase {
	public void testAction() throws Exception {
		final UndoableAction action = getAction();
		action.perform();
		verifyPerformed();
		action.undo();
		verifyUndone();
	}

	protected abstract void verifyPerformed() throws Exception;
	protected abstract void verifyUndone() throws Exception;
	protected abstract UndoableAction getAction() throws Exception;
	
}

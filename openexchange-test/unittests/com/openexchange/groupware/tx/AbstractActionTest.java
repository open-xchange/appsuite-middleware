
package com.openexchange.groupware.tx;

import org.junit.Test;
import com.openexchange.tx.UndoableAction;

public abstract class AbstractActionTest {

    @Test
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

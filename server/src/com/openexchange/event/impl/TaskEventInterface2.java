package com.openexchange.event.impl;

import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface TaskEventInterface2 extends TaskEventInterface{

    public void taskModified(Task oldTask, Task newTask, Session sessionObj);
}


package com.openexchange.fitnesse.tasks;

import java.util.List;
import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.tasks.TaskUpdateStep;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.fitnesse.wrappers.FitnesseResult;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link UpdateTask} - a wrapper to use TaskUpdateStep via FitNesse
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateTask extends AbstractTaskFixture {

    @Override
    protected Step createStep(Task task, String fixtureName, String expectedError) {
        return new TaskUpdateStep(task, data.getFixtureName(), data.getExpectedError());
    }

   

}

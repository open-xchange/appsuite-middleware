package com.openexchange.fitnesse.tasks;

import java.util.List;
import org.junit.ComparisonFailure;
import com.openexchange.ajax.kata.tasks.TaskVerificationStep;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.fitnesse.wrappers.FitnesseResult;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * {@link VerifyTask} - a wrapper to use TaskVerifyStep via FitNesse
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class VerifyTask extends AbstractTableTable {
    
    @Override
    public List doTable() throws Exception {
        final String fixtureName = data.getFixtureName();
        
        Task task = createTask(fixtureName, data);
        
        TaskVerificationStep taskStep = new TaskVerificationStep( task, data.getFixtureName() );
        taskStep.setIdentitySource( environment.getSymbol( fixtureName ) );
        environment.registerStep(taskStep);
        
        FitnesseResult returnValues = new FitnesseResult(data, FitnesseResult.PASS);
        
        try {
            taskStep.perform(environment.getClientForUser1());
        } catch (ComparisonFailure failure){
            int pos = findFailedFieldPosition( failure.getExpected() );
            returnValues.set(pos, FitnesseResult.ERROR + "expected:" + failure.getExpected() +", actual: "+ failure.getActual());
        }
        
        return returnValues.toResult();
    }
    
    public int findFailedFieldPosition(String expectedValue){
        for (int i = 0; i < data.size(); i++) {
            if ( expectedValue.equals( data.get(i) ) )
                return i;
        }
        throw new IllegalStateException("Could not find the broken field in the list of fields. This should not happen.");
        
    }

}

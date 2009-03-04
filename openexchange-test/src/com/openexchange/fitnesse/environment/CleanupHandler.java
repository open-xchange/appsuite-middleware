package com.openexchange.fitnesse.environment;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.kata.Step;

/**
 * 
 * {@link SymbolHandler}
 *
 * This is part of the environment FitNesse tests run in. The purpose of
 * this construct is to make sure that objects already created will be 
 * removed after the tests are done.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class CleanupHandler {
    List<Step> registeredSteps;
    
    public CleanupHandler() {
        super();
        registeredSteps = new LinkedList<Step>();
    }
    
    public void add(Step step) {
        registeredSteps.add(step);
    }

    /**
     * Perform the clean-up
     */
    public void perform() {
        List<Exception> exceptions = new LinkedList<Exception>();
        for(Step step: registeredSteps){
            try {
                step.cleanUp();
            } catch (Exception e) {
                exceptions.add(e);
                e.printStackTrace(); //TODO: remove if there is a better way
            }
        }
        //TODO: create MultiException thingy.
    }

}

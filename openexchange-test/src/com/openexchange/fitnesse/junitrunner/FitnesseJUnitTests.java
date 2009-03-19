
package com.openexchange.fitnesse.junitrunner;

/**
 * {@link FitnesseJUnitTests} - Runs FitNesse tests as JUnit tests. Usable by continuous integration systems.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FitnesseJUnitTests extends AbstractFitnesseJUnitTest {
    
    public FitnesseJUnitTests() {
        super("All fitnesse tests");
    }


    public void testAll() throws Exception {
        helper.assertSuitePasses("ContactTests");
    }

}

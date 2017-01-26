
package com.openexchange.l10n;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;

public class SuperCollatorTest {

    @Test
    public void testMustFindZhCn() {
        SuperCollator collator = SuperCollator.getByJavaLocale("zh_CN");
        assertNotNull(collator);
        assertNotSame(SuperCollator.DEFAULT, collator);
    }

    @Test
    public void testMustFindZhCn_() {
        SuperCollator collator = SuperCollator.getByJavaLocale("zh_CN_");
        assertNotNull(collator);
        assertNotSame(SuperCollator.DEFAULT, collator);
    }
}

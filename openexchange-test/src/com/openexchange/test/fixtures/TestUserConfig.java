
package com.openexchange.test.fixtures;

import com.openexchange.ajax.config.actions.Tree;

public interface TestUserConfig {

    public abstract Object get(final Tree tree);

    public abstract String getString(final Tree tree);

    public abstract boolean getBool(final Tree tree);

    public abstract long getLong(final Tree tree);

    public abstract int getInt(final Tree tree);

    public abstract void set(final Tree tree, final Object value);

}

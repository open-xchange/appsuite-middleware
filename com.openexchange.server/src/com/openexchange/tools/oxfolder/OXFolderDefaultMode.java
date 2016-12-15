
package com.openexchange.tools.oxfolder;

public enum OXFolderDefaultMode {
    DEFAULT("default"),
    DEFAULT_DELETABLE("default-deletable"),
    NONE("no-default-folders");

    private String text;

    private OXFolderDefaultMode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static OXFolderDefaultMode fromString(String text) {
        for (OXFolderDefaultMode mode : OXFolderDefaultMode.values()) {
            if (text.equalsIgnoreCase(mode.getText())) {
                return mode;
            }
        }
        return null;
    }
}

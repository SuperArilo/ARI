package com.tty.ari.enumType;

import com.tty.api.enumType.FilePathEnum;

public enum LangFile implements FilePathEnum {

    LANG("lang/[lang].yml", "[lang].yml"),
    DEATH_MESSAGE("lang/death-message/[lang].yml", "[lang].yml");

    private final String fullPathInJar;
    private final String fullFileName;

    LangFile(String fullPathInJar, String fullFileName) {
        this.fullPathInJar = fullPathInJar;
        this.fullFileName = fullFileName;
    }

    @Override
    public String getFullPathInJar() {
        return this.fullPathInJar;
    }

    @Override
    public String getFullFileName() {
        return this.fullFileName;
    }
}

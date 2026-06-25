package com.tty.ari.enumType;

import com.tty.api.enumType.FilePathEnum;

public enum LangFile implements FilePathEnum {

    LANG("lang/[lang].yml"),
    DEATH_MESSAGE("lang/death-message/[lang].yml");

    private final String filePath;

    LangFile(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String getPath() {
        return this.filePath;
    }
}

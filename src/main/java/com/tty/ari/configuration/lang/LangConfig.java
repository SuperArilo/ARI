package com.tty.ari.configuration.lang;

import com.tty.api.configuration.LangConfiguration;
import com.tty.ari.Ari;
import com.tty.ari.enumType.LangFile;

public class LangConfig extends LangConfiguration {
    public LangConfig() {
        super(Ari.instance, LangFile.LANG.getPath());
    }
}

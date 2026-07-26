package com.tty.ari.configuration.lang;

import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.Ari;
import com.tty.ari.enumType.LangFile;

public class LangConfig extends BaseLangConfig {

    public LangConfig() {
        super(Ari.instance, LangFile.LANG);
    }

    public LangConfig(AbstractJavaPlugin plugin) {
        super(plugin);
    }

}

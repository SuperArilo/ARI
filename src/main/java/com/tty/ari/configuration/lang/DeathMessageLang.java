package com.tty.ari.configuration.lang;

import com.tty.api.AbstractJavaPlugin;
import com.tty.ari.Ari;
import com.tty.ari.enumType.LangFile;

public class DeathMessageLang extends BaseLangConfig {

    public DeathMessageLang() {
        super(Ari.instance, LangFile.DEATH_MESSAGE);
    }

    public DeathMessageLang(AbstractJavaPlugin plugin) {
        super(plugin);
    }

}

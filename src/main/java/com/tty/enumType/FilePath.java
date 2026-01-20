package com.tty.enumType;


import com.tty.lib.Log;
import com.tty.lib.enum_type.FilePathEnum;
import lombok.Getter;

import java.util.Arrays;

public enum FilePath implements FilePathEnum {
    LANG("lang", "lang/[lang].yml"),
    COMMAND_ALIAS("command-alias", "module/command-alias.yml"),
    TPA_CONFIG("tpa", "module/tpa/setting.yml"),
    BACK_CONFIG("back", "module/back/setting.yml"),
    RTP_CONFIG("rtp", "module/rtp/setting.yml"),
    HOME_LIST_GUI("home-gui", "module/home/home-gui.yml"),
    HOME_CONFIG("home", "module/home/setting.yml"),
    HOME_EDIT_GUI("home-edit-gui", "module/home/home-edit-gui.yml"),
    WARP_LIST_GUI("warp-gui", "module/warp/warp-gui.yml"),
    WARP_CONFIG("warp", "module/warp/setting.yml"),
    WARP_EDIT_GUI("warp-edit-gui", "module/warp/warp-edit-gui.yml"),
    FUNCTION_CONFIG("function", "module/function/setting.yml"),
    SPAWN_CONFIG("spawn", "module/spawn/setting.yml"),
    DEATH_MESSAGE("death-message", "lang/death-message/[lang].yml");

    @Getter
    private final String nickName;
    private final String path;

    FilePath(String nickName, String path) {
        this.nickName = nickName;
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}

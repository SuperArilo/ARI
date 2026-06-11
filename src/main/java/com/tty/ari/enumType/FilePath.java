package com.tty.ari.enumType;

import com.tty.api.enumType.FilePathEnum;

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
    SPAWN_CONFIG("spawn", "module/spawn/setting.yml"),

    TAB_LIST_CONFIG("tab-list", "module/tab-list.yml"),
    GAME_ACTION_CONFIG("game-action", "module/game-action.yml"),
    CHAT_CONFIG("chat", "module/chat.yml"),
    ATTACK_BAR_CONFIG("attack-bar", "module/attack-bar.yml"),


    DEATH_MESSAGE("death-message", "lang/death-message/[lang].yml"),

    INV_GUI_CONFIG("inv-gui", "module/check-inventory-layout.yml");

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

    @Override
    public String getNickName() {
        return this.nickName;
    }

}

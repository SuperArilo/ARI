package com.tty.ari.enumType;

import com.tty.api.enumType.FilePathEnum;

public enum FilePath implements FilePathEnum {
    COMMAND_ALIAS("module/command-alias.yml"),
    TPA_CONFIG("module/tpa/setting.yml"),
    BACK_CONFIG("module/back.yml"),
    RTP_CONFIG("module/rtp/setting.yml"),
    HOME_LIST_GUI("module/home/home-gui.yml"),
    HOME_CONFIG("module/home/setting.yml"),
    HOME_EDIT_GUI("module/home/home-edit-gui.yml"),
    WARP_LIST_GUI("module/warp/warp-gui.yml"),
    WARP_CONFIG("module/warp/setting.yml"),
    WARP_EDIT_GUI("module/warp/warp-edit-gui.yml"),
    SPAWN_CONFIG("module/spawn.yml"),
    TAB_LIST_CONFIG("module/tab-list.yml"),
    GAME_ACTION_CONFIG("module/game-action.yml"),
    CHAT_CONFIG("module/chat.yml"),
    ATTACK_BAR_CONFIG("module/attack-bar.yml"),
    INV_GUI_CONFIG("module/check-inventory-layout.yml");

    private final String path;

    FilePath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}

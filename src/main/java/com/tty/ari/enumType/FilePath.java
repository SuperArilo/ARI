package com.tty.ari.enumType;

import com.tty.api.enumType.FilePathEnum;

public enum FilePath implements FilePathEnum {
    COMMAND_ALIAS("module/command-alias.yml", "command-alias.yml"),
    FUNCTION_CONFIG("module/function.yml", "function.yml"),
    HOME_LIST_GUI("module/home/home-gui.yml", "home-gui.yml"),
    HOME_CONFIG("module/home/setting.yml", "setting.yml"),
    HOME_EDIT_GUI("module/home/home-edit-gui.yml", "home-edit-gui.yml"),
    WARP_LIST_GUI("module/warp/warp-gui.yml", "warp-gui.yml"),
    WARP_CONFIG("module/warp/setting.yml", "setting.yml"),
    WARP_EDIT_GUI("module/warp/warp-edit-gui.yml", "warp-edit-gui.yml"),
    TAB_LIST_CONFIG("module/tab-list.yml", "tab-list.yml"),
    GAME_ACTION_CONFIG("module/game-action.yml", "game-action.yml"),
    CHAT_CONFIG("module/chat.yml", "chat.yml"),
    ATTACK_BAR_CONFIG("module/attack-bar.yml", "attack-bar.yml"),
    INV_GUI_CONFIG("module/check-inventory-layout.yml", "check-inventory-layout.yml");

    private final String fullPathInJar;
    private final String fullFileName;

    FilePath(String fullPathInJar, String fullFileName) {
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

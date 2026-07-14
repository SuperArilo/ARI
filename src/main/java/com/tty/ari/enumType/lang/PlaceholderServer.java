package com.tty.ari.enumType.lang;

import com.tty.api.enumType.PlaceholderTypeEnum;

public enum PlaceholderServer implements PlaceholderTypeEnum {

    SERVER_VERSION("server_version"),
    PLUGIN_BRANCH("plugin_branch"),
    PLUGIN_BUILD_TIME("plugin_build_time"),
    PLUGIN_BUILD_VERSION("plugin_build_version"),
    PLUGIN_COMMIT_ID_ABBREV("plugin_commit_id_abbrev"),
    PLUGIN_COMMIT_MESSAGE("plugin_commit_message"),
    PLUGIN_COMMIT_TIME("plugin_commit_time"),
    PLUGIN_COMMIT_USER_NAME("plugin_commit_user_name"),
    PLUGIN_GIT_TAG("plugin_git_tag"),
    PLUGIN_DEBUG_STATUS("plugin_debug_status");

    private final String type;

    PlaceholderServer(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

}

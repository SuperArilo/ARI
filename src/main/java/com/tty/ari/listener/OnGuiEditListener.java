package com.tty.ari.listener;

import com.tty.ari.Ari;
import com.tty.api.AbstractJavaPlugin;
import com.tty.api.gui.BaseInventory;
import com.tty.api.listener.BaseEditFunctionGuiListener;
import com.tty.api.state.GuiEditFunctionState;
import com.tty.ari.enumType.GuiType;
import com.tty.ari.states.gui.GuiEditFunctionStateService;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public abstract class OnGuiEditListener<T extends BaseInventory, D> extends BaseEditFunctionGuiListener<T, D> {

    private static final Pattern CONTENT_MESSAGE_PATTERN = Pattern.compile("^[a-zA-Z0-9\\\\u4e00-\\\\9fa5 ]+$");

    private static final Pattern PERMISSION_NODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");

    protected OnGuiEditListener(AbstractJavaPlugin plugin, GuiType guiType) {
        super(plugin, guiType);
    }

    @Override
    public GuiEditFunctionState<D> isHaveState(Player player) {
        GuiEditFunctionStateService stateService = Ari.instance.getStatusManager().get(GuiEditFunctionStateService.class);
        if (stateService.stateIsEmpty()) return null;
        if (stateService.isNotHaveState(player)) return null;
        List<GuiEditFunctionState> states = stateService.getStates(player);
        if (states.isEmpty()) {
            Ari.instance.getLog().error("player {} on edit status error, states is empty", player.getName());
            return null;
        }
        return states.getFirst();
    }

    /**
     * 检查 content内容 字符串是否合法
     * @param content 待检查字符串
     * @return 空值或不符合格式返回 false
     */
    protected boolean isContentValid(String content) {
        return content != null && CONTENT_MESSAGE_PATTERN.matcher(content).matches();
    }

    /**
     * 验证Minecraft权限节点格式
     * @param node 权限节点字符串
     * @return 空值或不符合格式返回false
     */
    public boolean isValidPermissionNode(String node) {
        return node != null && PERMISSION_NODE_PATTERN.matcher(node).matches();
    }

}

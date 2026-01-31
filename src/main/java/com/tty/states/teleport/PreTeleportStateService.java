package com.tty.states.teleport;

import com.tty.Ari;
import com.tty.Log;
import com.tty.dto.state.teleport.PreEntityToEntityState;
import com.tty.lib.services.StateService;
import com.tty.states.CoolDownStateService;
import com.tty.tool.ConfigUtils;
import com.tty.tool.StateMachineManager;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PreTeleportStateService extends StateService<PreEntityToEntityState> {

    public PreTeleportStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected void loopExecution(PreEntityToEntityState state) {

        Player owner = (Player) state.getOwner();
        Player target = (Player) state.getTarget();

        // 基本合法性检查
        if (target instanceof Player p && !p.isOnline()) {
            state.setOver(true);
            return;
        }

        if (target == null) {
            ConfigUtils.t("teleport.unable-player", owner).thenAccept(owner::sendMessage);
            state.setOver(true);
            return;
        }

        if (target.getName().equals(owner.getName())) {
            ConfigUtils.t("function.public.fail", owner).thenAccept(owner::sendMessage);
            state.setOver(true);
            return;
        }
        state.setPending(false);
        Log.debug("checking player {} -> {} request. count {}, max_count {}", owner.getName(), target.getName(), state.getCount(), state.getMax_count());
    }

    @Override
    protected void abortAddState(PreEntityToEntityState state) {
    }

    @Override
    protected void passAddState(PreEntityToEntityState state) {
        Player owner = (Player) state.getOwner();
        Player target = (Player) state.getTarget();

        ConfigUtils.t("function.tpa.send-message", owner).thenAccept(owner::sendMessage);

        Ari.PLACEHOLDER.render("function.tpa." + (state.getType().getKey().equals("tpa") ? "to-message" : "here-message"), (OfflinePlayer) owner)
            .thenAccept(result ->
                    Ari.SCHEDULER.runAtEntity(Ari.instance, target, task -> target.sendMessage(
                        result
                        .appendNewline()
                        .append(Ari.COMPONENT_SERVICE.setClickEventText(
                                Ari.DATA_SERVICE.getValue("function.public.agree"),
                                ClickEvent.Action.RUN_COMMAND,
                                "/ari tpaaccept " + owner.getName()))
                        .append(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("function.public.center")))
                        .append(Ari.COMPONENT_SERVICE.setClickEventText(
                                Ari.DATA_SERVICE.getValue("function.public.refuse"),
                                ClickEvent.Action.RUN_COMMAND,
                                "/ari tparefuse " + owner.getName()))), null));
    }

    @Override
    protected void onEarlyExit(PreEntityToEntityState state) {
    }

    @Override
    protected void onFinished(PreEntityToEntityState state) {
        Log.debug("player {} send to {} teleport request expired",  state.getOwner().getName(), state.getTarget().getName());
    }

    @Override
    protected void onServiceAbort(PreEntityToEntityState state) {

    }

    @Override
    protected boolean canAddState(PreEntityToEntityState state) {
        Player owner = (Player) state.getOwner();
        Player target = (Player) state.getTarget();
        StateMachineManager manager = Ari.STATE_MACHINE_MANAGER;
        //判断当前实体是否在传送冷却中
        if (!manager.get(CoolDownStateService.class).getStates(owner).isEmpty()) {
            ConfigUtils.t("teleport.cooling").thenAccept(owner::sendMessage);
            return false;
        }

        //检查是否已经发过请求了
        if (!this.getStates(owner).isEmpty()) {
            Ari.PLACEHOLDER.render("function.tpa.again", (OfflinePlayer) owner).thenAccept(i ->
                    Ari.SCHEDULER.runAtEntity(Ari.instance, owner, t -> owner.sendMessage(i), null));
            return false;
        }

        //判断当前发起玩家或目标玩家是否在传送状态中或者是否正在进行 rtp 传送
        if (!manager.get(TeleportStateService.class).getStates(owner).isEmpty() ||
                !manager.get(RandomTpStateService.class).getStates(owner).isEmpty() ||
                !manager.get(TeleportStateService.class).getStates(target).isEmpty() ||
                !manager.get(RandomTpStateService.class).getStates(target).isEmpty()) {
            ConfigUtils.t("teleport.has-teleport", owner).thenAccept(owner::sendMessage);
            return false;
        }

        return true;
    }

    public List<PreEntityToEntityState> getList() {
        return this.stateList;
    }
}

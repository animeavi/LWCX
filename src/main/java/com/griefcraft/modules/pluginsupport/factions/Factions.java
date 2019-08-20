package com.griefcraft.modules.pluginsupport.factions;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig.Factions.Protection;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import org.bukkit.plugin.Plugin;

public class Factions extends JavaModule {
    private boolean factionCheck;
    private Plugin factions = null;

    @Override
    public void load(LWC lwc) {
        this.factionCheck = lwc.getConfiguration().getBoolean("core.factionCheck", false);
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (factions == null || !factionCheck) {
            return;
        }

        FLocation fLocation = new FLocation(event.getBlock().getLocation());
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(event.getPlayer());
        Faction faction = Board.getInstance().getFactionAt(fLocation);

        if (!canProtect(fLocation, fPlayer, faction)) {
            event.getLWC().sendLocale(event.getPlayer(), "lwc.factions.blocked");
            event.setCancelled(true);
        }
    }

    public boolean canProtect(FLocation fLocation, FPlayer fPlayer, Faction faction) {
        Protection prot = FactionsPlugin.getInstance().conf().factions().protection();
        boolean fBypass = prot.getPlayersWhoBypassAllProtection().contains(fPlayer.getName())
                || fPlayer.isAdminBypassing();
        boolean fWilderness = faction.isWilderness() && !prot.isWildernessDenyBuild();
        boolean fWarzone = faction.isWarZone() && !prot.isWarZoneDenyBuild();
        boolean fSafezone = faction.isSafeZone() && !prot.isSafeZoneDenyBuild();
        if (fBypass || fWilderness || fWarzone || fSafezone)
            return true;

        if (faction.hasAccess(fPlayer, PermissibleAction.BUILD)) {
            return true;
        }

        return false;
    }
}

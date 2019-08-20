package com.griefcraft.modules.pluginsupport.factions;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import org.bukkit.plugin.Plugin;

public class FactionsLegacy extends JavaModule {
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
        boolean fBypass = Conf.playersWhoBypassAllProtection.contains(fPlayer.getName())
                || fPlayer.isAdminBypassing();
        boolean fWilderness = faction.isWilderness() && !Conf.wildernessDenyBuild;
        boolean fWarzone = faction.isWarZone() && !Conf.warZoneDenyBuild;
        boolean fSafezone = faction.isSafeZone() && !Conf.safeZoneDenyBuild;
        if (fBypass || fWilderness || fWarzone || fSafezone)
            return true;

        Faction myFaction = fPlayer.getFaction();
        if (!myFaction.getRelationTo(faction).confDenyBuild(faction.hasPlayersOnline())) {
            return true;
        } else {
            Access fAccess = faction.getAccess(fPlayer, PermissableAction.BUILD);
            if (fAccess != null) {
                return (fAccess == Access.ALLOW);
            }
        }

        return false;
    }
}

package io.github.mooy1.infinityexpansion.items.mobdata;

import org.bukkit.Material;

public enum MobDataTier {

    // ex: chicken
    PASSIVE(1, 75, Material.GREEN_CANDLE),

    // ex: slime
    NEUTRAL(1, 150, Material.BLUE_CANDLE),

    // ex: zombie
    HOSTILE(2, 300, Material.RED_CANDLE),

    // ex: enderman
    ADVANCED(4, 600, Material.RED_CANDLE),

    // ex: wither
    MINI_BOSS(32, 4500, Material.BLACK_CANDLE),

    // ex: ender dragon
    BOSS(96, 9000, Material.BLACK_CANDLE);

    final int xp;
    final int energy;
    final Material material;

    MobDataTier(int xp, int energy, Material material) {
        this.xp = (int) (xp * MobSimulationChamber.XP_MULTIPLIER);
        this.energy = energy;
        this.material = material;
    }

}

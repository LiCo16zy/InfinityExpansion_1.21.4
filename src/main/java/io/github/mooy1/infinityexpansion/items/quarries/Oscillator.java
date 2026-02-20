package io.github.mooy1.infinityexpansion.items.quarries;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.mooy1.infinityexpansion.categories.Groups;
import io.github.mooy1.infinityexpansion.items.materials.Materials;
import io.github.mooy1.infinitylib.common.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;

import net.guizhanss.guizhanlib.minecraft.helper.MaterialHelper;

public final class Oscillator extends SlimefunItem {

    private static final Map<String, Oscillator> OSCILLATORS = new HashMap<>();

    public final double chance;
    public final Material output;

    @Nullable
    public static Oscillator getOscillator(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        return OSCILLATORS.get(StackUtils.getId(item));
    }

    public static Oscillator forOre(Material ore, double chance) {
        SlimefunItemStack item = new SlimefunItemStack(
                "QUARRY_OSCILLATOR_" + ore.name(),
                ore,
                "&6" + MaterialHelper.getName(ore) + " 生产加速器",
                "&7放置在矿机中",
                "&7提高 " + (chance * 100) + "% 挖到此矿的几率"
        );
        ItemStack[] recipe = {
                Materials.MACHINE_PLATE, SlimefunItems.BLISTERING_INGOT_3, Materials.MACHINE_PLATE,
                SlimefunItems.BLISTERING_INGOT_3, new ItemStack(ore), SlimefunItems.BLISTERING_INGOT_3,
                Materials.MACHINE_PLATE, SlimefunItems.BLISTERING_INGOT_3, Materials.MACHINE_PLATE
        };
        return new Oscillator(item, recipe, chance, ore);
    }

    public static Oscillator forSingularity(Material ore, Material oreBlock, double chance, Oscillator base, SlimefunItemStack singularity) {
        SlimefunItemStack item = new SlimefunItemStack(
                "QUARRY_SINGULARITY_OSCILLATOR_" + ore.name(),
                oreBlock,
                "&b" + MaterialHelper.getName(ore) + " 奇点加速器",
                "",
                "&7奇点的力量融入了矿机..."
        );
        ItemStack[] recipe = {
                Materials.VOID_INGOT, base.getItem(), Materials.VOID_INGOT,
                base.getItem(), singularity, base.getItem(),
                Materials.VOID_INGOT, base.getItem(), Materials.VOID_INGOT
        };
        return new Oscillator(item, recipe, chance, ore);
    }

    private Oscillator(SlimefunItemStack item, ItemStack[] recipe, double chance, Material output) {
        super(Groups.MAIN_MATERIALS, item, RecipeType.ENHANCED_CRAFTING_TABLE, recipe);
        OSCILLATORS.put(getId(), this);
        this.chance = chance;
        this.output = output;
    }

}

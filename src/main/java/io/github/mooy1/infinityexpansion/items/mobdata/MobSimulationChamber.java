package io.github.mooy1.infinityexpansion.items.mobdata;

import javax.annotation.Nonnull;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.mooy1.infinityexpansion.utils.Util;
import io.github.mooy1.infinitylib.common.StackUtils;
import io.github.mooy1.infinitylib.machines.AbstractMachineBlock;
import io.github.mooy1.infinitylib.machines.MachineLore;
import io.github.mooy1.infinitylib.machines.TickingMenuBlock;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;

public final class MobSimulationChamber extends TickingMenuBlock implements EnergyNetComponent {

    static final double XP_MULTIPLIER = InfinityExpansion.config().getDouble("mob-simulation-options.xp-multiplier", 0, 1000);
    static final boolean CHIP_STACKING = InfinityExpansion.config().getBoolean("mob-simulation-options.enable-chip-stacking");

    private static final ItemStack NO_CARD = new CustomItemStack(Material.BARRIER, "&c请放入生物芯片!");
    private static final ItemStack CHIP_OVERLOAD = new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c芯片过载!", "&7请减少芯片数量");
    private static final int CARD_SLOT = 37;
    private static final int STATUS_SLOT = 10;
    private static final int[] OUTPUT_SLOTS = {
            13, 14, 15, 16,
            22, 23, 24, 25,
            31, 32, 33, 34,
            40, 41, 42, 43
    };
    private static final int XP_SLOT = 46;

    private final int energy;
    private final int buffer;
    private final int interval;

    public MobSimulationChamber(ItemGroup category, SlimefunItemStack item, RecipeType type, ItemStack[] recipe, int energy, int buffer, int interval) {
        super(category, item, type, recipe);
        this.energy = energy;
        this.buffer = buffer;
        this.interval = interval;
    }

    @Override
    protected void onBreak(@Nonnull BlockBreakEvent e, @Nonnull BlockMenu menu) {
        super.onBreak(e, menu);
        e.getPlayer().giveExp(Util.getIntData("xp", menu.getLocation()));
        StorageCacheUtils.setData(menu.getLocation(), "xp", "0");
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return this.buffer;
    }

    @Override
    protected void setup(BlockMenuPreset blockMenuPreset) {
        blockMenuPreset.drawBackground(OUTPUT_BORDER, new int[] {
                3, 4, 5, 6, 7, 8,
                12, 17,
                21, 26,
                30, 35,
                39, 44,
                48, 49, 50, 51, 52, 53
        });
        blockMenuPreset.drawBackground(new int[] {
                0, 1, 2,
                9, 11,
                18, 19, 20,
                STATUS_SLOT, XP_SLOT
        });
        blockMenuPreset.drawBackground(INPUT_BORDER, new int[] {
                27, 28, 29,
                36, 38,
                45, 46, 47
        });
    }

    @Nonnull
    @Override
    protected int[] getInputSlots(@Nonnull DirtyChestMenu menu, @Nonnull ItemStack item) {
        return new int[0];
    }

    @Override
    protected int[] getInputSlots() {
        return new int[] { CARD_SLOT };
    }

    @Override
    protected int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    public void onNewInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {
        Location l = b.getLocation();
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(l);
        if (blockData.getData("xp") == null) {
            blockData.setData("xp", "O");
        }
        menu.replaceExistingItem(XP_SLOT, makeXpItem(0));
        menu.addMenuClickHandler(XP_SLOT, (p, slot, item, action) -> {
            int xp = Util.getIntData("xp", l);
            if (xp > 0) {
                p.giveExp(xp);
                p.playSound(l, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                blockData.setData("xp", "O");
                menu.replaceExistingItem(XP_SLOT, makeXpItem(0));
            }
            return false;
        });
    }

    private static ItemStack makeXpItem(int stored) {
        return new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a已储存经验值: " + stored, "", "&a> 点击领取");
    }

    @Override
    protected void tick(@Nonnull Block b, @Nonnull BlockMenu inv) {
        ItemStack input = inv.getItemInSlot(CARD_SLOT);

        if (input == null) {
            return;
        }

        MobDataCard card = MobDataCard.CARDS.get(StackUtils.getId(input));

        if (card == null) {
            if (inv.hasViewer()) {
                inv.replaceExistingItem(STATUS_SLOT, NO_CARD);
            }
            return;
        }

        if (CHIP_STACKING && input.getAmount() * card.tier.energy + this.energy > this.buffer) {
            if (inv.hasViewer()) {
                inv.replaceExistingItem(STATUS_SLOT, CHIP_OVERLOAD);
            }
            return;
        }

        int effectiveCount = CHIP_STACKING ? input.getAmount() : 1;
        int energy = card.tier.energy * effectiveCount + this.energy;

        if (getCharge(b.getLocation()) < energy) {
            if (inv.hasViewer()) {
                inv.replaceExistingItem(STATUS_SLOT, AbstractMachineBlock.NO_ENERGY_ITEM);
            }
            return;
        }

        removeCharge(b.getLocation(), energy);

        int xp = Util.getIntData("xp", b.getLocation());

        if (inv.hasViewer()) {
            inv.replaceExistingItem(STATUS_SLOT, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,
                    "&a模拟中... (" + MachineLore.formatEnergy(energy) + " J/s)")
            );
            inv.replaceExistingItem(XP_SLOT, makeXpItem(xp));
        }

        if (InfinityExpansion.slimefunTickCount() % this.interval != 0) {
            return;
        }

        StorageCacheUtils.setData(b.getLocation(), "xp",
                String.valueOf(xp + card.tier.xp * effectiveCount));

        for (int i = 0; i < effectiveCount; i++) {
            ItemStack item = card.drops.getRandom();
            if (inv.fits(item, OUTPUT_SLOTS)) {
                inv.pushItem(item.clone(), OUTPUT_SLOTS);
            } else if (inv.hasViewer()) {
                inv.replaceExistingItem(STATUS_SLOT, NO_ROOM_ITEM);
            }
        }
    }
}

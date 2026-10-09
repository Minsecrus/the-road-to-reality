package com.trtr.crafting;

import com.trtr.TRTRItems;
import com.trtr.TRTRMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** The server owns the shape and escrows one flint; replayed or forged button packets cannot create items. */
public final class KnappingMenu extends AbstractContainerMenu {
    public static final int NEW_FLINT = 25;
    private final Player owner;
    private final SimpleContainerData data = new SimpleContainerData(2);
    private ItemStack material = ItemStack.EMPTY;

    public KnappingMenu(int id, Inventory inventory) {
        super(TRTRMenus.KNAPPING.get(), id);
        owner = inventory.player;
        data.set(0, KnappingPattern.FULL);
        addDataSlots(data);
    }

    public KnappingMenu(int id, Inventory inventory, InteractionHand hand) {
        this(id, inventory);
        ItemStack source = owner.getItemInHand(hand);
        if (isFlint(source)) {
            material = owner.hasInfiniteMaterials() ? source.copyWithCount(1) : source.split(1);
            data.set(1, 1);
        }
    }

    public static boolean isFlint(ItemStack stack) {
        return stack.is(TRTRItems.FLINT.get()) || stack.is(Items.FLINT);
    }

    public static void open(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server && isFlint(player.getItemInHand(hand))) {
            server.openMenu(new SimpleMenuProvider(
                    (id, inventory, ignored) -> new KnappingMenu(id, inventory, hand),
                    Component.translatable("menu.trtr.knapping")));
        }
    }

    public int mask() { return data.get(0); }
    public boolean hasMaterial() { return data.get(1) == 1; }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (player.level().isClientSide() || !stillValid(player)) return false;
        if (button == NEW_FLINT) {
            if (hasMaterial() && mask() == KnappingPattern.FULL) return false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack source = player.getInventory().getItem(i);
                if (isFlint(source)) {
                    material = player.hasInfiniteMaterials() ? source.copyWithCount(1) : source.split(1);
                    data.set(0, KnappingPattern.FULL);
                    data.set(1, 1);
                    broadcastChanges();
                    return true;
                }
            }
            player.displayClientMessage(Component.translatable("message.trtr.knapping_no_flint"), true);
            return false;
        }
        if (!hasMaterial() || KnappingPattern.ruined(mask()) || !KnappingPattern.canChip(mask(), button)) return false;
        int next = mask() & ~(1 << button);
        data.set(0, next);
        player.level().playSound(null, player.blockPosition(), SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.5F, 1.4F);
        if (next == KnappingPattern.BLADE) {
            material = ItemStack.EMPTY;
            data.set(1, 0);
            give(player, new ItemStack(TRTRItems.FLINT_BLADE.get()));
        }
        broadcastChanges();
        return true;
    }

    public static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && mask() == KnappingPattern.FULL && !material.isEmpty()
                && !player.hasInfiniteMaterials()) {
            give(player, material);
        }
        material = ItemStack.EMPTY;
        data.set(1, 0);
    }

    @Override public boolean stillValid(Player player) { return player == owner && player.isAlive(); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}

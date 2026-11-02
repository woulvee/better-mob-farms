package com.woulvee.bettermobfarms.menu;

import com.woulvee.bettermobfarms.block.entity.FeedingTroughBlockEntity;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// Laid out like the hopper: three food slots in a row above the player inventory
public class FeedingTroughMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = FeedingTroughBlockEntity.CONTAINER_SIZE;
    private static final int INV_SLOT_END = SLOT_COUNT + 36;

    private final Container trough;

    // Client side constructor
    public FeedingTroughMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT));
    }

    public FeedingTroughMenu(int containerId, Inventory inventory, Container trough) {
        super(BMFMenus.FEEDING_TROUGH.get(), containerId);
        checkContainerSize(trough, SLOT_COUNT);
        this.trough = trough;
        trough.startOpen(inventory.player);

        for (int i = 0; i < SLOT_COUNT; i++) {
            addSlot(new Slot(trough, i, 62 + i * 18, 20));
        }
        addStandardInventorySlots(inventory, 8, 51);
    }

    @Override
    public boolean stillValid(Player player) {
        return trough.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();
            if (slotIndex < SLOT_COUNT) {
                if (!moveItemStackTo(stack, SLOT_COUNT, INV_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return clicked;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        trough.stopOpen(player);
    }
}

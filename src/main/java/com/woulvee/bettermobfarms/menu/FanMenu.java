package com.woulvee.bettermobfarms.menu;

import com.woulvee.bettermobfarms.block.entity.FanBlockEntity;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FanMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = FanBlockEntity.CONTAINER_SIZE;
    private static final int INV_SLOT_END = SLOT_COUNT + 36;

    private final Container fan;

    // Client side constructor
    public FanMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT));
    }

    public FanMenu(int containerId, Inventory inventory, Container fan) {
        super(BMFMenus.FAN.get(), containerId);
        checkContainerSize(fan, SLOT_COUNT);
        this.fan = fan;
        fan.startOpen(inventory.player);

        addSlot(new UpgradeSlot(fan, FanBlockEntity.SLOT_WIDTH, 52, 21));
        addSlot(new UpgradeSlot(fan, FanBlockEntity.SLOT_HEIGHT, 52, 47));
        addSlot(new UpgradeSlot(fan, FanBlockEntity.SLOT_LENGTH, 107, 21));
        addSlot(new UpgradeSlot(fan, FanBlockEntity.SLOT_POWER, 107, 47));
        addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return fan.stillValid(player);
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

            if (stack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return clicked;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        fan.stopOpen(player);
    }

    // Accepts only the upgrade that belongs to its slot
    private static class UpgradeSlot extends Slot {
        UpgradeSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return itemStack.is(FanBlockEntity.upgradeForSlot(getContainerSlot()));
        }
    }
}

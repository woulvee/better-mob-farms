package com.woulvee.bettermobfarms.menu;

import com.woulvee.bettermobfarms.block.entity.IncubatorBlockEntity;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class IncubatorMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = IncubatorBlockEntity.CONTAINER_SIZE;
    private static final int INV_SLOT_END = SLOT_COUNT + 36;

    private final Container incubator;
    private final ContainerData data;

    // Client side constructor
    public IncubatorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(IncubatorBlockEntity.DATA_COUNT));
    }

    public IncubatorMenu(int containerId, Inventory inventory, Container incubator, ContainerData data) {
        super(BMFMenus.INCUBATOR.get(), containerId);
        checkContainerSize(incubator, SLOT_COUNT);
        checkContainerDataCount(data, IncubatorBlockEntity.DATA_COUNT);
        this.incubator = incubator;
        this.data = data;
        incubator.startOpen(inventory.player);

        addSlot(new IncubatorSlot(incubator, IncubatorBlockEntity.SLOT_DNA, 26, 14));
        addSlot(new IncubatorSlot(incubator, IncubatorBlockEntity.SLOT_EGG, 26, 54));
        addSlot(new IncubatorSlot(incubator, IncubatorBlockEntity.SLOT_NUTRIENTS, 116, 28));
        addSlot(new IncubatorSlot(incubator, IncubatorBlockEntity.SLOT_MUTATED_EGG, 80, 54));
        addSlot(new IncubatorSlot(incubator, IncubatorBlockEntity.SLOT_SPAWN_EGG, 151, 54));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    // Progress of the processes from 0 to 1
    public float injectProgress() {
        return (float) data.get(IncubatorBlockEntity.DATA_INJECT) / IncubatorBlockEntity.INJECT_TIME;
    }

    public float mutateProgress() {
        return (float) data.get(IncubatorBlockEntity.DATA_MUTATE) / IncubatorBlockEntity.MUTATE_TIME;
    }

    public float incubateProgress() {
        return (float) data.get(IncubatorBlockEntity.DATA_INCUBATE) / IncubatorBlockEntity.INCUBATE_TIME;
    }

    public ItemStack dnaSyringe() {
        return incubator.getItem(IncubatorBlockEntity.SLOT_DNA);
    }

    @Override
    public boolean stillValid(Player player) {
        return incubator.stillValid(player);
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
        incubator.stopOpen(player);
    }

    // Accepts only what IncubatorBlockEntity#isValidForSlot allows, the spawn egg output takes nothing
    private static class IncubatorSlot extends Slot {
        IncubatorSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return IncubatorBlockEntity.isValidForSlot(getContainerSlot(), itemStack);
        }
    }
}

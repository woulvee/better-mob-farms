package com.woulvee.bettermobfarms.block.entity;

import java.util.Optional;

import com.woulvee.bettermobfarms.block.IncubatorBlock;
import com.woulvee.bettermobfarms.block.IncubatorEgg;
import com.woulvee.bettermobfarms.item.BMFDataComponents;
import com.woulvee.bettermobfarms.item.BMFItems;
import com.woulvee.bettermobfarms.menu.IncubatorMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

// The incubator runs three processes:
// 1. Injecting: a DNA syringe is injected into an egg. The egg is used up and the empty syringe stays in its slot.
// 2. Mutating: starts right after injecting and ends with a mutated egg carrying the DNA in the mutated egg slot.
// 3. Incubating: a mutated egg and one liquid nutrients turn into the spawn egg of the DNA's mob.
// Mutating and incubating can run at the same time, a new injection waits until the mutation is done.
public class IncubatorBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_DNA = 0;
    public static final int SLOT_EGG = 1;
    public static final int SLOT_NUTRIENTS = 2;
    public static final int SLOT_MUTATED_EGG = 3;
    public static final int SLOT_SPAWN_EGG = 4;
    public static final int CONTAINER_SIZE = 5;

    public static final int INJECT_TIME = 60;
    public static final int MUTATE_TIME = 200;
    public static final int INCUBATE_TIME = 400;

    public static final int DATA_INJECT = 0;
    public static final int DATA_MUTATE = 1;
    public static final int DATA_INCUBATE = 2;
    public static final int DATA_COUNT = 3;

    private static final Component DEFAULT_NAME = Component.translatable("block.bettermobfarms.incubator");

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int injectProgress;
    private int mutateProgress;
    private int incubateProgress;
    // The DNA of the egg that is mutating right now, and which egg it was for the model
    private @Nullable EntityType<?> mutatingDna;
    private IncubatorEgg mutatingEgg = IncubatorEgg.NONE;
    // Server: the DNA last sent to clients. Client: the DNA the mutated egg is colored with.
    private @Nullable EntityType<?> displayDna;
    // Players with the menu open, the door is open while there are any
    private int openCount;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_INJECT -> injectProgress;
                case DATA_MUTATE -> mutateProgress;
                default -> incubateProgress;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_INJECT -> injectProgress = value;
                case DATA_MUTATE -> mutateProgress = value;
                default -> incubateProgress = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(BMFBlockEntities.INCUBATOR.get(), pos, state);
    }

    // Which items a slot accepts, shared with the menu. The output slots take nothing but mutated eggs.
    public static boolean isValidForSlot(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_DNA -> stack.is(BMFItems.SYRINGE_DNA.get()) && stack.has(BMFDataComponents.DNA.get())
                    && !stack.has(BMFDataComponents.PLAYER_DNA.get());
            case SLOT_EGG -> stack.is(ItemTags.EGGS);
            case SLOT_NUTRIENTS -> stack.is(BMFItems.LIQUID_NUTRIENTS.get());
            case SLOT_MUTATED_EGG -> stack.is(BMFItems.MUTATED_EGG.get()) && stack.has(BMFDataComponents.DNA.get());
            default -> false;
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IncubatorBlockEntity incubator) {
        boolean changed = incubator.tickInjecting() | incubator.tickMutating() | incubator.tickIncubating();
        if (changed) {
            incubator.setChanged();
        }
    }

    private boolean tickInjecting() {
        EntityType<?> dna = items.get(SLOT_DNA).get(BMFDataComponents.DNA.get());
        boolean canInject = mutatingDna == null && dna != null
                && items.get(SLOT_EGG).is(ItemTags.EGGS) && items.get(SLOT_MUTATED_EGG).isEmpty();
        if (!canInject) {
            boolean wasInjecting = injectProgress > 0;
            injectProgress = 0;
            return wasInjecting;
        }
        if (++injectProgress >= INJECT_TIME) {
            injectProgress = 0;
            mutatingDna = dna;
            mutatingEgg = IncubatorEgg.of(items.get(SLOT_EGG));
            mutateProgress = 0;
            items.get(SLOT_EGG).shrink(1);
            items.set(SLOT_DNA, new ItemStack(BMFItems.SYRINGE.get()));
        }
        return true;
    }

    private boolean tickMutating() {
        if (mutatingDna == null) {
            return false;
        }
        if (mutateProgress < MUTATE_TIME) {
            mutateProgress++;
            return true;
        }
        // Done, waits here until the mutated egg slot is free
        if (!items.get(SLOT_MUTATED_EGG).isEmpty()) {
            return false;
        }
        items.set(SLOT_MUTATED_EGG, BMFItems.mutatedEgg(mutatingDna, mutatingEgg));
        mutatingDna = null;
        mutatingEgg = IncubatorEgg.NONE;
        mutateProgress = 0;
        return true;
    }

    private boolean tickIncubating() {
        Optional<Holder<Item>> spawnEgg = incubationResult();
        if (spawnEgg.isEmpty()) {
            boolean wasIncubating = incubateProgress > 0;
            incubateProgress = 0;
            return wasIncubating;
        }
        if (++incubateProgress >= INCUBATE_TIME) {
            incubateProgress = 0;
            items.get(SLOT_MUTATED_EGG).shrink(1);
            items.get(SLOT_NUTRIENTS).shrink(1);
            ItemStack output = items.get(SLOT_SPAWN_EGG);
            if (output.isEmpty()) {
                items.set(SLOT_SPAWN_EGG, new ItemStack(spawnEgg.get()));
            } else {
                output.grow(1);
            }
        }
        return true;
    }

    // The spawn egg the current mutated egg hatches into, if incubating can run
    private Optional<Holder<Item>> incubationResult() {
        EntityType<?> dna = items.get(SLOT_MUTATED_EGG).get(BMFDataComponents.DNA.get());
        if (dna == null || items.get(SLOT_NUTRIENTS).isEmpty()) {
            return Optional.empty();
        }
        ItemStack output = items.get(SLOT_SPAWN_EGG);
        return SpawnEggItem.byId(dna).filter(egg -> output.isEmpty()
                || output.is(egg) && output.getCount() < output.getMaxStackSize());
    }

    // Shows the egg inside and opens the door while the menu is open or a spawn egg is ready.
    // A mutated egg is shown while it mutates and while it waits in its slot.
    private void updateBlockState() {
        if (level == null || level.isClientSide()) {
            return;
        }
        IncubatorEgg egg;
        if (!items.get(SLOT_SPAWN_EGG).isEmpty()) {
            egg = IncubatorEgg.SPAWN_EGG;
        } else if (currentDna() != null) {
            egg = IncubatorEgg.MUTATED_EGG;
        } else {
            egg = IncubatorEgg.of(items.get(SLOT_EGG));
        }
        boolean open = openCount > 0 || egg == IncubatorEgg.SPAWN_EGG;
        // A finished mutation that waits for its slot to be emptied does not count as working
        boolean working = injectProgress > 0 || mutatingDna != null && mutateProgress < MUTATE_TIME || incubateProgress > 0;

        BlockState state = getBlockState();
        BlockState updated = state.setValue(IncubatorBlock.EGG, egg).setValue(IncubatorBlock.OPEN, open).setValue(IncubatorBlock.WORKING, working);
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
        // The client needs the DNA to color the mutated egg, a block update sends getUpdateTag
        EntityType<?> dna = egg == IncubatorEgg.MUTATED_EGG ? currentDna() : null;
        if (dna != displayDna) {
            displayDna = dna;
            level.sendBlockUpdated(worldPosition, updated, updated, Block.UPDATE_CLIENTS);
        }
    }

    // The DNA of the mutated egg inside: the one mutating right now, or the one waiting in its slot
    private @Nullable EntityType<?> currentDna() {
        return mutatingDna != null ? mutatingDna : items.get(SLOT_MUTATED_EGG).get(BMFDataComponents.DNA.get());
    }

    // On the client: the DNA the mutated egg in the block model is colored with, see IncubatorEggTint
    public @Nullable EntityType<?> displayDna() {
        return displayDna;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        output.storeNullable("DisplayDna", EntityType.CODEC, currentDna());
        return output.buildResult();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateBlockState();
    }

    @Override
    public void startOpen(ContainerUser containerUser) {
        if (!containerUser.getLivingEntity().isSpectator()) {
            openCount++;
            updateBlockState();
        }
    }

    @Override
    public void stopOpen(ContainerUser containerUser) {
        if (!containerUser.getLivingEntity().isSpectator()) {
            openCount = Math.max(0, openCount - 1);
            updateBlockState();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return isValidForSlot(slot, itemStack);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new IncubatorMenu(containerId, inventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        injectProgress = input.getIntOr("InjectProgress", 0);
        mutateProgress = input.getIntOr("MutateProgress", 0);
        incubateProgress = input.getIntOr("IncubateProgress", 0);
        mutatingDna = input.read("MutatingDna", EntityType.CODEC).orElse(null);
        mutatingEgg = input.read("MutatingEgg", IncubatorEgg.CODEC).orElse(IncubatorEgg.NONE);

        // Only part of the update tag: redraw the block when the color of the mutated egg changes
        EntityType<?> dna = input.read("DisplayDna", EntityType.CODEC).orElse(null);
        if (level != null && level.isClientSide() && dna != displayDna) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
        displayDna = dna;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("InjectProgress", injectProgress);
        output.putInt("MutateProgress", mutateProgress);
        output.putInt("IncubateProgress", incubateProgress);
        output.storeNullable("MutatingDna", EntityType.CODEC, mutatingDna);
        output.store("MutatingEgg", IncubatorEgg.CODEC, mutatingEgg);
    }
}

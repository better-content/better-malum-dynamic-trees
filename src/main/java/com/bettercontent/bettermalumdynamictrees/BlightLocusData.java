package com.bettercontent.bettermalumdynamictrees;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.level.saveddata.SavedData;

/** Durable once-only claims for selected Overworld district cells. */
public final class BlightLocusData extends SavedData {
    private static final String DATA_NAME = "better_malum_dynamic_trees_blight_loci";
    private static final String CELLS_KEY = "attempted_cells";
    private final Set<Long> attemptedCells = new HashSet<>();

    public static BlightLocusData get(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlightLocusData::load, BlightLocusData::new, DATA_NAME);
    }

    public boolean claim(long cellKey) {
        if (!attemptedCells.add(cellKey)) {
            return false;
        }
        setDirty();
        return true;
    }

    static BlightLocusData load(CompoundTag tag) {
        BlightLocusData data = new BlightLocusData();
        ListTag cells = tag.getList(CELLS_KEY, 4);
        for (int i = 0; i < cells.size(); i++) {
            data.attemptedCells.add(((LongTag) cells.get(i)).getAsLong());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag cells = new ListTag();
        attemptedCells.stream().sorted().forEach(key -> cells.add(LongTag.valueOf(key)));
        tag.put(CELLS_KEY, cells);
        return tag;
    }
}

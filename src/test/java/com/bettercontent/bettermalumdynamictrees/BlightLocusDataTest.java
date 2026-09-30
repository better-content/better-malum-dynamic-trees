package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class BlightLocusDataTest {
    @Test
    void savedClaimCannotRestartTheNativeEventAfterReload() {
        long selectedCell = ((long) -9 << 32) ^ (17L & 0xffffffffL);
        BlightLocusData initial = new BlightLocusData();

        assertTrue(initial.claim(selectedCell));
        assertFalse(initial.claim(selectedCell));

        CompoundTag saved = initial.save(new CompoundTag());
        BlightLocusData restored = BlightLocusData.load(saved);
        assertFalse(restored.claim(selectedCell));
        assertTrue(restored.claim(selectedCell + 1));
    }
}

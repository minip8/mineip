package io.github.minip8.mineip.util;

import net.minecraft.world.effect.MobEffect;

import java.util.HashSet;
import java.util.Set;

public class PotionEffectsUtil {
    private static final Set<MobEffect> disabledEffects = new HashSet<>();

    public static boolean addDisabledEffect(MobEffect effectId) {
        return disabledEffects.add(effectId);
    }

    public static boolean removeDisabledEffect(MobEffect effectId) {
        return disabledEffects.remove(effectId);
    }

    public static boolean isDisabledEffect(MobEffect effectId) {
        return disabledEffects.contains(effectId);
    }
}

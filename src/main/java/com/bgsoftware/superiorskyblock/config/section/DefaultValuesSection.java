package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class DefaultValuesSection extends SettingsContainerHolder implements SettingsManager.DefaultValues {

    @Override
    public int getIslandSize() {
        return getContainer().defaultIslandSize;
    }

    @Override
    public Map<Key, Integer> getBlockLimits() {
        return getContainer().defaultBlockLimits;
    }

    @Override
    public Map<Key, Integer> getEntityLimits() {
        return getContainer().defaultEntityLimits;
    }

    @Override
    public int getWarpsLimit() {
        return getContainer().defaultWarpsLimit;
    }

    @Override
    public int getTeamLimit() {
        return getContainer().defaultTeamLimit;
    }

    @Override
    public int getCoopLimit() {
        return getContainer().defaultCoopLimit;
    }

    @Override
    public double getCropGrowth() {
        return getContainer().defaultCropGrowth;
    }

    @Override
    public double getSpawnerRates() {
        return getContainer().defaultSpawnerRates;
    }

    @Override
    public double getMobDrops() {
        return getContainer().defaultMobDrops;
    }

    @Override
    public BigDecimal getBankLimit() {
        return getContainer().defaultBankLimit;
    }

    @Override
    public Map<Key, Integer>[] getGenerators() {
        return getGeneratorsUnsafe().clone();
    }

    public Map<Key, Integer>[] getGeneratorsUnsafe() {
        return getContainer().defaultGenerator;
    }

    @Override
    public Map<Integer, Integer> getRoleLimits() {
        return Collections.unmodifiableMap(getContainer().defaultRoleLimits.asMap());
    }

    public Int2IntMapView getRoleLimitsAsView() {
        return getContainer().defaultRoleLimits;
    }

}

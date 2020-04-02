/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.util.Properties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiable<I extends Identifiable<I>> extends AbstractExtendable<I> implements Identifiable<I>, Validable, MultiVariantObject {

    protected final String id;

    protected String name;

    protected boolean fictitious = false;

    protected final Properties properties = new Properties();

    AbstractIdentifiable(String id, String name) {
        this.id = id;
        this.name = name;
    }

    AbstractIdentifiable(String id, String name, boolean fictitious) {
        this(id, name);
        this.fictitious = fictitious;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : id;
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        this.fictitious = fictitious;
        getNetwork().getListeners().notifyUpdate(this, "fictitious", oldValue, fictitious);
    }

    @Override
    public abstract NetworkImpl getNetwork();

    protected abstract String getTypeDescription();

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public Properties.Type getPropertyType(String key) {
        return properties.getPropertyType(key);
    }

    @Override
    public Optional<String> getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Optional<String> getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public OptionalInt getIntegerProperty(String key) {
        return properties.getIntegerProperty(key);
    }

    @Override
    public OptionalInt getIntegerProperty(String key, Integer defaultValue) {
        return properties.getIntegerProperty(key, defaultValue);
    }

    @Override
    public OptionalDouble getDoubleProperty(String key) {
        return properties.getDoubleProperty(key);
    }

    @Override
    public OptionalDouble getDoubleProperty(String key, Double defaultValue) {
        return properties.getDoubleProperty(key, defaultValue);
    }

    @Override
    public Optional<Boolean> getBooleanProperty(String key) {
        return properties.getBooleanProperty(key);
    }

    @Override
    public Optional<Boolean> getBooleanProperty(String key, Boolean defaultValue) {
        return properties.getBooleanProperty(key, defaultValue);
    }

    @Override
    public String setProperty(String key, String value) {
        Pair<Properties.Type, Object> newValue = new ImmutablePair<>(Properties.Type.STRING, value);
        Pair<Properties.Type, Object> oldValue = properties.put(key, newValue);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, newValue);
        } else {
            notifyElementReplaced(key, oldValue, newValue);
        }
        return oldValue != null && Properties.isSameType(oldValue, newValue) ? (String) oldValue.getValue() : null;
    }

    @Override
    public Integer setIntegerProperty(String key, Integer value) {
        Pair<Properties.Type, Object> newValue = new ImmutablePair<>(Properties.Type.INTEGER, value);
        Pair<Properties.Type, Object> oldValue = properties.put(key, newValue);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, newValue);
        } else {
            notifyElementReplaced(key, oldValue, newValue);
        }
        return oldValue != null && Properties.isSameType(oldValue, newValue) ? (Integer) oldValue.getValue() : null;
    }

    @Override
    public Double setDoubleProperty(String key, Double value) {
        Pair<Properties.Type, Object> newValue = new ImmutablePair<>(Properties.Type.DOUBLE, value);
        Pair<Properties.Type, Object> oldValue = properties.put(key, newValue);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, newValue);
        } else {
            notifyElementReplaced(key, oldValue, newValue);
        }
        return oldValue != null && Properties.isSameType(oldValue, newValue) ? (Double) oldValue.getValue() : null;
    }

    @Override
    public Boolean setBooleanProperty(String key, Boolean value) {
        Pair<Properties.Type, Object> newValue = new ImmutablePair<>(Properties.Type.BOOLEAN, value);
        Pair<Properties.Type, Object> oldValue = properties.put(key, newValue);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, newValue);
        } else {
            notifyElementReplaced(key, oldValue, newValue);
        }
        return oldValue != null && Properties.isSameType(oldValue, newValue) ? (Boolean) oldValue.getValue() : null;
    }

    private void notifyElementAdded(String key, Object newValue) {
        getNetwork().getListeners().notifyElementAdded(this, () -> "properties[" + key + "]", newValue);
    }

    private void notifyElementReplaced(String key, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyElementReplaced(this, () -> "properties[" + key + "]", oldValue, newValue);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public Boolean removeProperty(String key) {
        boolean hasProperty = hasProperty(key);
        if (hasProperty) {
            Pair<Properties.Type, Object> oldValue = properties.remove(key);
            getNetwork().getListeners().notifyElementRemoved(this, () -> "properties[" + key + "]", oldValue);
        }
        return hasProperty;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.reduceVariantArraySize(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.deleteVariantArrayElement(index));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.allocateVariantArrayElement(indexes, sourceIndex));
    }
}

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

    protected final Map<String, Pair<Type, Object>> properties = new HashMap<>();

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
    public Type getPropertyType(String key) {
        Pair<Type, Object> val = properties.get(key);
        return val != null ? val.getKey() : null;
    }

    private boolean isValueFound(Pair<Type, Object> val, Type type) {
        return val != null && type.equals(val.getKey());
    }

    @Override
    public String getProperty(String key) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.STRING) ? (String) val.getValue() : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.STRING) ? (String) val.getValue() : defaultValue;
    }

    @Override
    public Integer getIntegerProperty(String key) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.INTEGER) ? (Integer) val.getValue() : null;
    }

    @Override
    public Integer getIntegerProperty(String key, Integer defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.INTEGER) ? (Integer) val.getValue() : defaultValue;
    }

    @Override
    public Double getDoubleProperty(String key) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.DOUBLE) ? (Double) val.getValue() : null;
    }

    @Override
    public Double getDoubleProperty(String key, Double defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.DOUBLE) ? (Double) val.getValue() : defaultValue;
    }

    @Override
    public Boolean getBooleanProperty(String key) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.BOOLEAN) ? (Boolean) val.getValue() : null;
    }

    @Override
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return isValueFound(val, Type.BOOLEAN) ? (Boolean) val.getValue() : defaultValue;
    }

    @Override
    public String setProperty(String key, String value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.STRING, value);
        Pair<Type, Object> oldValue = properties.put(key, val);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, val);
        } else {
            notifyElementReplaced(key, oldValue, val);
        }
        return oldValue != null && val.getKey().equals(oldValue.getKey()) ? (String) oldValue.getValue() : null;
    }

    @Override
    public Integer setIntegerProperty(String key, Integer value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.INTEGER, value);
        Pair<Type, Object> oldValue = properties.put(key, val);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, val);
        } else {
            notifyElementReplaced(key, oldValue, val);
        }
        return oldValue != null && val.getKey().equals(oldValue.getKey()) ? (Integer) oldValue.getValue() : null;
    }

    @Override
    public Double setDoubleProperty(String key, Double value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.DOUBLE, value);
        Pair<Type, Object> oldValue = properties.put(key, val);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, val);
        } else {
            notifyElementReplaced(key, oldValue, val);
        }
        return oldValue != null && val.getKey().equals(oldValue.getKey()) ? (Double) oldValue.getValue() : null;
    }

    @Override
    public Boolean setBooleanProperty(String key, Boolean value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.BOOLEAN, value);
        Pair<Type, Object> oldValue = properties.put(key, val);
        if (Objects.isNull(oldValue)) {
            notifyElementAdded(key, val);
        } else {
            notifyElementReplaced(key, oldValue, val);
        }
        return oldValue != null && val.getKey().equals(oldValue.getKey()) ? (Boolean) oldValue.getValue() : null;
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
            Pair<Type, Object> oldValue = properties.remove(key);
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

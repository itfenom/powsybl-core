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
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiable<I extends Identifiable<I>> extends AbstractExtendable<I> implements Identifiable<I>, Validable, MultiVariantObject {

    protected final String id;

    protected String name;

    protected final Properties properties = new Properties();

    protected final Map<String, Pair<Type, Object>> typedProperties = new HashMap<>();

    AbstractIdentifiable(String id, String name) {
        this.id = id;
        this.name = name;
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
    public abstract NetworkImpl getNetwork();

    protected abstract String getTypeDescription();

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }

    public Properties getProperties() {
        return properties;
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
    public String getProperty(String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Object val = properties.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : null;
    }

    @Override
    public String setProperty(String key, String value) {
        String oldValue = (String) properties.put(key, value);
        if (Objects.isNull(oldValue)) {
            getNetwork().getListeners().notifyElementAdded(this, () -> "properties[" + key + "]", value);
        } else {
            getNetwork().getListeners().notifyElementReplaced(this, () -> "properties[" + key + "]", oldValue, value);
        }
        return oldValue;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }

    @Override
    public boolean hasTypedProperty(String key) {
        return typedProperties.containsKey(key);
    }

    @Override
    public Type getPropertyType(String key) {
        Pair<Type, Object> val = typedProperties.get(key);
        return val != null ? val.getKey() : null;
    }

    @Override
    public String getStringProperty(String key) {
        Pair<Type, Object> val = typedProperties.get(key);
        return (val != null && Type.STRING.equals(val.getKey())) ? (String) val.getValue() : null;
    }

    @Override
    public Integer getIntegerProperty(String key) {
        Pair<Type, Object> val = typedProperties.get(key);
        return (val != null && Type.INTEGER.equals(val.getKey())) ? (Integer) val.getValue() : null;
    }

    @Override
    public Double getDoubleProperty(String key) {
        Pair<Type, Object> val = typedProperties.get(key);
        return (val != null && Type.DOUBLE.equals(val.getKey())) ? (Double) val.getValue() : null;
    }

    @Override
    public Boolean getBooleanProperty(String key) {
        Pair<Type, Object> val = typedProperties.get(key);
        return (val != null && Type.BOOLEAN.equals(val.getKey())) ? (Boolean) val.getValue() : null;
    }

    @Override
    public Pair<Type, Object> getTypedProperty(String key) {
        return typedProperties.get(key);
    }

    @Override
    public String setStringProperty(String key, String value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.STRING, value);
        typedProperties.put(key, val);
        return value;
    }

    @Override
    public Integer setIntegerProperty(String key, Integer value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.INTEGER, value);
        typedProperties.put(key, val);
        return value;
    }

    @Override
    public Double setDoubleProperty(String key, Double value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.DOUBLE, value);
        typedProperties.put(key, val);
        return value;
    }

    @Override
    public Boolean setBooleanProperty(String key, Boolean value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.BOOLEAN, value);
        typedProperties.put(key, val);
        return value;
    }

    @Override
    public Pair<Type, Object> setTypedProperty(String key, Pair<Type, Object> value) {
        return typedProperties.put(key, value);
    }

    @Override
    public Set<String> getTypedPropertyNames() {
        return typedProperties.keySet();
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

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
    public PropertyType getPropertyType(String key) {
        return properties.getPropertyType(key);
    }

    @Override
    public String getStringProperty(String key) {
        return properties.getStringProperty(key);
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        return properties.getStringProperty(key, defaultValue);
    }

    @Override
    public Optional<String> getOptionalStringProperty(String key) {
        return Optional.ofNullable(properties.getStringProperty(key));
    }

    @Override
    public String setStringProperty(String key, String value) {
        notifyElementModification(key, value);
        return properties.putString(key, value);
    }

    @Override
    public Set<String> getStringPropertyNames() {
        return properties.stringKeySet();
    }

    @Override
    public boolean hasStringProperty() {
        return !properties.isStringEmpty();
    }

    @Override
    public boolean hasStringProperty(String key) {
        return properties.containsStringKey(key);
    }

    @Override
    public Integer getIntegerProperty(String key) {
        return properties.getIntegerProperty(key);
    }

    @Override
    public Integer getIntegerProperty(String key, Integer defaultValue) {
        return properties.getIntegerProperty(key, defaultValue);
    }

    @Override
    public OptionalInt getOptionalIntegerProperty(String key) {
        return OptionalInt.of(properties.getIntegerProperty(key));
    }

    @Override
    public Integer setIntegerProperty(String key, Integer value) {
        notifyElementModification(key, value);
        return properties.putInteger(key, value);
    }

    @Override
    public Set<String> getIntegerPropertyNames() {
        return properties.integerKeySet();
    }

    @Override
    public boolean hasIntegerProperty() {
        return !properties.isIntegerEmpty();
    }

    @Override
    public boolean hasIntegerProperty(String key) {
        return properties.containsIntegerKey(key);
    }

    @Override
    public Double getDoubleProperty(String key) {
        return properties.getDoubleProperty(key);
    }

    @Override
    public Double getDoubleProperty(String key, Double defaultValue) {
        return properties.getDoubleProperty(key, defaultValue);
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String key) {
        return OptionalDouble.of(properties.getDoubleProperty(key));
    }

    @Override
    public Double setDoubleProperty(String key, Double value) {
        notifyElementModification(key, value);
        return properties.putDouble(key, value);
    }

    @Override
    public Set<String> getDoublePropertyNames() {
        return properties.doubleKeySet();
    }

    @Override
    public boolean hasDoubleProperty() {
        return !properties.isDoubleEmpty();
    }

    @Override
    public boolean hasDoubleProperty(String key) {
        return properties.containsDoubleKey(key);
    }

    @Override
    public Boolean getBooleanProperty(String key) {
        return properties.getBooleanProperty(key);
    }

    @Override
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        return properties.getBooleanProperty(key, defaultValue);
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String key) {
        return Optional.ofNullable(properties.getBooleanProperty(key));
    }

    @Override
    public Boolean setBooleanProperty(String key, Boolean value) {
        notifyElementModification(key, value);
        return properties.putBoolean(key, value);
    }

    @Override
    public Set<String> getBooleanPropertyNames() {
        return properties.booleanKeySet();
    }

    @Override
    public boolean hasBooleanProperty() {
        return !properties.isBooleanEmpty();
    }

    @Override
    public boolean hasBooleanProperty(String key) {
        return properties.containsBooleanKey(key);
    }

    private void notifyElementModification(String key, Object value) {
        if (properties.containsStringKey(key)) {
            notifyElementReplaced(key, properties.getStringProperty(key), value);
        } else if (properties.containsIntegerKey(key)) {
            notifyElementReplaced(key, properties.getIntegerProperty(key), value);
        } else if (properties.containsDoubleKey(key)) {
            notifyElementReplaced(key, properties.getDoubleProperty(key), value);
        } else if (properties.containsBooleanKey(key)) {
            notifyElementReplaced(key, properties.getBooleanProperty(key), value);
        } else {
            notifyElementAdded(key, value);
        }
    }

    private void notifyElementAdded(String key, Object newValue) {
        getNetwork().getListeners().notifyElementAdded(this, () -> "properties[" + key + "]", newValue);
    }

    private void notifyElementReplaced(String key, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyElementReplaced(this, () -> "properties[" + key + "]", oldValue, newValue);
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

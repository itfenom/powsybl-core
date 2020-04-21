/**
 * Copyright (c) 2020, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Properties {

    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private final Map<String, String> stringProperties = new HashMap<>();
    private final Map<String, Integer> integerProperties = new HashMap<>();
    private final Map<String, Double> doubleProperties = new HashMap<>();
    private final Map<String, Boolean> booleanProperties = new HashMap<>();

    public String putString(String key, String value) {
        integerProperties.remove(key);
        doubleProperties.remove(key);
        booleanProperties.remove(key);
        return stringProperties.put(key, value);
    }

    public String getStringProperty(String key) {
        return stringProperties.get(key);
    }

    public String getStringProperty(String key, String defaultValue) {
        return stringProperties.getOrDefault(key, defaultValue);
    }

    public String removeString(String key) {
        return stringProperties.remove(key);
    }

    public boolean isStringEmpty() {
        return stringProperties.isEmpty();
    }

    public Set<String> stringKeySet() {
        return stringProperties.keySet();
    }

    public boolean containsStringKey(String key) {
        return stringProperties.containsKey(key);
    }

    public Integer putInteger(String key, Integer value) {
        stringProperties.remove(key);
        doubleProperties.remove(key);
        booleanProperties.remove(key);
        return integerProperties.put(key, value);
    }

    public Integer getIntegerProperty(String key) {
        return integerProperties.get(key);
    }

    public Integer getIntegerProperty(String key, Integer defaultValue) {
        return integerProperties.getOrDefault(key, defaultValue);
    }

    public Integer removeInteger(String key) {
        return integerProperties.remove(key);
    }

    public boolean isIntegerEmpty() {
        return integerProperties.isEmpty();
    }

    public Set<String> integerKeySet() {
        return integerProperties.keySet();
    }

    public boolean containsIntegerKey(String key) {
        return integerProperties.containsKey(key);
    }

    public Double putDouble(String key, Double value) {
        integerProperties.remove(key);
        stringProperties.remove(key);
        booleanProperties.remove(key);
        return doubleProperties.put(key, value);
    }

    public Double getDoubleProperty(String key) {
        return doubleProperties.get(key);
    }

    public Double getDoubleProperty(String key, Double defaultValue) {
        return doubleProperties.getOrDefault(key, defaultValue);
    }

    public Double removeDouble(String key) {
        return doubleProperties.remove(key);
    }

    public boolean isDoubleEmpty() {
        return doubleProperties.isEmpty();
    }

    public Set<String> doubleKeySet() {
        return doubleProperties.keySet();
    }

    public boolean containsDoubleKey(String key) {
        return doubleProperties.containsKey(key);
    }

    public Boolean putBoolean(String key, Boolean value) {
        integerProperties.remove(key);
        doubleProperties.remove(key);
        stringProperties.remove(key);
        return booleanProperties.put(key, value);
    }

    public Boolean getBooleanProperty(String key) {
        return booleanProperties.get(key);
    }

    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        return booleanProperties.getOrDefault(key, defaultValue);
    }

    public Boolean removeBoolean(String key) {
        return booleanProperties.remove(key);
    }

    public boolean isBooleanEmpty() {
        return booleanProperties.isEmpty();
    }

    public Set<String> booleanKeySet() {
        return booleanProperties.keySet();
    }

    public boolean containsBooleanKey(String key) {
        return booleanProperties.containsKey(key);
    }

    public PropertyType getPropertyType(String key) {
        PropertyType type;
        if (stringProperties.containsKey(key)) {
            type = PropertyType.STRING;
        } else if (integerProperties.containsKey(key)) {
            type = PropertyType.INTEGER;
        } else if (doubleProperties.containsKey(key)) {
            type = PropertyType.DOUBLE;
        } else if (booleanProperties.containsKey(key)) {
            type = PropertyType.BOOLEAN;
        } else {
            type = null;
        }
        return type;
    }

    public void remove(String key) {
        removeString(key);
        removeInteger(key);
        removeDouble(key);
        removeBoolean(key);
    }

    public void mergeProperties(DanglingLine dl1, DanglingLine dl2) {
        Map<String, String> stringPropertiesDl1 = new HashMap<>();
        Map<String, String> stringPropertiesDl2 = new HashMap<>();
        dl1.getStringPropertyNames().forEach(prop -> stringPropertiesDl1.put(prop, dl1.getStringProperty(prop)));
        dl2.getStringPropertyNames().forEach(prop -> stringPropertiesDl2.put(prop, dl2.getStringProperty(prop)));

        Map<String, Integer> integerPropertiesDl1 = new HashMap<>();
        Map<String, Integer> integerPropertiesDl2 = new HashMap<>();
        dl1.getIntegerPropertyNames().forEach(prop -> integerPropertiesDl1.put(prop, dl1.getIntegerProperty(prop)));
        dl2.getIntegerPropertyNames().forEach(prop -> integerPropertiesDl2.put(prop, dl2.getIntegerProperty(prop)));

        Map<String, Double> doublePropertiesDl1 = new HashMap<>();
        Map<String, Double> doublePropertiesDl2 = new HashMap<>();
        dl1.getDoublePropertyNames().forEach(prop -> doublePropertiesDl1.put(prop, dl1.getDoubleProperty(prop)));
        dl2.getDoublePropertyNames().forEach(prop -> doublePropertiesDl2.put(prop, dl2.getDoubleProperty(prop)));

        Map<String, Boolean> booleanPropertiesDl1 = new HashMap<>();
        Map<String, Boolean> booleanPropertiesDl2 = new HashMap<>();
        dl1.getBooleanPropertyNames().forEach(prop -> booleanPropertiesDl1.put(prop, dl1.getBooleanProperty(prop)));
        dl2.getBooleanPropertyNames().forEach(prop -> booleanPropertiesDl2.put(prop, dl2.getBooleanProperty(prop)));

        mergeTypedProperties(stringPropertiesDl1, stringPropertiesDl2, stringProperties);
        mergeTypedProperties(integerPropertiesDl1, integerPropertiesDl2, integerProperties);
        mergeTypedProperties(doublePropertiesDl1, doublePropertiesDl2, doubleProperties);
        mergeTypedProperties(booleanPropertiesDl1, booleanPropertiesDl2, booleanProperties);
    }

    private <P> void mergeTypedProperties(Map<String, P> propertiesDl1, Map<String, P> propertiesDl2, Map<String, P> properties) {
        Set<String> dl1Properties = propertiesDl1.keySet();
        Set<String> dl2Properties = propertiesDl2.keySet();
        Set<String> commonProperties = Sets.intersection(dl1Properties, dl2Properties);
        Sets.difference(dl1Properties, commonProperties).forEach(prop -> putMergedProperty(prop, propertiesDl1.get(prop), properties));
        Sets.difference(dl2Properties, commonProperties).forEach(prop -> putMergedProperty(prop, propertiesDl2.get(prop), properties));
        commonProperties.forEach(prop -> {
            if (propertiesDl1.get(prop).equals("")) {
                putMergedProperty(prop, propertiesDl2.get(prop), properties);
            } else if (propertiesDl2.get(prop).equals("") || propertiesDl1.get(prop).equals(propertiesDl2.get(prop))) {
                putMergedProperty(prop, propertiesDl1.get(prop), properties);
            } else {
                LOGGER.error("Inconsistencies of property type for '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line",
                    prop, propertiesDl1.get(prop), propertiesDl2.get(prop));
            }
        });
    }

    private <P> void putMergedProperty(String key, P value, Map<String, P> properties) {
        boolean isPropertyRemoved = false;

        isPropertyRemoved = !Objects.isNull(stringProperties.remove(key));
        isPropertyRemoved = isPropertyRemoved || !Objects.isNull(integerProperties.remove(key));
        isPropertyRemoved = isPropertyRemoved || !Objects.isNull(doubleProperties.remove(key));
        isPropertyRemoved = isPropertyRemoved || !Objects.isNull(booleanProperties.remove(key));

        if (!isPropertyRemoved) {
            properties.put(key, value);
        }
    }
}

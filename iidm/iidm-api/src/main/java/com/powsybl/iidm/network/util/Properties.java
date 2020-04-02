package com.powsybl.iidm.network.util;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.DanglingLine;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Properties {

    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private static final String INCONSISTENCY_WARN_EMPTY_SIDE_1 = "Inconsistencies of property '{}' between both sides of merged line. Side 1 is empty, keeping side 2 value '{}'";
    private static final String INCONSISTENCY_WARN_EMPTY_SIDE_2 = "Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'";
    private static final String INCONSISTENCY_ERROR_BOTH_SIDES = "Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line";

    private final Map<String, Pair<Type, Object>> propertyList = new HashMap<>();

    public enum Type {
        STRING, INTEGER, DOUBLE, BOOLEAN;
    }

    public Pair<Type, Object> get(String key) {
        return propertyList.get(key);
    }

    public Pair<Type, Object> put(String key, Pair<Type, Object> value) {
        return propertyList.put(key, value);
    }

    public Pair<Type, Object> remove(String key) {
        return propertyList.remove(key);
    }

    public boolean isEmpty() {
        return propertyList.isEmpty();
    }

    public boolean containsKey(String key) {
        return propertyList.containsKey(key);
    }

    public Map<String, Pair<Type, Object>> getPropertyList() {
        return Collections.unmodifiableMap(propertyList);
    }

    public Type getPropertyType(String key) {
        Pair<Type, Object> val = propertyList.get(key);
        return val != null ? val.getKey() : null;
    }

    public Optional<String> getProperty(String key) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        Optional<String> returnValue;
        if (val == null || val.getValue() == null) {
            returnValue = Optional.empty();
        } else {
            returnValue = Optional.of((String) val.getValue());
        }
        return returnValue;
    }

    public Optional<String> getProperty(String key, String defaultValue) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        Optional<String> returnValue;
        if (val == null && defaultValue == null) {
            returnValue = Optional.empty();
        } else {
            returnValue = Optional.of(isValueFound(val, Type.STRING) ? (String) val.getValue() : defaultValue);
        }
        return returnValue;
    }

    public OptionalInt getIntegerProperty(String key) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        OptionalInt returnValue;
        if (val == null || val.getValue() == null) {
            returnValue = OptionalInt.empty();
        } else {
            returnValue = OptionalInt.of((Integer) val.getValue());
        }
        return returnValue;
    }

    public OptionalInt getIntegerProperty(String key, Integer defaultValue) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        OptionalInt returnValue;
        if (val == null && defaultValue == null) {
            returnValue = OptionalInt.empty();
        } else {
            returnValue = OptionalInt.of(isValueFound(val, Type.INTEGER) ? (Integer) val.getValue() : defaultValue);
        }
        return returnValue;
    }

    public OptionalDouble getDoubleProperty(String key) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        OptionalDouble returnValue;
        if (val == null || val.getValue() == null) {
            returnValue = OptionalDouble.empty();
        } else {
            returnValue = OptionalDouble.of((Double) val.getValue());
        }
        return returnValue;
    }

    public OptionalDouble getDoubleProperty(String key, Double defaultValue) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        OptionalDouble returnValue;
        if (val == null && defaultValue == null) {
            returnValue = OptionalDouble.empty();
        } else {
            returnValue = OptionalDouble.of(isValueFound(val, Type.DOUBLE) ? (Double) val.getValue() : defaultValue);
        }
        return returnValue;
    }

    public Optional<Boolean> getBooleanProperty(String key) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        Optional<Boolean> returnValue;
        if (val == null || val.getValue() == null) {
            returnValue = Optional.empty();
        } else {
            returnValue = Optional.of((Boolean) val.getValue());
        }
        return returnValue;
    }

    public Optional<Boolean> getBooleanProperty(String key, Boolean defaultValue) {
        Pair<Properties.Type, Object> val = propertyList.get(key);
        Optional<Boolean> returnValue;
        if (val == null && defaultValue == null) {
            returnValue = Optional.empty();
        } else {
            returnValue = Optional.of(isValueFound(val, Type.BOOLEAN) ? (Boolean) val.getValue() : defaultValue);
        }
        return returnValue;
    }

    private boolean isValueFound(Pair<Type, Object> value, Type type) {
        return value != null && type.equals(value.getKey());
    }

    public static boolean isSameType(Pair<Type, Object> oldValue, Pair<Type, Object> newValue) {
        return newValue.getKey().equals(oldValue.getKey());
    }

    public Set<String> keySet() {
        return propertyList.keySet();
    }

    private Object getProperty(DanglingLine dl, String prop) {
        Object property = null;
        switch (dl.getPropertyType(prop)) {
            case STRING:
                property = dl.getProperty(prop);
                break;
            case INTEGER:
                property = dl.getIntegerProperty(prop);
                break;
            case DOUBLE:
                property = dl.getDoubleProperty(prop);
                break;
            case BOOLEAN:
                property = dl.getBooleanProperty(prop);
                break;
            default:
                break;
        }
        return property;
    }

    private void setProperty(Map<String, Pair<Type, Object>> properties, DanglingLine dl, String prop) {
        Type type = dl.getPropertyType(prop);
        switch (type) {
            case STRING:
                properties.put(prop, new ImmutablePair<>(type, ((Optional) getProperty(dl, prop)).get()));
                break;
            case INTEGER:
                properties.put(prop, new ImmutablePair<>(type, ((OptionalInt) getProperty(dl, prop)).getAsInt()));
                break;
            case DOUBLE:
                properties.put(prop, new ImmutablePair<>(type, ((OptionalDouble) getProperty(dl, prop)).getAsDouble()));
                break;
            case BOOLEAN:
                properties.put(prop, new ImmutablePair<>(type, ((Optional) getProperty(dl, prop)).get()));
                break;
            default:
                break;
        }
    }

    public void mergeProperty(DanglingLine dl1, DanglingLine dl2, String prop, Type type) {
        Object dl1Property = getProperty(dl1, prop);
        Object dl2Property = getProperty(dl2, prop);

        if (Objects.equals(dl1Property, dl2Property)) {
            if (dl1Property instanceof Optional && ((Optional) dl1Property).isPresent()) {
                propertyList.put(prop, new ImmutablePair<>(type, ((Optional) dl1Property).get()));
            } else if (dl1Property instanceof OptionalInt && ((OptionalInt) dl1Property).isPresent()) {
                propertyList.put(prop, new ImmutablePair<>(type, ((OptionalInt) dl1Property).getAsInt()));
            } else if (dl1Property instanceof OptionalDouble && ((OptionalDouble) dl1Property).isPresent()) {
                propertyList.put(prop, new ImmutablePair<>(type, ((OptionalDouble) dl1Property).getAsDouble()));
            }
        } else if (dl1Property instanceof Optional && dl2Property instanceof Optional && ((Optional) dl1Property).isPresent() &&
            (!((Optional) dl2Property).isPresent() || Type.STRING.equals(type) &&  ((Optional) dl2Property).get().toString().isEmpty())) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, ((Optional) dl1Property).get());
            propertyList.put(prop, new ImmutablePair<>(type, ((Optional) dl1Property).get()));
        } else if (dl1Property instanceof Optional && dl2Property instanceof Optional && ((Optional) dl2Property).isPresent() &&
            (!((Optional) dl1Property).isPresent() || Type.STRING.equals(type) && ((Optional) dl1Property).get().toString().isEmpty())) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, ((Optional) dl2Property).get());
            propertyList.put(prop, new ImmutablePair<>(type, ((Optional) dl2Property).get()));
        } else if (dl1Property instanceof OptionalInt && dl2Property instanceof OptionalInt && ((OptionalInt) dl1Property).isPresent() && !((OptionalInt) dl2Property).isPresent()) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, ((OptionalInt) dl1Property).getAsInt());
            propertyList.put(prop, new ImmutablePair<>(type, ((OptionalInt) dl1Property).getAsInt()));
        } else if (dl1Property instanceof OptionalInt && dl2Property instanceof OptionalInt && !((OptionalInt) dl1Property).isPresent() && ((OptionalInt) dl2Property).isPresent()) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, ((OptionalInt) dl2Property).getAsInt());
            propertyList.put(prop, new ImmutablePair<>(type, ((OptionalInt) dl2Property).getAsInt()));
        } else if (dl1Property instanceof OptionalDouble && dl2Property instanceof OptionalDouble && ((OptionalDouble) dl1Property).isPresent() && !((OptionalDouble) dl2Property).isPresent()) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, ((OptionalDouble) dl1Property).getAsDouble());
            propertyList.put(prop, new ImmutablePair<>(type, ((OptionalDouble) dl1Property).getAsDouble()));
        } else if (dl1Property instanceof OptionalDouble && dl2Property instanceof OptionalDouble && !((OptionalDouble) dl1Property).isPresent() && ((OptionalDouble) dl2Property).isPresent()) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, ((OptionalDouble) dl2Property).getAsDouble());
            propertyList.put(prop, new ImmutablePair<>(type, ((OptionalDouble) dl2Property).getAsDouble()));
        } else {
            LOGGER.error(INCONSISTENCY_ERROR_BOTH_SIDES, prop, dl1Property, dl2Property);
        }
    }

    public void mergeProperties(DanglingLine dl1, DanglingLine dl2) {
        Set<String> dl1Properties = dl1.getPropertyNames();
        Set<String> dl2Properties = dl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(dl1Properties, dl2Properties);
        Sets.difference(dl1Properties, commonProperties).forEach(prop -> setProperty(propertyList, dl1, prop));
        Sets.difference(dl2Properties, commonProperties).forEach(prop -> setProperty(propertyList, dl2, prop));
        commonProperties.forEach(prop -> {
            if (dl1.getPropertyType(prop).equals(dl2.getPropertyType(prop))) {
                mergeProperty(dl1, dl2, prop, dl1.getPropertyType(prop));
            } else {
                LOGGER.error("Inconsistencies of property type for '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line",
                    prop, dl1.getPropertyType(prop), dl2.getPropertyType(prop));
            }
        });
    }
}

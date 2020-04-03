package com.powsybl.iidm.network.util;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
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

    public static class Property {
        private final Pair<Type, Object> entry;

        public Property(Type type, Object value) {
            entry = new ImmutablePair<>(type, value);
        }

        public Property(Object value) {
            Type type;
            if (value instanceof String) {
                type = Type.STRING;
            } else if (value instanceof Boolean) {
                type = Type.BOOLEAN;
            } else if (value instanceof Integer) {
                type = Type.INTEGER;
            } else if (value instanceof Double) {
                type = Type.DOUBLE;
            } else {
                throw new PowsyblException("Invalid property type, only available types are : STRING, INTEGER, DOUBLE, BOOLEAN");
            }
            entry = new ImmutablePair<>(type, value);
        }

        public Type getType() {
            return entry.getKey();
        }

        public Object getValue() {
            return entry.getValue();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Property property = (Property) o;
            return Objects.equals(entry, property.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entry);
        }

        @Override
        public String toString() {
            return "Property{" +
                    "entry=" + entry +
                    '}';
        }
    }

    private final Map<String, Property> propertyList = new HashMap<>();

    public enum Type {
        STRING, INTEGER, DOUBLE, BOOLEAN;
    }

    public Property put(String key, Property value) {
        return propertyList.put(key, value);
    }

    public Property remove(String key) {
        return propertyList.remove(key);
    }

    public boolean isEmpty() {
        return propertyList.isEmpty();
    }

    public boolean containsKey(String key) {
        return propertyList.containsKey(key);
    }

    public Map<String, Property> getPropertyList() {
        return Collections.unmodifiableMap(propertyList);
    }

    public Type getPropertyType(String key) {
        Property val = propertyList.get(key);
        return val != null ? val.getType() : null;
    }

    public <P> Optional<P> getProperty(String key) {
        Property val = propertyList.get(key);
        P returnValue = null;
        if (val != null && val.getValue() != null) {
            returnValue = (P) val.getValue();
        }
        return Optional.ofNullable(returnValue);
    }

    public <P> Optional<P> getProperty(String key, P defaultValue) {
        Optional<P> val = getProperty(key);
        return Optional.of(val.orElse(defaultValue));
    }

    public static boolean isSameType(Properties.Property oldValue, Properties.Property newValue) {
        return newValue.getType() == oldValue.getType();
    }

    public Set<String> keySet() {
        return propertyList.keySet();
    }

    public void mergeProperty(DanglingLine dl1, DanglingLine dl2, String name, Type type) {
        Optional dl1Property = dl1.getProperty(name);
        Optional dl2Property = dl2.getProperty(name);

        if (Objects.equals(dl1Property, dl2Property)) {
            if (dl1Property.isPresent()) {
                put(name, new Property(type, dl1Property.get()));
            }
        } else if (dl1Property.isPresent() && (!dl2Property.isPresent() ||
            dl2.getPropertyType(name) == Type.STRING && ((String) dl2Property.get()).isEmpty())) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, name, dl1Property.get());
            put(name, new Property(type, dl1Property.get()));
        } else if (dl2Property.isPresent() && (!dl1Property.isPresent() ||
            dl2.getPropertyType(name) == Type.STRING && ((String) dl1Property.get()).isEmpty())) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, name, dl2Property.get());
            put(name, new Property(type, dl2Property.get()));
        } else {
            LOGGER.error(INCONSISTENCY_ERROR_BOTH_SIDES, name, dl1Property, dl2Property);
        }
    }

    private void setDanglingLineProperty(Map<String, Property> properties, DanglingLine dl, String name) {
        Type type = dl.getPropertyType(name);
        dl.getProperty(name).ifPresent(o -> properties.put(name, new Property(type, o)));
    }

    public void mergeProperties(DanglingLine dl1, DanglingLine dl2) {
        Set<String> dl1Properties = dl1.getPropertyNames();
        Set<String> dl2Properties = dl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(dl1Properties, dl2Properties);
        Sets.difference(dl1Properties, commonProperties).forEach(prop -> setDanglingLineProperty(propertyList, dl1, prop));
        Sets.difference(dl2Properties, commonProperties).forEach(prop -> setDanglingLineProperty(propertyList, dl2, prop));
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

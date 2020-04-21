/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.*;

import com.powsybl.commons.extensions.Extendable;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Identifiable<I extends Identifiable<I>> extends Extendable<I> {

    enum PropertyType {
        STRING("string"),
        INTEGER("integer"),
        DOUBLE("double"),
        BOOLEAN("boolean");

        private String name;
        PropertyType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Get the network associated to the object.
     */
    Network getNetwork();

    /**
     * Get the unique identifier of the object.
     */
    String getId();

    /**
     * Get an the (optional) name  of the object.
     */
    String getName();

    /**
     * Check that this object has some properties.
     *
     * @deprecated Use {@link #hasStringProperty()} instead.
     */
    @Deprecated
    default boolean hasProperty() {
        return hasStringProperty();
    }

    /**
     * Get the type of the property.
     */
    PropertyType getPropertyType(String key);

    /**
     * Check that this object has some string properties.
     */
    boolean hasStringProperty();

    /**
     * Check that this object has some integer properties.
     */
    boolean hasIntegerProperty();

    /**
     * Check that this object has some double properties.
     */
    boolean hasDoubleProperty();

    /**
     * Check that this object has some boolean properties.
     */
    boolean hasBooleanProperty();

    /**
     * Get properties associated to the object.
     *
     * @deprecated Use {@link #getProperty(String)} & {@link #setProperty(String, String)} instead.
     */
    @Deprecated
    default Properties getProperties() {
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Check that this object has property with specified name.
     *
     * @deprecated Use {@link #hasStringProperty(String)} instead.
     */
    default boolean hasProperty(String key) {
        return hasStringProperty(key);
    }

    /**
     * Check that this object has string property with specified name.
     */
    boolean hasStringProperty(String key);

    /**
     * Check that this object has integer property with specified name.
     */
    boolean hasIntegerProperty(String key);

    /**
     * Check that this object has double property with specified name.
     */
    boolean hasDoubleProperty(String key);

    /**
     * Check that this object has boolean property with specified name.
     */
    boolean hasBooleanProperty(String key);

    /**
     * Get property associated to specified key.
     *
     * @deprecated Use {@link #getStringProperty(String)} instead.
     */
    @Deprecated
    default String getProperty(String key) {
        return getStringProperty(key);
    }

    /**
     * Get property associated to specified key, with default value.
     *
     * @deprecated Use {@link #getStringProperty(String, String)} instead.
     */
    @Deprecated
    default String getProperty(String key, String defaultValue) {
        return getStringProperty(key, defaultValue);
    }

    /**
     * Set property value associated to specified key.
     *
     * @deprecated Use {@link #setStringProperty(String, String)} instead.
     */
    @Deprecated
    default String setProperty(String key, String value) {
        return setStringProperty(key, value);
    }

    /**
     * Get string property associated to specified key.
     */
    String getStringProperty(String key);

    /**
     * Get string property associated to specified key, with default value.
     */
    String getStringProperty(String key, String defaultValue);

    /**
     * Get string property associated to specified key.
     */
    Optional<String> getOptionalStringProperty(String key);

    /**
     * Set string property value associated to specified key.
     */
    String setStringProperty(String key, String value);

    /**
     * Get integer property associated to specified key.
     */
    Integer getIntegerProperty(String key);

    /**
     * Get integer property associated to specified key, with default value.
     */
    Integer getIntegerProperty(String key, Integer defaultValue);

    /**
     * Get integer property associated to specified key.
     */
    OptionalInt getOptionalIntegerProperty(String key);

    /**
     * Set integer property value associated to specified key.
     */
    Integer setIntegerProperty(String key, Integer value);

    /**
     * Get double property associated to specified key.
     */
    Double getDoubleProperty(String key);

    /**
     * Get double property associated to specified key, with default value.
     */
    Double getDoubleProperty(String key, Double defaultValue);

    /**
     * Get double property associated to specified key.
     */
    OptionalDouble getOptionalDoubleProperty(String key);

    /**
     * Set double property value associated to specified key.
     */
    Double setDoubleProperty(String key, Double value);

    /**
     * Get boolean property associated to specified key.
     */
    Boolean getBooleanProperty(String key);

    /**
     * Get boolean property associated to specified key, with default value.
     */
    Boolean getBooleanProperty(String key, Boolean defaultValue);

    /**
     * Get boolean property associated to specified key.
     */
    Optional<Boolean> getOptionalBooleanProperty(String key);

    /**
     * Set boolean property value associated to specified key.
     */
    Boolean setBooleanProperty(String key, Boolean value);

    /**
     * Get properties key values.
     *
     * @deprecated Use {@link #getStringPropertyNames()} instead.
     */
    @Deprecated
    default Set<String> getPropertyNames() {
        return getStringPropertyNames();
    }

    /**
     * Get string properties key values.
     */
    Set<String> getStringPropertyNames();

    /**
     * Get integer properties key values.
     */
    Set<String> getIntegerPropertyNames();

    /**
     * Get double properties key values.
     */
    Set<String> getDoublePropertyNames();

    /**
     * Get boolean properties key values.
     */
    Set<String> getBooleanPropertyNames();

    /**
     * Get the fictitious status
     */
    default boolean isFictitious() {
        return false;
    }

    /**
     * Set the fictitious status
     */
    default void setFictitious(boolean fictitious) {
        throw new UnsupportedOperationException();
    }
}

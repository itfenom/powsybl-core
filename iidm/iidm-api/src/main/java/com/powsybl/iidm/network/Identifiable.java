/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Set;

import com.powsybl.commons.extensions.Extendable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Properties;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Identifiable<I extends Identifiable<I>> extends Extendable<I> {

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
     */
    boolean hasProperty();

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
     * @deprecated Use {@link #hasTypedProperty(String)} instead.
     */
    @Deprecated
    default boolean hasProperty(String key) {
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Get property associated to specified key.
     *
     * @deprecated Use {@link #getStringProperty(String)}, {@link #getIntegerProperty(String)},
     *  {@link #getDoubleProperty(String)} or {@link #getBooleanProperty(String)} instead.
     */
    @Deprecated
    default String getProperty(String key) {
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Get property associated to specified key, with default value.
     *
     * @deprecated Use {@link #getStringProperty(String)}, {@link #getIntegerProperty(String)},
     *  {@link #getDoubleProperty(String)} or {@link #getBooleanProperty(String)} instead.
     */
    @Deprecated
    default String getProperty(String key, String defaultValue) {
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Set property value associated to specified key.
     *
     * @deprecated Use {@link #setStringProperty(String, String)}, {@link #setIntegerProperty(String, Integer)},
     *  {@link #setDoubleProperty(String, Double)}, {@link #setBooleanProperty(String, Boolean)} or {@link #setTypedProperty(String, Pair)} instead.
     */
    @Deprecated
    default String setProperty(String key, String value) {
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Get properties key values.
     *
     * @deprecated Use {@link #getTypedPropertyNames()} instead.
     */
    @Deprecated
    default Set<String> getPropertyNames() {
        throw new UnsupportedOperationException("Deprecated");
    }

    public enum Type {
        STRING, INTEGER, DOUBLE, BOOLEAN;
    }

    /**
     * Check that this object has property with specified name.
     */
    boolean hasTypedProperty(String key);

    /**
     * Get the type of the property associated to specified key
     */
    Type getPropertyType(String key);

    /**
     * Get property associated to specified key as a String.
     */
    String getStringProperty(String key);

    /**
     * Get property associated to specified key as an Integer.
     */
    Integer getIntegerProperty(String key);

    /**
     * Get property associated to specified key as a Double.
     */
    Double getDoubleProperty(String key);

    /**
     * Get property associated to specified key as a Boolean.
     */
    Boolean getBooleanProperty(String key);

    /**
     * Get property associated to specified key.
     */
    Pair<Type, Object> getTypedProperty(String key);

    /**
     * Set String property value associated to specified key.
     */
    String setStringProperty(String key, String value);

    /**
     * Set Integer property value associated to specified key.
     */
    Integer setIntegerProperty(String key, Integer value);

    /**
     * Set Double property value associated to specified key.
     */
    Double setDoubleProperty(String key, Double value);

    /**
     * Set Boolean property value associated to specified key.
     */
    Boolean setBooleanProperty(String key, Boolean value);

    /**
     * Set property value associated to specified key.
     */
    Pair<Type, Object> setTypedProperty(String key, Pair<Type, Object> value);

    /**
     * Get properties key values.
     */
    Set<String> getTypedPropertyNames();
}

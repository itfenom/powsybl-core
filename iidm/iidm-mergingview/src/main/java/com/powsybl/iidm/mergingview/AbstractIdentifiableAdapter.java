/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Identifiable;

import java.util.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractIdentifiableAdapter<I extends Identifiable<I>> extends AbstractAdapter<I> implements Identifiable<I> {

    protected AbstractIdentifiableAdapter(final I delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public MergingView getNetwork() {
        return getIndex().getView();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getId() {
        return getDelegate().getId();
    }

    @Override
    public Optional<String> getOptionalName() {
        return getDelegate().getOptionalName();
    }

    @Override
    public PropertyType getPropertyType(String key) {
        return getDelegate().getPropertyType(key);
    }

    @Override
    public boolean hasStringProperty() {
        return getDelegate().hasStringProperty();
    }

    @Override
    public boolean hasIntegerProperty() {
        return getDelegate().hasIntegerProperty();
    }

    @Override
    public boolean hasDoubleProperty() {
        return getDelegate().hasDoubleProperty();
    }

    @Override
    public boolean hasBooleanProperty() {
        return getDelegate().hasBooleanProperty();
    }

    @Override
    public boolean hasStringProperty(String key) {
        return getDelegate().hasStringProperty(key);
    }

    @Override
    public boolean hasIntegerProperty(String key) {
        return getDelegate().hasIntegerProperty(key);
    }

    @Override
    public boolean hasDoubleProperty(String key) {
        return getDelegate().hasDoubleProperty(key);
    }

    @Override
    public boolean hasBooleanProperty(String key) {
        return getDelegate().hasBooleanProperty(key);
    }

    @Override
    public String getStringProperty(String key) {
        return getDelegate().getStringProperty(key);
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        return getDelegate().getStringProperty(key, defaultValue);
    }

    @Override
    public Optional<String> getOptionalStringProperty(String key) {
        return getDelegate().getOptionalStringProperty(key);
    }

    @Override
    public String setStringProperty(String key, String value) {
        return getDelegate().setStringProperty(key, value);
    }

    @Override
    public Integer getIntegerProperty(String key) {
        return getDelegate().getIntegerProperty(key);
    }

    @Override
    public Integer getIntegerProperty(String key, Integer defaultValue) {
        return getDelegate().getIntegerProperty(key, defaultValue);
    }

    @Override
    public OptionalInt getOptionalIntegerProperty(String key) {
        return getDelegate().getOptionalIntegerProperty(key);
    }

    @Override
    public Integer setIntegerProperty(String key, Integer value) {
        return getDelegate().setIntegerProperty(key, value);
    }

    @Override
    public Double getDoubleProperty(String key) {
        return getDelegate().getDoubleProperty(key);
    }

    @Override
    public Double getDoubleProperty(String key, Double defaultValue) {
        return getDelegate().getDoubleProperty(key, defaultValue);
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String key) {
        return getDelegate().getOptionalDoubleProperty(key);
    }

    @Override
    public Double setDoubleProperty(String key, Double value) {
        return getDelegate().setDoubleProperty(key, value);
    }

    @Override
    public Boolean getBooleanProperty(String key) {
        return getDelegate().getBooleanProperty(key);
    }

    @Override
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        return getDelegate().getBooleanProperty(key, defaultValue);
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String key) {
        return getDelegate().getOptionalBooleanProperty(key);
    }

    @Override
    public Boolean setBooleanProperty(String key, Boolean value) {
        return getDelegate().setBooleanProperty(key, value);
    }

    @Override
    public Set<String> getStringPropertyNames() {
        return getDelegate().getStringPropertyNames();
    }

    @Override
    public Set<String> getIntegerPropertyNames() {
        return getDelegate().getIntegerPropertyNames();
    }

    @Override
    public Set<String> getDoublePropertyNames() {
        return getDelegate().getDoublePropertyNames();
    }

    @Override
    public Set<String> getBooleanPropertyNames() {
        return getDelegate().getBooleanPropertyNames();
    }

    @Override
    public boolean isFictitious() {
        return getDelegate().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        getDelegate().setFictitious(fictitious);
    }

    @Override
    public <E extends Extension<I>> void addExtension(final Class<? super E> type, final E extension) {
        getDelegate().addExtension(type, extension);
    }

    @Override
    public <E extends Extension<I>> E getExtension(final Class<? super E> type) {
        return getDelegate().getExtension(type);
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(final String name) {
        return getDelegate().getExtensionByName(name);
    }

    @Override
    public <E extends Extension<I>> boolean removeExtension(final Class<E> type) {
        return getDelegate().removeExtension(type);
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        return getDelegate().getExtensions();
    }

    @Override
    public <E extends Extension<I>, B extends ExtensionAdder<I, E>> B newExtension(Class<B> type) {
        return getDelegate().newExtension(type);
    }
}

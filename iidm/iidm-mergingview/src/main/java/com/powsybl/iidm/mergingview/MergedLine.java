/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergedLine implements Line {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergedLine.class);

    private static final String UNEXPECTED_SIDE_VALUE = "Unexpected side value: ";

    private final MergingViewIndex index;

    private final DanglingLine dl1;

    private final DanglingLine dl2;

    private String id;

    private final String name;

    private final Map<String, Pair<Type, Object>> properties = new HashMap<>();

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.dl1 = Objects.requireNonNull(dl1, "dangling line 1 is null");
        this.dl2 = Objects.requireNonNull(dl2, "dangling line 2 is null");
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildId(dl1, dl2), index::contains) : buildId(dl1, dl2);
        this.name = buildName(dl1, dl2);
        mergeProperties(dl1, dl2);
    }

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2) {
        this(index, dl1, dl2, false);
    }

    private static String buildId(final DanglingLine dl1, final DanglingLine dl2) {
        String id;
        if (dl1.getId().compareTo(dl2.getId()) < 0) {
            id = dl1.getId() + " + " + dl2.getId();
        } else {
            id = dl2.getId() + " + " + dl1.getId();
        }
        return id;
    }

    private static String buildName(final DanglingLine dl1, final DanglingLine dl2) {
        String name;
        int compareResult = dl1.getName().compareTo(dl2.getName());
        if (compareResult == 0) {
            name = dl1.getName();
        } else if (compareResult < 0) {
            name = dl1.getName() + " + " + dl2.getName();
        } else {
            name = dl2.getName() + " + " + dl1.getName();
        }
        return name;
    }

    private static final String INCONSISTENCY_WARN_EMPTY_SIDE_1 = "Inconsistencies of property '{}' between both sides of merged line. Side 1 is empty, keeping side 2 value '{}'";
    private static final String INCONSISTENCY_WARN_EMPTY_SIDE_2 = "Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'";
    private static final String INCONSISTENCY_ERROR_BOTH_SIDES = "Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line";

    private void mergeStringProperty(String prop) {
        if (Objects.equals(dl1.getProperty(prop), dl2.getProperty(prop))) {
            setProperty(prop, dl1.getProperty(prop));
        } else if (dl1.getProperty(prop).isEmpty()) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, dl2.getProperty(prop));
            setProperty(prop, dl2.getProperty(prop));
        } else if (dl2.getProperty(prop).isEmpty()) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, dl1.getProperty(prop));
            setProperty(prop, dl1.getProperty(prop));
        } else {
            LOGGER.error(INCONSISTENCY_ERROR_BOTH_SIDES, prop, dl1.getProperty(prop), dl2.getProperty(prop));
        }
    }

    private void mergeIntegerProperty(String prop) {
        if (Objects.equals(dl1.getIntegerProperty(prop), dl2.getIntegerProperty(prop))) {
            setIntegerProperty(prop, dl1.getIntegerProperty(prop));
        } else if (dl1.getIntegerProperty(prop) == null) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, dl2.getIntegerProperty(prop));
            setIntegerProperty(prop, dl2.getIntegerProperty(prop));
        } else if (dl2.getIntegerProperty(prop) == null) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, dl1.getIntegerProperty(prop));
            setIntegerProperty(prop, dl1.getIntegerProperty(prop));
        } else {
            LOGGER.error(INCONSISTENCY_ERROR_BOTH_SIDES, prop, dl1.getIntegerProperty(prop), dl2.getIntegerProperty(prop));
        }
    }

    private void mergeDoubleProperty(String prop) {
        if (Objects.equals(dl1.getDoubleProperty(prop), dl2.getDoubleProperty(prop))) {
            setDoubleProperty(prop, dl1.getDoubleProperty(prop));
        } else if (dl1.getDoubleProperty(prop) == null) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, dl2.getDoubleProperty(prop));
            setDoubleProperty(prop, dl2.getDoubleProperty(prop));
        } else if (dl2.getDoubleProperty(prop) == null) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, dl1.getDoubleProperty(prop));
            setDoubleProperty(prop, dl1.getDoubleProperty(prop));
        } else {
            LOGGER.error(INCONSISTENCY_ERROR_BOTH_SIDES, prop, dl1.getDoubleProperty(prop), dl2.getDoubleProperty(prop));
        }
    }

    private void mergeBooleanProperty(String prop) {
        if (Objects.equals(dl1.getBooleanProperty(prop), dl2.getBooleanProperty(prop))) {
            setBooleanProperty(prop, dl1.getBooleanProperty(prop));
        } else if (dl1.getBooleanProperty(prop) == null) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_1, prop, dl2.getBooleanProperty(prop));
            setBooleanProperty(prop, dl2.getBooleanProperty(prop));
        } else if (dl2.getBooleanProperty(prop) == null) {
            LOGGER.warn(INCONSISTENCY_WARN_EMPTY_SIDE_2, prop, dl1.getBooleanProperty(prop));
            setBooleanProperty(prop, dl1.getBooleanProperty(prop));
        } else {
            LOGGER.error(INCONSISTENCY_ERROR_BOTH_SIDES, prop, dl1.getBooleanProperty(prop), dl2.getBooleanProperty(prop));
        }
    }

    private void mergeProperties(DanglingLine dl1, DanglingLine dl2) {
        Set<String> dl1Properties = dl1.getPropertyNames();
        Set<String> dl2Properties = dl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(dl1Properties, dl2Properties);
        Sets.difference(dl1Properties, commonProperties).forEach(prop -> {
            switch (dl1.getPropertyType(prop)) {
                case STRING:
                    setProperty(prop, dl1.getProperty(prop));
                    break;
                case INTEGER:
                    setIntegerProperty(prop, dl1.getIntegerProperty(prop));
                    break;
                case DOUBLE:
                    setDoubleProperty(prop, dl1.getDoubleProperty(prop));
                    break;
                case BOOLEAN:
                    setBooleanProperty(prop, dl1.getBooleanProperty(prop));
                    break;
            }
        });
        Sets.difference(dl2Properties, commonProperties).forEach(prop -> {
            switch (dl2.getPropertyType(prop)) {
                case STRING:
                    setProperty(prop, dl2.getProperty(prop));
                    break;
                case INTEGER:
                    setIntegerProperty(prop, dl2.getIntegerProperty(prop));
                    break;
                case DOUBLE:
                    setDoubleProperty(prop, dl2.getDoubleProperty(prop));
                    break;
                case BOOLEAN:
                    setBooleanProperty(prop, dl2.getBooleanProperty(prop));
                    break;
            }
        });
        commonProperties.forEach(prop -> {
            if (dl1.getPropertyType(prop).equals(dl2.getPropertyType(prop))) {
                switch (dl1.getPropertyType(prop)) {
                    case STRING:
                        mergeStringProperty(prop);
                        break;
                    case INTEGER:
                        mergeIntegerProperty(prop);
                        break;
                    case DOUBLE:
                        mergeDoubleProperty(prop);
                        break;
                    case BOOLEAN:
                        mergeBooleanProperty(prop);
                        break;
                }
            } else {
                LOGGER.error("Inconsistencies of property type for '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line",
                    prop, dl1.getPropertyType(prop), dl2.getPropertyType(prop));
            }
        });
    }

    void computeAndSetP0() {
        double p1 = dl1.getTerminal().getP();
        double p2 = dl2.getTerminal().getP();
        if (!Double.isNaN(p1) && !Double.isNaN(p2)) {
            double losses = p1 + p2;
            dl1.setP0((p1 + losses / 2.0) * sign(p2));
            dl2.setP0((p2 + losses / 2.0) * sign(p1));
        }
    }

    void computeAndSetQ0() {
        double q1 = dl1.getTerminal().getQ();
        double q2 = dl2.getTerminal().getQ();
        if (!Double.isNaN(q1) && !Double.isNaN(q2)) {
            double losses = q1 + q2;
            dl1.setQ0((q1 + losses / 2.0) * sign(q2));
            dl2.setQ0((q2 + losses / 2.0) * sign(q1));
        }
    }

    private static int sign(double value) {
        // Sign depends on the transit flow:
        // P1 ---->-----DL1.P0 ---->----- DL2.P0 ---->---- P2
        // The sign of DL1.P0 is the same as P2, and respectively the sign of DL2.P0 is the same than P1
        return (value >= 0) ? 1 : -1;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.LINE;
    }

    @Override
    public boolean isTieLine() {
        return false;
    }

    @Override
    public MergingView getNetwork() {
        return index.getView();
    }

    @Override
    public Terminal getTerminal(final Side side) {
        switch (side) {
            case ONE:
                return getTerminal1();
            case TWO:
                return getTerminal2();
            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public Terminal getTerminal1() {
        return index.getTerminal(dl1.getTerminal());
    }

    @Override
    public Terminal getTerminal2() {
        return index.getTerminal(dl2.getTerminal());
    }

    @Override
    public CurrentLimits getCurrentLimits(final Side side) {
        switch (side) {
            case ONE:
                return getCurrentLimits1();
            case TWO:
                return getCurrentLimits2();
            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return dl1.getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return dl1.newCurrentLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return dl2.getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return dl2.newCurrentLimits();
    }

    @Override
    public String getId() {
        return id;
    }

    MergedLine setId(String id) {
        Objects.requireNonNull(id, "id is null");
        this.id = Identifiables.getUniqueId(id, index::contains);
        return this;
    }

    @Override
    public double getR() {
        return dl1.getR() + dl2.getR();
    }

    @Override
    public Line setR(final double r) {
        dl1.setR(r / 2.0d);
        dl2.setR(r / 2.0d);
        return this;
    }

    @Override
    public double getX() {
        return dl1.getX() + dl2.getX();
    }

    @Override
    public Line setX(final double x) {
        dl1.setX(x / 2.0d);
        dl2.setX(x / 2.0d);
        return this;
    }

    @Override
    public double getG1() {
        return dl1.getG();
    }

    @Override
    public Line setG1(final double g1) {
        dl1.setG(g1);
        return this;
    }

    @Override
    public double getG2() {
        return dl2.getG();
    }

    @Override
    public Line setG2(final double g2) {
        dl2.setG(g2);
        return this;
    }

    @Override
    public double getB1() {
        return dl1.getB();
    }

    @Override
    public Line setB1(final double b1) {
        dl1.setB(b1);
        return this;
    }

    @Override
    public double getB2() {
        return dl2.getB();
    }

    @Override
    public Line setB2(final double b2) {
        dl2.setB(b2);
        return this;
    }

    @Override
    public Terminal getTerminal(final String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);

        Terminal terminal1 = dl1.getTerminal();
        Terminal terminal2 = dl2.getTerminal();
        if (voltageLevelId.equals(terminal1.getVoltageLevel().getId())) {
            return terminal1;
        } else if (voltageLevelId.equals(terminal2.getVoltageLevel().getId())) {
            return terminal2;
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    @Override
    public Side getSide(final Terminal terminal) {
        Objects.requireNonNull(terminal);

        Terminal term = terminal;
        if (term instanceof AbstractAdapter) {
            term = ((AbstractAdapter<Terminal>) term).getDelegate();
        }
        if (term == dl1.getTerminal()) {
            return Side.ONE;
        } else if (term == dl2.getTerminal()) {
            return Side.TWO;
        } else {
            throw new PowsyblException("The terminal is not connected to this branch");
        }
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(final float limitReduction) {
        return checkPermanentLimit1(limitReduction) || checkPermanentLimit2(limitReduction);
    }

    @Override
    public int getOverloadDuration() {
        Branch.Overload o1 = checkTemporaryLimits1();
        Branch.Overload o2 = checkTemporaryLimits2();
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public boolean checkPermanentLimit(final Side side, final float limitReduction) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction);

            case TWO:
                return checkPermanentLimit2(limitReduction);

            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public boolean checkPermanentLimit(final Side side) {
        return checkPermanentLimit(side, 1f);
    }

    @Override
    public boolean checkPermanentLimit1(final float limitReduction) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getTerminal1().getI());
    }

    @Override
    public boolean checkPermanentLimit1() {
        return checkPermanentLimit1(1f);
    }

    @Override
    public boolean checkPermanentLimit2(final float limitReduction) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getTerminal2().getI());
    }

    @Override
    public boolean checkPermanentLimit2() {
        return checkPermanentLimit2(1f);
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, final float limitReduction) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction);

            case TWO:
                return checkTemporaryLimits2(limitReduction);

            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public Overload checkTemporaryLimits(final Side side) {
        return checkTemporaryLimits(side, 1f);
    }

    @Override
    public Overload checkTemporaryLimits1(final float limitReduction) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, getTerminal1().getI());
    }

    @Override
    public Overload checkTemporaryLimits1() {
        return checkTemporaryLimits1(1f);
    }

    @Override
    public Overload checkTemporaryLimits2(final float limitReduction) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, getTerminal2().getI());
    }

    @Override
    public Overload checkTemporaryLimits2() {
        return checkTemporaryLimits2(1f);
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return Stream.concat(dl1.getTerminals().stream(),
                             dl2.getTerminals().stream())
                     .map(index::getTerminal)
                     .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(final String key) {
        return properties.containsKey(key);
    }

    @Override
    public Type getPropertyType(final String key) {
        Pair<Type, Object> val = properties.get(key);
        return val != null ? val.getKey() : null;
    }

    @Override
    public String getProperty(final String key) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.STRING.equals(val.getKey())) ? (String) val.getValue() : null;
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.STRING.equals(val.getKey())) ? (String) val.getValue() : defaultValue;
    }

    @Override
    public Integer getIntegerProperty(final String key) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.INTEGER.equals(val.getKey())) ? (Integer) val.getValue() : null;
    }

    @Override
    public Integer getIntegerProperty(final String key, final Integer defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.INTEGER.equals(val.getKey())) ? (Integer) val.getValue() : defaultValue;
    }

    @Override
    public Double getDoubleProperty(final String key) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.DOUBLE.equals(val.getKey())) ? (Double) val.getValue() : null;
    }

    @Override
    public Double getDoubleProperty(final String key, final Double defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.DOUBLE.equals(val.getKey())) ? (Double) val.getValue() : defaultValue;
    }

    @Override
    public Boolean getBooleanProperty(final String key) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.BOOLEAN.equals(val.getKey())) ? (Boolean) val.getValue() : null;
    }

    @Override
    public Boolean getBooleanProperty(final String key, final Boolean defaultValue) {
        Pair<Type, Object> val = properties.get(key);
        return (val != null && Type.BOOLEAN.equals(val.getKey())) ? (Boolean) val.getValue() : defaultValue;
    }

    @Override
    public String setProperty(final String key, final String value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.STRING, value);
        Pair<Type, Object> oldVal = properties.put(key, val);
        return oldVal != null ? (String) oldVal.getValue() : null;
    }

    @Override
    public Integer setIntegerProperty(final String key, final Integer value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.INTEGER, value);
        Pair<Type, Object> oldVal = properties.put(key, val);
        return oldVal != null ? (Integer) oldVal.getValue() : null;
    }

    @Override
    public Double setDoubleProperty(final String key, final Double value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.DOUBLE, value);
        Pair<Type, Object> oldVal = properties.put(key, val);
        return oldVal != null ? (Double) oldVal.getValue() : null;
    }

    @Override
    public Boolean setBooleanProperty(final String key, final Boolean value) {
        Pair<Type, Object> val = new ImmutablePair<>(Type.BOOLEAN, value);
        Pair<Type, Object> oldVal = properties.put(key, val);
        return oldVal != null ? (Boolean) oldVal.getValue() : null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public Boolean removeProperty(final String key) {
        Pair<Type, Object> removedVal = properties.remove(key);
        return removedVal != null;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> void addExtension(final Class<? super E> type, final E extension) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> E getExtension(final Class<? super E> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> E getExtensionByName(final String name) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> boolean removeExtension(final Class<E> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> Collection<E> getExtensions() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>, B extends ExtensionAdder<Line, E>> B newExtension(Class<B> type) {
        throw MergingView.createNotImplementedException();
    }
}

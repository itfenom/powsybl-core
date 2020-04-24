/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.Properties;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.LimitViolationUtils;
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

    private final Properties properties = new Properties();

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.dl1 = Objects.requireNonNull(dl1, "dangling line 1 is null");
        this.dl2 = Objects.requireNonNull(dl2, "dangling line 2 is null");
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildId(dl1, dl2), index::contains) : buildId(dl1, dl2);
        this.name = buildName(dl1, dl2);
        properties.mergeProperties(dl1, dl2);
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
        return dl1.getOptionalName()
                .map(name1 -> dl2.getOptionalName()
                        .map(name2 -> buildName(name1, name2))
                        .orElse(name1))
                .orElseGet(() -> dl2.getOptionalName().orElse(null));
    }

    private static String buildName(String name1, String name2) {
        int compareResult = name1.compareTo(name2);
        if (compareResult == 0) {
            return name1;
        } else if (compareResult < 0) {
            return name1 + " + " + name2;
        } else {
            return name2 + " + " + name1;
        }
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

    Side getSide(final DanglingLine dl) {
        Objects.requireNonNull(dl);
        return getSide(dl.getTerminal());
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
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(name);
    }

    @Override
    public String getNameOrId() {
        return getOptionalName().orElse(id);
    }

    @Override
    public boolean isFictitious() {
        return dl1.isFictitious() || dl2.isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        dl1.setFictitious(fictitious);
        dl2.setFictitious(fictitious);
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
        dl1.setStringProperty(key, value);
        dl2.setStringProperty(key, value);
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
        dl1.setIntegerProperty(key, value);
        dl2.setIntegerProperty(key, value);
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
        dl1.setDoubleProperty(key, value);
        dl2.setDoubleProperty(key, value);
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
        dl1.setBooleanProperty(key, value);
        dl2.setBooleanProperty(key, value);
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

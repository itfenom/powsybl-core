/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StaticVarCompensatorAdderAdapter extends AbstractIdentifiableAdderAdapter<StaticVarCompensatorAdder> implements StaticVarCompensatorAdder {

    StaticVarCompensatorAdderAdapter(StaticVarCompensatorAdder delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public StaticVarCompensatorAdapter add() {
        checkAndSetUniqueId();
        return getIndex().getStaticVarCompensator(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public StaticVarCompensatorAdder setBmin(double bMin) {
        getDelegate().setBmin(bMin);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBmax(double bMax) {
        getDelegate().setBmax(bMax);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setVoltageSetPoint(double voltageSetPoint) {
        getDelegate().setVoltageSetPoint(voltageSetPoint);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setReactivePowerSetPoint(double reactivePowerSetPoint) {
        getDelegate().setReactivePowerSetPoint(reactivePowerSetPoint);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setRegulationMode(StaticVarCompensator.RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setNode(int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBus(String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setConnectableBus(String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }
}
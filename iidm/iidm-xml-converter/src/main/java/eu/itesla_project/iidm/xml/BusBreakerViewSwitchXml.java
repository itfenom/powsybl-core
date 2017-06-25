/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusBreakerViewSwitchXml extends SwitchXml<VoltageLevel.BusBreakerView.SwitchAdder> {

    static final BusBreakerViewSwitchXml INSTANCE = new BusBreakerViewSwitchXml();

    @Override
    protected void writeRootElementAttributes(Switch s, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        super.writeRootElementAttributes(s, vl, context);
        VoltageLevel.BusBreakerView v = vl.getBusBreakerView();
        Bus bus1 = v.getBus1(s.getId());
        Bus bus2 = v.getBus2(s.getId());
        context.getWriter().writeAttribute("bus1", context.getAnonymizer().anonymizeString(bus1.getId()));
        context.getWriter().writeAttribute("bus2", context.getAnonymizer().anonymizeString(bus2.getId()));
    }

    @Override
    protected VoltageLevel.BusBreakerView.SwitchAdder createAdder(VoltageLevel vl) {
        return vl.getBusBreakerView().newSwitch();
    }

    @Override
    protected Switch readRootElementAttributes(VoltageLevel.BusBreakerView.SwitchAdder adder, XmlReaderContext context) {
        boolean open = XmlUtil.readBoolAttribute(context.getReader(), "open");
        boolean ficticious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "ficticious", false);
        String bus1 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus1"));
        String bus2 = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "bus2"));
        return adder.setOpen(open)
                .setFicticious(ficticious)
                .setBus1(bus1)
                .setBus2(bus2)
                .add();
    }
}

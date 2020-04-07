/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorXml extends AbstractConnectableXml<StaticVarCompensator, StaticVarCompensatorAdder, VoltageLevel> {

    static final StaticVarCompensatorXml INSTANCE = new StaticVarCompensatorXml();

    static final String ROOT_ELEMENT_NAME = "staticVarCompensator";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(StaticVarCompensator svc) {
        return !Objects.equals(svc, svc.getRegulatingTerminal().getConnectable());
    }

    @Override
    protected void writeRootElementAttributes(StaticVarCompensator svc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("bMin", svc.getBmin(), context.getWriter());
        XmlUtil.writeDouble("bMax", svc.getBmax(), context.getWriter());
        XmlUtil.writeDouble("voltageSetPoint", svc.getVoltageSetPoint(), context.getWriter());
        XmlUtil.writeDouble("reactivePowerSetPoint", svc.getReactivePowerSetPoint(), context.getWriter());
        context.getWriter().writeAttribute("regulationMode", svc.getRegulationMode().name());
        writeNodeOrBus(null, svc.getTerminal(), context);
        writePQ(null, svc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(StaticVarCompensator svc, VoltageLevel vl, NetworkXmlWriterContext context) {
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(!Objects.equals(svc, svc.getRegulatingTerminal().getConnectable()),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmXmlVersion.V_1_1, context, () -> {
                try {
                    TerminalRefXml.writeTerminalRef(svc.getRegulatingTerminal(), context, REGULATING_TERMINAL);
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
    }

    @Override
    protected StaticVarCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newStaticVarCompensator();
    }

    @Override
    protected StaticVarCompensator readRootElementAttributes(StaticVarCompensatorAdder adder, NetworkXmlReaderContext context) {
        double bMin = XmlUtil.readDoubleAttribute(context.getReader(), "bMin");
        double bMax = XmlUtil.readDoubleAttribute(context.getReader(), "bMax");
        double voltageSetPoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "voltageSetPoint");
        double reactivePowerSetPoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "reactivePowerSetPoint");
        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.valueOf(context.getReader().getAttributeValue(null, "regulationMode"));
        adder.setBmin(bMin)
                .setBmax(bMax)
                .setVoltageSetPoint(voltageSetPoint)
                .setReactivePowerSetPoint(reactivePowerSetPoint)
                .setRegulationMode(regulationMode);
        readNodeOrBus(adder, context);
        StaticVarCompensator svc = adder.add();
        readPQ(null, svc.getTerminal(), context.getReader());
        return svc;
    }

    @Override
    protected void readSubElements(StaticVarCompensator svc, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            if (context.getReader().getLocalName().equals(REGULATING_TERMINAL)) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
                String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                String side = context.getReader().getAttributeValue(null, "side");
                context.getEndTasks().add(() -> svc.setRegulatingTerminal(TerminalRefXml
                        .readTerminalRef(svc.getTerminal().getVoltageLevel().getSubstation().getNetwork(), id, side)));
            } else {
                super.readSubElements(svc, context);
            }
        });
    }
}

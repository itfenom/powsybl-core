/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.IidmImportExportType;
import com.powsybl.iidm.network.*;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerXml extends AbstractTransformerXml<TwoWindingsTransformer, TwoWindingsTransformerAdder> {

    static final TwoWindingsTransformerXml INSTANCE = new TwoWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "twoWindingsTransformer";
    static final String RATIO_TAP_CHANGER_ELEMENT_NAME = "ratioTapChanger";
    static final String PHASE_TAP_CHANGER_ELEMENT_NAME = "phaseTapChanger";
    static final String TERMINAL_REF_ELEMENT_NAME = "terminalRef";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(TwoWindingsTransformer twt) {
        return twt.getRatioTapChanger() != null
                || twt.getPhaseTapChanger() != null
                || twt.getCurrentLimits1() != null
                || twt.getCurrentLimits2() != null;
    }

    @Override
    protected boolean hasControlValues(TwoWindingsTransformer twt) {
        return twt.getRatioTapChanger() != null || twt.getPhaseTapChanger() != null;
    }

    @Override
    protected boolean hasStateValues(TwoWindingsTransformer twt) {
        return isTerminalHavingStateValues(twt.getTerminal1()) || isTerminalHavingStateValues(twt.getTerminal2());
    }

    boolean hasTopoValues(TwoWindingsTransformer twt, NetworkXmlWriterContext  context) {
        return isTerminalHavingTopoValues(twt.getTerminal1(), context) || isTerminalHavingTopoValues(twt.getTerminal2(), context);
    }

    @Override
    protected void writeRootElementAttributes(TwoWindingsTransformer twt, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM) {
            XmlUtil.writeDouble("r", twt.getR(), context.getWriter());
            XmlUtil.writeDouble("x", twt.getX(), context.getWriter());
            XmlUtil.writeDouble("g", twt.getG(), context.getWriter());
            XmlUtil.writeDouble("b", twt.getB(), context.getWriter());
            XmlUtil.writeDouble("ratedU1", twt.getRatedU1(), context.getWriter());
            XmlUtil.writeDouble("ratedU2", twt.getRatedU2(), context.getWriter());
        }
        if (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM ||  context.getTargetFile() == IncrementalIidmFiles.TOPO) {
            writeNodeOrBus(1, twt.getTerminal1(), context);
            writeNodeOrBus(2, twt.getTerminal2(), context);
        }
        if (context.getOptions().isWithBranchSV() && (context.getOptions().getImportExportType() == IidmImportExportType.FULL_IIDM || context.getTargetFile() == IncrementalIidmFiles.STATE)) {
            writePQ(1, twt.getTerminal1(), context.getWriter());
            writePQ(2, twt.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(TwoWindingsTransformer twt, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        if (context.getTargetFile() == IncrementalIidmFiles.TOPO || context.getTargetFile() == IncrementalIidmFiles.STATE) {
            return;
        }
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            writeRatioTapChanger(RATIO_TAP_CHANGER_ELEMENT_NAME, rtc, context);
        }
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            writePhaseTapChanger(PHASE_TAP_CHANGER_ELEMENT_NAME, ptc, context);
        }
        if (context.getOptions().getImportExportType() == IidmImportExportType.INCREMENTAL_IIDM) {
            return;
        }
        if (twt.getCurrentLimits1() != null) {
            writeCurrentLimits(1, twt.getCurrentLimits1(), context.getWriter());
        }
        if (twt.getCurrentLimits2() != null) {
            writeCurrentLimits(2, twt.getCurrentLimits2(), context.getWriter());
        }
    }

    @Override
    protected TwoWindingsTransformerAdder createAdder(Substation s) {
        return s.newTwoWindingsTransformer();
    }

    @Override
    protected TwoWindingsTransformer readRootElementAttributes(TwoWindingsTransformerAdder adder, NetworkXmlReaderContext context) {
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
        double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        double ratedU1 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU1");
        double ratedU2 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU2");
        adder.setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2);
        readNodeOrBus(adder, context);
        TwoWindingsTransformer twt = adder.add();
        readPQ(1, twt.getTerminal1(), context.getReader());
        readPQ(2, twt.getTerminal2(), context.getReader());
        return twt;
    }

    @Override
    protected void readSubElements(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, twt::newCurrentLimits1, context.getReader());
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, twt::newCurrentLimits2, context.getReader());
                    break;

                case RATIO_TAP_CHANGER_ELEMENT_NAME:
                    readRatioTapChanger(twt, context);
                    break;

                case PHASE_TAP_CHANGER_ELEMENT_NAME:
                    readPhaseTapChanger(twt, context);
                    break;

                default:
                    super.readSubElements(twt, context);
            }
        });
    }
}

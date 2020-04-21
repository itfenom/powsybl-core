/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import javax.xml.stream.XMLStreamException;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class PropertiesXml {

    static final String PROPERTY = "property";

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final String TYPE_STRING = "string";
    private static final String TYPE_INTEGER = "integer";
    private static final String TYPE_DOUBLE = "double";
    private static final String TYPE_BOOLEAN = "boolean";

    public static void write(Identifiable<?> identifiable, NetworkXmlWriterContext context) throws XMLStreamException {
        if (identifiable.hasStringProperty()) {
            for (String name : identifiable.getStringPropertyNames()) {
                String value = identifiable.getStringProperty(name);
                context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
                context.getWriter().writeAttribute(NAME, name);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                    try {
                        context.getWriter().writeAttribute(TYPE, TYPE_STRING);
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                });
                context.getWriter().writeAttribute(VALUE, value);
            }
        }
        if (identifiable.hasIntegerProperty()) {
            for (String name : identifiable.getIntegerPropertyNames()) {
                String value = identifiable.getIntegerProperty(name).toString();
                context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
                context.getWriter().writeAttribute(NAME, name);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                    try {
                        context.getWriter().writeAttribute(TYPE, TYPE_INTEGER);
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                });
                context.getWriter().writeAttribute(VALUE, value);
            }
        }
        if (identifiable.hasDoubleProperty()) {
            for (String name : identifiable.getDoublePropertyNames()) {
                String value = identifiable.getDoubleProperty(name).toString();
                context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
                context.getWriter().writeAttribute(NAME, name);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                    try {
                        context.getWriter().writeAttribute(TYPE, TYPE_DOUBLE);
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                });
                context.getWriter().writeAttribute(VALUE, value);
            }
        }
        if (identifiable.hasBooleanProperty()) {
            for (String name : identifiable.getBooleanPropertyNames()) {
                String value = identifiable.getBooleanProperty(name).toString();
                context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
                context.getWriter().writeAttribute(NAME, name);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                    try {
                        context.getWriter().writeAttribute(TYPE, TYPE_BOOLEAN);
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                });
                context.getWriter().writeAttribute(VALUE, value);
            }
        }
    }

    public static void read(Identifiable identifiable, NetworkXmlReaderContext context) {
        assert context.getReader().getLocalName().equals(PROPERTY);
        String name = context.getReader().getAttributeValue(null, NAME);
        String type = context.getReader().getAttributeValue(null, TYPE);
        String value = context.getReader().getAttributeValue(null, VALUE);

        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            identifiable.setStringProperty(name, value);
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            switch (type) {
                case TYPE_STRING:
                    identifiable.setStringProperty(name, value);
                    break;
                case TYPE_INTEGER:
                    identifiable.setIntegerProperty(name, Integer.parseInt(value));
                    break;
                case TYPE_DOUBLE:
                    identifiable.setDoubleProperty(name, Double.parseDouble(value));
                    break;
                case TYPE_BOOLEAN:
                    identifiable.setBooleanProperty(name, Boolean.parseBoolean(value));
                    break;
                default:
                    throw new PowsyblException("");
            }
        });
    }

    private PropertiesXml() {
    }
}

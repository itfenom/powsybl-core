/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XmlExporterBaseExtensions  extends AbstractConverterTest {

    public void exporterTestBaseExtensions(Network network, String xiidmBaseRef, String xiidmExtRef) throws IOException {
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.SEPARATE_BASE_EXTENSIONS, "true");

        MemDataSource dataSource = new MemDataSource();

        new XMLExporter().export(network, properties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getClass().getResourceAsStream(xiidmBaseRef), is);
        }
        // check the exported extensions file and compare it to xiidmExtRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("ext", "xiidm"))) {
            compareXml(getClass().getResourceAsStream(xiidmExtRef), is);
        }
    }

    @Test
    public void test() throws IOException {
        exporterTestBaseExtensions(MultipleExtensionsTestNetworkFactory.create(),
                "/multiple-extensions-base.xiidm",
                "/multiple-extensions-ext.xiidm");
    }
}

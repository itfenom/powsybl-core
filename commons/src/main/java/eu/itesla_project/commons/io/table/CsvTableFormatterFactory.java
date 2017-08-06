/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io.table;

import java.io.Writer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvTableFormatterFactory implements TableFormatterFactory {

    @Override
    public TableFormatter create(Writer writer, String title, TableFormatterConfig config, Column... columns) {
        return new CsvTableFormatter(writer, title, config, columns);
    }
}

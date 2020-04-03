/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.commons.PowsyblException
import com.powsybl.commons.extensions.Extension
import com.powsybl.iidm.network.Identifiable
import com.powsybl.iidm.network.util.Properties

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiableExtension {
    static Object propertyMissing(Identifiable self, String name) {
        // first check if an extension exist then a property
        Extension extension = self.getExtensionByName(name)
        if (extension != null) {
            extension
        } else {
            switch(self.getPropertyType(name)) {
                case Properties.Type.BOOLEAN:
                    self.getProperty(name).orElse(null)
                    break;
                case Properties.Type.DOUBLE:
                    if (self.getProperty(name).isPresent()) {
                        self.getProperty(name).get()
                    }
                    break;
                case Properties.Type.INTEGER:
                    if (self.getProperty(name).isPresent()) {
                        self.getProperty(name).get()
                    }
                    break;
                default:
                    self.getProperty(name).orElse(null)
            }
        }
    }
    static void propertyMissing(Identifiable self, String name, Object value) {
        if (value == null) {
            self.removeProperty(name)
        } else {
            switch(self.getPropertyType(name)) {
                case Properties.Type.BOOLEAN:
                    self.getProperty(name).orElse(null)
                    break;
                case Properties.Type.DOUBLE:
                    if (self.getProperty(name).isPresent()) {
                        self.getProperty(name).get()
                    }
                    break;
                case Properties.Type.INTEGER:
                    if (self.getProperty(name).isPresent()) {
                        self.getProperty(name).get()
                    }
                    break;
                default:
                    self.getProperty(name).orElse(null)
            }
        }
    }

    /**
     * To fix private field accessibility issue.
     * https://issues.apache.org/jira/browse/GROOVY-3010
     */
    static void setId(Identifiable self, String id) {
        throw new PowsyblException("ID modification of '" + self.id + "' is not allowed")
    }
}

/*
 * Copyright 2017-2020 Volkan Yazıcı
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permits and
 * limitations under the License.
 */

package com.vlkan.log4j2.logstash.layout.resolver;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.lookup.MainMapLookup;

public class MainMapResolver implements EventResolver {

    private static final MainMapLookup MAIN_MAP_LOOKUP = new MainMapLookup();

    private final EventResolverContext context;

    private final String key;

    static String getName() {
        return "main";
    }

    MainMapResolver(EventResolverContext context, String key) {
        this.context = context;
        this.key = key;
    }

    @Override
    public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
        String value = MAIN_MAP_LOOKUP.lookup(key);
        boolean valueExcluded = context.isEmptyPropertyExclusionEnabled() && StringUtils.isEmpty(value);
        if (valueExcluded) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeObject(value);
        }
    }

}

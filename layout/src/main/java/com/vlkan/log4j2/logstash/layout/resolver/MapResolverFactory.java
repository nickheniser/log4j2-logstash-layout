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

public class MapResolverFactory implements EventResolverFactory<MapResolver> {

    private static final MapResolverFactory INSTANCE = new MapResolverFactory();

    private MapResolverFactory() {}

    static MapResolverFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return MapResolver.getName();
    }

    @Override
    public MapResolver create(EventResolverContext context, String key) {
        return new MapResolver(context, key);
    }

}

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

package com.vlkan.log4j2.logstash.layout;

import org.apache.logging.log4j.core.layout.ByteBufferDestination;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

class FixedByteBufferDestination implements ByteBufferDestination {

    private final ByteBuffer byteBuffer;

    FixedByteBufferDestination(int maxByteCount) {
        this.byteBuffer = ByteBuffer.allocate(maxByteCount);
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public ByteBuffer drain(ByteBuffer sourceByteBuffer) {
        if (byteBuffer != sourceByteBuffer) {
            sourceByteBuffer.flip();
            byteBuffer.put(sourceByteBuffer);
        } else if (byteBuffer.remaining() == 0) {
            throw new BufferOverflowException();
        }
        return byteBuffer;
    }

    @Override
    public void writeBytes(ByteBuffer sourceByteBuffer) {
        byteBuffer.put(sourceByteBuffer);
    }

    @Override
    public void writeBytes(byte[] data, int offset, int length) {
        byteBuffer.put(data,offset,length);
    }

}

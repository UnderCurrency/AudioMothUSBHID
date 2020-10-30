/*
 *  (c)  Copyright 2020 Undercurrency
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.undercurrency.audiomoth.usbhid.model;

public enum PacketLength {
    ZERO("0.0.0",39),
    ONE_TWO("1.2.0",40),
    ONE_TWO_ONE("1.2.1",42),
    ONE_TWO_TWO("1.2.2",43),
    ONE_FOUR("1.4.0",58)
    ;
    private String firmwareVersion;
    private int packetLenght;

    PacketLength(String firmwareVersion, int packetLenght) {
        this.firmwareVersion = firmwareVersion;
        this.packetLenght = packetLenght;
    }
}

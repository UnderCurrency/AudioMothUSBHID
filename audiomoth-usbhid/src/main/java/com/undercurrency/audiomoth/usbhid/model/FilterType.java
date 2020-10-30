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


import com.google.gson.annotations.SerializedName;

public enum FilterType {
    @SerializedName("low")
    LOW("low"),
    @SerializedName("band")
    BAND("band"),
    @SerializedName("high")
    HIGH("high");

    private final String value;

    public String getValue() {
        return value;
    }

    private FilterType(String value) {
        this.value = value;
    }
}
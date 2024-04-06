/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.service.controls.templates;

import androidx.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper for parceling/unparceling {@link ControlTemplate}.
 * @hide
 */
public final class ControlTemplateWrapper implements Parcelable {

    public ControlTemplateWrapper(@NonNull ControlTemplate template) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public ControlTemplate getWrappedTemplate() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static final @NonNull Creator<ControlTemplateWrapper> CREATOR =
            new Creator<ControlTemplateWrapper>() {
        @Override
        public ControlTemplateWrapper createFromParcel(@NonNull Parcel source) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public ControlTemplateWrapper[] newArray(int size) {
            throw new RuntimeException("Stub!");
        }
    };
}
/*
 * Copyright (C) 2012 The Android Open Source Project
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

package io.chub.android.contacts.list;

import android.net.Uri;
import android.util.Log;

import io.chub.android.contacts.common.list.ContactEntryListAdapter;
import io.chub.android.contacts.common.list.ContactListItemView;
import io.chub.android.contacts.common.list.PhoneNumberPickerFragment;

/**
 * Version of PhoneNumberPickerFragment used specifically for legacy support.
 */
public class LegacyPhoneNumberPickerFragment extends PhoneNumberPickerFragment {

    private static final String TAG = LegacyPhoneNumberPickerFragment.class.getSimpleName();

    @Override
    protected boolean getVisibleScrollbarEnabled() {
        return false;
    }

    @Override
    protected Uri getPhoneUri(int position) {
        final LegacyPhoneNumberListAdapter adapter = (LegacyPhoneNumberListAdapter) getAdapter();
        return adapter.getPhoneUri(position);
    }

    @Override
    protected ContactEntryListAdapter createListAdapter() {
        LegacyPhoneNumberListAdapter adapter = new LegacyPhoneNumberListAdapter(getActivity());
        adapter.setDisplayPhotos(true);
        return adapter;
    }

    @Override
    protected void setPhotoPosition(ContactEntryListAdapter adapter) {
        // no-op
    }

    @Override
    protected void startPhoneNumberShortcutIntent(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPhotoPosition(ContactListItemView.PhotoPosition photoPosition) {
        Log.w(TAG, "setPhotoPosition() is ignored in legacy compatibility mode.");
    }
}

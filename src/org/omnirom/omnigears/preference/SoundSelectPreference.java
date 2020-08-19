/*
 *  Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnigears.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.CustomDialogPreference;

import java.io.File;
import java.util.Arrays;

public class SoundSelectPreference extends CustomDialogPreference {
    private SoundListAdapter mAdapter;
    private AsyncPlayer mPlayer;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mSelectedValue;
    private ListView mListView;

    public SoundSelectPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SoundSelectPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public SoundSelectPreference(Context context) {
        this(context, null);
    }

    public SoundSelectPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setDialogLayoutResource(R.layout.sound_list_dialog);
        mPlayer = new AsyncPlayer("SoundSelectPreference");

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SoundSelectPreference, defStyleAttr, defStyleRes);
        mEntries = a.getTextArray(R.styleable.SoundSelectPreference_entries);
        mEntryValues = a.getTextArray(R.styleable.SoundSelectPreference_entryValues);
        a.recycle();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mAdapter = new SoundListAdapter(getContext());
        mListView = (ListView) view.findViewById(R.id.sound_list_view);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(mAdapter);
        int idx =findIndexOfValue(mSelectedValue);
        if (idx != -1) {
            mListView.setItemChecked(idx, true);
            mListView.setSelection(idx);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SoundViewHolder holder = (SoundViewHolder) view.getTag();
                Uri sound = Uri.fromFile(new File(holder.mSoundUri.toString()));
                mPlayer.play(getContext(), sound, false, AudioManager.STREAM_SYSTEM);
                mSelectedValue = holder.mSoundUri.toString();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (callChangeListener(mSelectedValue)) {
                if (shouldPersist()) {
                    persistString(mSelectedValue);
                }
                notifyChanged();
                int idx = findIndexOfValue(mSelectedValue);
                if (idx != -1) {
                    setSummary(mEntries[idx]);
                }
            }
        }
    }

    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            mSelectedValue = mEntryValues[index].toString();
            setSummary(mEntries[index]);
        }
    }

    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public class SoundListAdapter extends ArrayAdapter<CharSequence> {
        private final LayoutInflater mInflater;

        public SoundListAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(Arrays.asList(mEntries));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SoundViewHolder holder = SoundViewHolder.createOrRecycle(mInflater, convertView);
            convertView = holder.rootView;
            holder.soundName.setText(mEntries[position]);
            holder.mSoundUri = mEntryValues[position];
            return convertView;
        }
    }

    public static class SoundViewHolder {
        public View rootView;
        public CheckedTextView soundName;
        public CharSequence mSoundUri;

        public static SoundViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
            if (convertView == null) {
                convertView = inflater.inflate(com.android.internal.R.layout.select_dialog_singlechoice_material, null);

                SoundViewHolder holder = new SoundViewHolder();
                holder.rootView = convertView;
                holder.soundName = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
                return holder;
            } else {
                return (SoundViewHolder) convertView.getTag();
            }
        }
    }
}

package com.android.settings;

import android.os.Bundle;
import android.content.Intent;
import android.content.ComponentName;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import java.util.ArrayList;
import java.util.List;

public class SafeCallsOSSettings extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.app.vpn", "com.app.vpn.MainActivity"));
        startActivity(intent);

        Activity activity = getActivity();

        if (activity != null) {
          activity.finish();
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
    new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                boolean enabled) {
            final ArrayList<SearchIndexableResource> result = new ArrayList<>();

            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.safecallsos_settings_prefs;
            result.add(sir);
            return result;
        }
    };
}

package net.radioandrea.netpets;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class NetPetSettings extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction fragTran = getFragmentManager().beginTransaction();
        fragTran.replace(android.R.id.content, new PrefsFragment());
        fragTran.commit();
    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}

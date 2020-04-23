package killer_i.s2s.s2sconnect.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceService {
    SharedPreferences sharedPref;

    public SharedPreferenceService(Context c, String name) {
         this.sharedPref = c.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void putValue(String key, String value) {
        SharedPreferences.Editor editor = this.sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return this.sharedPref.getString(key, null);
    }
}

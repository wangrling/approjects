package com.android.approjects.permissions;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.approjects.R;

import androidx.annotation.Nullable;

public class RuntimePermissionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_permissions, null);

        // BEGIN_INCLUDE(m_only_permission)
        if (Build.VERSION.SDK_INT < 23) {
            /*
            The contacts permissions have been declared in the AndroidManifest for Android M  and
            above only. They are not available on older platforms, so we are hiding the button to
            access the contacts database.
            This shows how new runtime-only permissions can be added, that do not apply to older
            platform versions. This can be useful for automated updates where additional
            permissions might prompt the user on upgrade.
             */
            root.findViewById(R.id.button_contacts).setVisibility(View.GONE);
        }
        // END_INCLUDE(m_only_permission)

        return root;
    }
}

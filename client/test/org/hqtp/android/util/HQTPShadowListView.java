package org.hqtp.android.util;

import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowListView;

@Implements(ListView.class)
public class HQTPShadowListView extends ShadowListView {
    private OnItemLongClickListener itemLongClickListener;

    @Implementation
    public OnItemLongClickListener getOnItemLongClickListener() {
        return itemLongClickListener;
    }

    @Implementation
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }
}

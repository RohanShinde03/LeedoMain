package com.tribeappsoft.leedo.util.discreteScrollView;

import android.view.View;

/**
 * Created by yarolegovich on 02.03.2017.
 */

public interface DiscreteScrollItemTransformer {
    void transformItem(View item, float position);
}

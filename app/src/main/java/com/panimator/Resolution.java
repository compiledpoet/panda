package com.panimator;

import android.util.Size;

import java.util.Comparator;

/**
 * Created by ASANDA on 2018/01/09.
 */

public class Resolution implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
        return Long.signum((long)lhs.getWidth() * lhs.getHeight() / (long)rhs.getWidth() * rhs.getHeight());
    }
}

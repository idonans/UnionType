package com.idonans.uniontype;

import androidx.annotation.Nullable;

/**
 * {@linkplain androidx.recyclerview.widget.DiffUtil}
 */
public interface DeepDiff {

    boolean isSameItem(@Nullable Object other);

    boolean isSameContent(@Nullable Object other);

}

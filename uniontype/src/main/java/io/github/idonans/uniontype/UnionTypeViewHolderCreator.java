package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface UnionTypeViewHolderCreator {

    @Nullable
    UnionTypeViewHolder create(@NonNull Host host);

}

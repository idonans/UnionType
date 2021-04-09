package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface UnionTypeMapper {

    int UNION_TYPE_NULL = -1;

    @Nullable
    UnionTypeViewHolder map(@NonNull Host host, int unionType);

}

package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public interface UnionTypeMapper {

    int UNION_TYPE_NULL = RecyclerView.INVALID_TYPE;

    @Nullable
    UnionTypeViewHolder map(@NonNull Host host, int unionType);

}

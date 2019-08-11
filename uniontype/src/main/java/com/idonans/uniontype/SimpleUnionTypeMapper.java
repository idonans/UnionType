package com.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

public class SimpleUnionTypeMapper implements UnionTypeMapper {

    private final SparseArrayCompat<UnionTypeViewHolderCreator> mCreators = new SparseArrayCompat<>();

    public void put(int unionType, UnionTypeViewHolderCreator creator) {
        mCreators.put(unionType, creator);
    }

    @Nullable
    @Override
    public UnionTypeViewHolder map(@NonNull Host host, int unionType) {
        UnionTypeViewHolderCreator creator = mCreators.get(unionType);
        if (creator != null) {
            return creator.create(host);
        }
        return null;
    }

}

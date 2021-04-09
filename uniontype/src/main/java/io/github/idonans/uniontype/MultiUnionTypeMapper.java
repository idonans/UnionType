package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MultiUnionTypeMapper implements UnionTypeMapper {

    private final UnionTypeMapper[] mMappers;

    public MultiUnionTypeMapper(UnionTypeMapper... mappers) {
        mMappers = mappers;
    }

    @Nullable
    @Override
    public UnionTypeViewHolder map(@NonNull Host host, int unionType) {
        if (mMappers != null) {
            for (UnionTypeMapper mapper : mMappers) {
                if (mapper != null) {
                    UnionTypeViewHolder target = mapper.map(host, unionType);
                    if (target != null) {
                        return target;
                    }
                }
            }
        }
        return null;
    }

}

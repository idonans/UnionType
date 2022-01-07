package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * {@linkplain DeepDiff}
 * {@linkplain androidx.recyclerview.widget.DiffUtil}
 */
public final class UnionTypeItemObject {

    public int unionType;
    @Nullable
    public Object itemObject;

    public UnionTypeItemObject(int unionType, @Nullable Object itemObject) {
        this.unionType = unionType;
        this.itemObject = itemObject;
    }

    public void update(int unionType, @Nullable Object itemObject) {
        this.unionType = unionType;
        this.itemObject = itemObject;
    }

    @Nullable
    public <T> T getItemObject(@NonNull Class<T> clazz) {
        if (clazz.isInstance(this.itemObject)) {
            //noinspection unchecked
            return (T) this.itemObject;
        }

        return null;
    }

    /**
     * {@linkplain androidx.recyclerview.widget.DiffUtil}
     */
    public boolean isSameItem(@NonNull UnionTypeItemObject other) {
        if (this.unionType != other.unionType) {
            return false;
        }

        if (this.itemObject instanceof DeepDiff) {
            return ((DeepDiff) this.itemObject).isSameItem(other.itemObject);
        }
        if (other.itemObject instanceof DeepDiff) {
            return ((DeepDiff) other.itemObject).isSameItem(this.itemObject);
        }

        return Objects.equals(this.itemObject, other.itemObject);
    }

    /**
     * {@linkplain androidx.recyclerview.widget.DiffUtil}
     */
    public boolean isSameContent(@NonNull UnionTypeItemObject other) {
        if (this.unionType != other.unionType) {
            return false;
        }

        if (this.itemObject instanceof DeepDiff) {
            return ((DeepDiff) this.itemObject).isSameContent(other.itemObject);
        }
        if (other.itemObject instanceof DeepDiff) {
            return ((DeepDiff) other.itemObject).isSameContent(this.itemObject);
        }

        return false;
    }

}

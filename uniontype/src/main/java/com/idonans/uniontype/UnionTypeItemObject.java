package com.idonans.uniontype;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * {@linkplain DeepDiff}
 * {@linkplain androidx.recyclerview.widget.DiffUtil}
 */
public class UnionTypeItemObject<T> {

    public final int unionType;
    public T itemObject;

    public UnionTypeItemObject(int unionType, T itemObject) {
        this.unionType = unionType;
        this.itemObject = itemObject;
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

    public static <T> UnionTypeItemObject<T> valueOf(int unionType, T itemObject) {
        return new UnionTypeItemObject(unionType, itemObject);
    }
}

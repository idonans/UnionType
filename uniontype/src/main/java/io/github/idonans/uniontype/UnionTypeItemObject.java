package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * @see DeepDiff
 * @see androidx.recyclerview.widget.DiffUtil
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

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getItemObject(@NonNull Class<T> clazz) {
        if (clazz.isInstance(this.itemObject)) {
            return (T) this.itemObject;
        }

        return null;
    }

    /**
     * @param other 待比较的目标对象
     * @return 如果可以复用同一个 ViewHolder 返回 true, 否则返回 false.
     * @see androidx.recyclerview.widget.DiffUtil.Callback#areItemsTheSame(int, int)
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
     * @param other 待比较的目标对象
     * @return 如果需要在复用同一个 ViewHolder 时需要触发 update, 返回 false. 否则返回 true.
     * @see androidx.recyclerview.widget.DiffUtil.Callback#areContentsTheSame(int, int)
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

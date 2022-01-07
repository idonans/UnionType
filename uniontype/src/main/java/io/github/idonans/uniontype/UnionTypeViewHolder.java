package io.github.idonans.uniontype;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.github.idonans.core.thread.Threads;

public abstract class UnionTypeViewHolder extends RecyclerView.ViewHolder {

    @NonNull
    public final Host host;
    @Nullable
    public UnionTypeItemObject unionTypeItemObject;

    public UnionTypeViewHolder(@NonNull Host host, @LayoutRes int layout) {
        this(host, host.getLayoutInflater().inflate(layout, host.getRecyclerView(), false));
    }

    public UnionTypeViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(itemView);
        this.host = host;
    }

    public final void onBind(@Nullable UnionTypeItemObject unionTypeItemObject) {
        this.unionTypeItemObject = unionTypeItemObject;
        onBindUpdate();
    }

    public abstract void onBindUpdate();

    @Nullable
    public <T> T getItemObject(@NonNull Class<T> clazz) {
        if (this.unionTypeItemObject == null) {
            return null;
        }
        return this.unionTypeItemObject.getItemObject(clazz);
    }

    /**
     * 一个 UnionTypeViewHolder 只对应一个 UnionType
     *
     * @return 当前 UnionTypeViewHolder 应当显示的 UnionType. 默认返回 {@link UnionTypeMapper#UNION_TYPE_NULL}
     * @see UnionTypeItemObject
     */
    public int getBestUnionType() {
        return UnionTypeMapper.UNION_TYPE_NULL;
    }

    /**
     * 用于检查当前 ViewHolder 的 type 是否匹配，如果不匹配（返回 false），会调用 {@link #notifySelfChanged()}.
     * 如果当前 bestUnionType 或者 itemViewType 是 {@link UnionTypeMapper#UNION_TYPE_NULL}, 则总是返回 true.
     *
     * @return 如果 UnionType 与实际渲染值匹配返回 true, 否则返回 false.
     */
    public boolean validateUnionType() {
        final int showUnionType = getItemViewType();
        if (showUnionType == UnionTypeMapper.UNION_TYPE_NULL) {
            return true;
        }

        final int bestUnionType = getBestUnionType();
        if (bestUnionType == UnionTypeMapper.UNION_TYPE_NULL) {
            return true;
        }

        if (showUnionType != bestUnionType) {
            Threads.postUi(this::notifySelfChanged);
            return false;
        }
        return true;
    }

    public void notifySelfChanged() {
        //noinspection deprecation
        final int position = getAdapterPosition();
        if (position >= 0) {
            final RecyclerView.Adapter<?> adapter = host.getRecyclerView().getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(position);
            } else {
                UnionTypeLog.v("ignore. notifySelfChanged adapter is null. [%s]", getClass().getName());
            }
        } else {
            UnionTypeLog.v("ignore. notifySelfChanged position is:%s. [%s]", position, getClass().getName());
        }
    }

}

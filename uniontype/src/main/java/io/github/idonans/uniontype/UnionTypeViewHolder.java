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
    public Object itemObject;

    public UnionTypeViewHolder(@NonNull Host host, @LayoutRes int layout) {
        this(host, host.getLayoutInflater().inflate(layout, host.getRecyclerView(), false));
    }

    public UnionTypeViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(itemView);
        this.host = host;
    }

    public final void onBind(@Nullable Object itemObject) {
        this.itemObject = itemObject;
        onBindUpdate();
    }

    public abstract void onBindUpdate();

    /**
     * 一个 UnionTypeViewHolder 只对应一个 UnionType
     *
     * @return 当前 UnionTypeViewHolder 应当显示的 UnionType. 默认返回 {@link RecyclerView#INVALID_TYPE}
     * @see UnionTypeItemObject
     */
    public int getBestUnionType() {
        return RecyclerView.INVALID_TYPE;
    }

    /**
     * 用于检查当前 ViewHolder 的 type 是否匹配，如果不匹配（返回 false），会调用 {@link #notifySelfChanged()}.
     * 如果当前 bestUnionType 或者 itemViewType 是 {@link RecyclerView#INVALID_TYPE}, 则总是返回 true.
     *
     * @return 如果 UnionType 与实际渲染值匹配返回 true, 否则返回 false.
     */
    public boolean validateUnionType() {
        final int showUnionType = getItemViewType();
        if (showUnionType == RecyclerView.INVALID_TYPE) {
            return true;
        }

        final int bestUnionType = getBestUnionType();
        if (bestUnionType == RecyclerView.INVALID_TYPE) {
            return true;
        }

        if (showUnionType != bestUnionType) {
            Threads.postUi(this::notifySelfChanged);
            return false;
        }
        return true;
    }

    public void notifySelfChanged() {
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

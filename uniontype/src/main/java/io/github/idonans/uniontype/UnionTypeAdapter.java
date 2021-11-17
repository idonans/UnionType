package io.github.idonans.uniontype;

import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Preconditions;

public class UnionTypeAdapter extends RecyclerView.Adapter<UnionTypeViewHolder> {

    private final AsyncGroupArrayList mData = new AsyncGroupArrayList(this);
    private Host mHost;
    private UnionTypeMapper mUnionTypeMapper;

    public void setHost(@NonNull Host host) {
        mHost = host;
    }

    public void setUnionTypeMapper(@NonNull UnionTypeMapper unionTypeMapper) {
        mUnionTypeMapper = unionTypeMapper;
    }

    public Host getHost() {
        return mHost;
    }

    public UnionTypeMapper getUnionTypeMapper() {
        return mUnionTypeMapper;
    }

    @NonNull
    @Override
    public UnionTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Preconditions.checkNotNull(mUnionTypeMapper);
        Preconditions.checkNotNull(mHost);

        UnionTypeViewHolder viewHolder = mUnionTypeMapper.map(mHost, viewType);
        if (viewHolder == null) {
            viewHolder = new NullUnionTypeViewHolder(mHost);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        UnionTypeItemObject itemObject = getItem(position);
        if (itemObject == null) {
            return UnionTypeMapper.UNION_TYPE_NULL;
        }
        return itemObject.unionType;
    }

    @Override
    public void onBindViewHolder(@NonNull UnionTypeViewHolder holder, int position) {
        UnionTypeItemObject itemObject = getItem(position);
        if (itemObject != null) {
            holder.onBind(itemObject.itemObject);
        }

        if (mOnLoadPrePageListener != null) {
            if (position <= mLoadPrePageOffset) {
                mOnLoadPrePageListener.onLoadPrePage();
            }
        }
        if (mOnLoadNextPageListener != null) {
            int count = getItemCount();
            if (count - position - 1 <= mLoadNextPageOffset) {
                mOnLoadNextPageListener.onLoadNextPage();
            }
        }
    }

    @NonNull
    public AsyncGroupArrayList getData() {
        return mData;
    }

    @Nullable
    public UnionTypeItemObject getItem(int position) {
        return mData.getReadOnly().getItem(position);
    }

    @Override
    public int getItemCount() {
        return mData.getReadOnly().size();
    }

    public int getGroupItemsSize(int group) {
        return mData.getReadOnly().getGroupItemsSize(group);
    }

    public int getGroupPositionStart(int group) {
        return mData.getReadOnly().getGroupPositionStart(group);
    }

    @Nullable
    public int[] getGroupAndPosition(int position) {
        return mData.getReadOnly().getGroupAndPosition(position);
    }

    @Nullable
    public UnionTypeItemObject getGroupItem(int group, int positionInGroup) {
        return mData.getReadOnly().getGroupItem(group, positionInGroup);
    }

    /**
     * 加载上一页
     */
    public interface OnLoadPrePageListener {
        void onLoadPrePage();
    }

    private int mLoadPrePageOffset = 5;
    private OnLoadPrePageListener mOnLoadPrePageListener;

    public void setLoadPrePageOffset(@IntRange(from = 0) int offset) {
        mLoadPrePageOffset = offset;
    }

    public void setOnLoadPrePageListener(OnLoadPrePageListener listener) {
        mOnLoadPrePageListener = listener;
    }

    /**
     * 加载下一页
     */
    public interface OnLoadNextPageListener {
        void onLoadNextPage();
    }

    private int mLoadNextPageOffset = 5;
    private OnLoadNextPageListener mOnLoadNextPageListener;

    public void setLoadNextPageOffset(@IntRange(from = 0) int offset) {
        mLoadNextPageOffset = offset;
    }

    public void setOnLoadNextPageListener(OnLoadNextPageListener listener) {
        mOnLoadNextPageListener = listener;
    }

}

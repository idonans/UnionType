package com.idonans.uniontype;

import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

public class UnionTypeAdapter extends RecyclerView.Adapter<UnionTypeViewHolder> {

    private RangeArrayList<UnionTypeItemObject> mData = new RangeArrayList<>();
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
            holder.onBind(position, itemObject.itemObject);
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

    @Nullable
    public UnionTypeItemObject getItem(int position) {
        int size = mData.size();
        if (position >= 0 && position < size) {
            return mData.get(position);
        }
        return null;
    }

    @NonNull
    public List<UnionTypeItemObject> getData() {
        return mData;
    }

    /**
     * {@linkplain DeepDiff}
     * {@linkplain DiffUtil}
     */
    public void setData(@Nullable Collection<UnionTypeItemObject> data) {
        List<UnionTypeItemObject> oldData = getData();
        if (data == null) {
            mData = new RangeArrayList<>();
        } else {
            mData = new RangeArrayList<>(data);
        }
        List<UnionTypeItemObject> newData = getData();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldData.size();
            }

            @Override
            public int getNewListSize() {
                return newData.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                UnionTypeItemObject oldItemObject = oldData.get(oldItemPosition);
                UnionTypeItemObject newItemObject = newData.get(newItemPosition);
                if (oldItemObject == null || newItemObject == null) {
                    return false;
                }
                return oldItemObject.isSameItem(newItemObject);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                UnionTypeItemObject oldItemObject = oldData.get(oldItemPosition);
                UnionTypeItemObject newItemObject = newData.get(newItemPosition);
                if (oldItemObject == null || newItemObject == null) {
                    return false;
                }
                return oldItemObject.isSameContent(newItemObject);
            }
        });
        diffResult.dispatchUpdatesTo(this);
    }

    public void insertLastData(@Nullable Collection<UnionTypeItemObject> items) {
        insertData(getItemCount(), items);
    }

    public void insertFirstData(@Nullable Collection<UnionTypeItemObject> items) {
        insertData(0, items);
    }

    public void insertData(int position, @Nullable Collection<UnionTypeItemObject> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int size = items.size();
        int count = getItemCount();
        if (position < 0 || position > count) {
            Timber.e(new IllegalArgumentException(), "invalid position:%s, item count:%s", position, count);
            return;
        }
        mData.addAll(position, items);
        notifyItemRangeInserted(position, size);
    }

    public void removeData(int position) {
        removeData(position, 1);
    }

    public void removeData(int position, int size) {
        if (size <= 0) {
            return;
        }

        int count = getItemCount();
        if (position < 0 || position >= count) {
            Timber.e(new IllegalArgumentException(), "invalid position:%s, item count:%s", position, count);
            return;
        }
        if (position + size > count) {
            Timber.e(new IllegalArgumentException(), "invalid position:%s, size:%s, item count:%s", position, size, count);
            return;
        }

        mData.removeRange(position, position + size);
        notifyItemRangeRemoved(position, size);
    }

    @Override
    public int getItemCount() {
        return mData.size();
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

    private static class RangeArrayList<E> extends ArrayList<E> {
        public RangeArrayList(int initialCapacity) {
            super(initialCapacity);
        }

        public RangeArrayList() {
        }

        public RangeArrayList(@NonNull Collection c) {
            super(c);
        }

        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }

}

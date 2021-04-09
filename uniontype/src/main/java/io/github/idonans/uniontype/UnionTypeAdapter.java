package io.github.idonans.uniontype;

import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnionTypeAdapter extends RecyclerView.Adapter<UnionTypeViewHolder> {

    private GroupArrayList mData = new GroupArrayList();
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
        return mData.getItem(position);
    }

    @Nullable
    public UnionTypeItemObject getGroupItem(int group, int positionInGroup) {
        return mData.getGroupItem(group, positionInGroup);
    }

    public int getGroupPositionStart(int group) {
        return mData.getGroupPositionStart(group);
    }

    @Nullable
    public int[] getGroupAndPosition(int position) {
        return mData.getGroupAndPosition(position);
    }

    @NonNull
    public GroupArrayList getData() {
        return mData;
    }

    /**
     * {@linkplain DeepDiff}
     * {@linkplain DiffUtil}
     */
    public void setData(@Nullable GroupArrayList data) {
        GroupArrayList oldData = getData();
        if (data == null) {
            mData = new GroupArrayList();
        } else {
            mData = new GroupArrayList(data);
        }
        GroupArrayList newData = getData();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldData.getItemCount();
            }

            @Override
            public int getNewListSize() {
                return newData.getItemCount();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                UnionTypeItemObject oldItemObject = oldData.getItem(oldItemPosition);
                UnionTypeItemObject newItemObject = newData.getItem(newItemPosition);
                if (oldItemObject == null || newItemObject == null) {
                    return false;
                }
                return oldItemObject.isSameItem(newItemObject);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                UnionTypeItemObject oldItemObject = oldData.getItem(oldItemPosition);
                UnionTypeItemObject newItemObject = newData.getItem(newItemPosition);
                if (oldItemObject == null || newItemObject == null) {
                    return false;
                }
                return oldItemObject.isSameContent(newItemObject);
            }
        });
        diffResult.dispatchUpdatesTo(this);
    }

    public void setGroupItems(int group, Collection<UnionTypeItemObject> items) {
        List<UnionTypeItemObject> groupItemsOld = new ArrayList<>();
        List<UnionTypeItemObject> groupItemsNew = new ArrayList<>();

        List<UnionTypeItemObject> groupItemsOriginal = mData.getGroupItems(group);
        if (groupItemsOriginal != null) {
            groupItemsOld.addAll(groupItemsOriginal);
        }
        if (items != null) {
            groupItemsNew.addAll(items);
        }

        int groupPositionStart = mData.getGroupPositionStart(group);
        mData.setGroupItems(group, items);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return groupItemsOld.size();
            }

            @Override
            public int getNewListSize() {
                return groupItemsNew.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                UnionTypeItemObject oldItemObject = groupItemsOld.get(oldItemPosition);
                UnionTypeItemObject newItemObject = groupItemsNew.get(newItemPosition);
                if (oldItemObject == null || newItemObject == null) {
                    return false;
                }
                return oldItemObject.isSameItem(newItemObject);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                UnionTypeItemObject oldItemObject = groupItemsOld.get(oldItemPosition);
                UnionTypeItemObject newItemObject = groupItemsNew.get(newItemPosition);
                if (oldItemObject == null || newItemObject == null) {
                    return false;
                }
                return oldItemObject.isSameContent(newItemObject);
            }
        });
        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(groupPositionStart + position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(groupPositionStart + position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(groupPositionStart + fromPosition, groupPositionStart + toPosition);
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                notifyItemRangeChanged(groupPositionStart + position, count, payload);
            }
        });
    }

    public boolean insertGroupItems(int group, int positionInGroup, Collection<UnionTypeItemObject> items) {
        int[] positionAndSize = mData.insertGroupItems(group, positionInGroup, items);
        if (positionAndSize != null) {
            notifyItemRangeInserted(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean appendGroupItems(int group, Collection<UnionTypeItemObject> items) {
        int[] positionAndSize = mData.appendGroupItems(group, items);
        if (positionAndSize != null) {
            notifyItemRangeInserted(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean clearGroupItems(int group) {
        int[] positionAndSize = mData.clearGroupItems(group);
        if (positionAndSize != null) {
            notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean removeGroupItem(int group, int positionInGroup) {
        int[] positionAndSize = mData.removeGroupItem(group, positionInGroup);
        if (positionAndSize != null) {
            notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean removeGroupItems(int group, int positionInGroup, int size) {
        int[] positionAndSize = mData.removeGroupItems(group, positionInGroup, size);
        if (positionAndSize != null) {
            notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean removeItem(int position) {
        int[] positionAndSize = mData.removeItem(position);
        if (positionAndSize != null) {
            notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean removeItems(int position, GroupArrayList.Filter filter) {
        int[] positionAndSize = mData.removeItems(position, filter);
        if (positionAndSize != null) {
            notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
            return true;
        }
        return false;
    }

    public boolean move(int fromPosition, int toPosition) {
        int[] movePosition = mData.move(fromPosition, toPosition);
        if (movePosition != null) {
            notifyItemMoved(movePosition[0], movePosition[1]);
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return mData.getItemCount();
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

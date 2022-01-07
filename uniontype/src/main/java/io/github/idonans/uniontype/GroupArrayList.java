package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupArrayList {

    @NonNull
    private final SparseArrayCompat<ArrayListWrapper> mData;

    GroupArrayList() {
        this(null);
    }

    GroupArrayList(@Nullable GroupArrayList input) {
        mData = new SparseArrayCompat<>();
        if (input != null) {
            int size = input.mData.size();
            for (int i = 0; i < size; i++) {
                int key = input.mData.keyAt(i);
                final ArrayListWrapper groupItems = input.mData.valueAt(i);
                mData.put(key, groupItems == null ? null : new ArrayListWrapper(groupItems));
            }
        }
    }

    @Nullable
    public List<UnionTypeItemObject> getGroupItems(int group) {
        return mData.get(group);
    }

    /**
     * 获得指定组下的数据数量，如果该组下没有数据，返回0．
     */
    public int getGroupItemsSize(int group) {
        final ArrayListWrapper groupItems = mData.get(group);
        if (groupItems == null) {
            return 0;
        }

        return groupItems.size();
    }

    /**
     * 清空指定组下的数据
     */
    public void clearGroupItems(int group) {
        final ArrayListWrapper groupItems = mData.get(group);
        if (groupItems == null) {
            return;
        }

        groupItems.clear();
    }

    /**
     * 删除指定组
     *
     * @param group 分组
     */
    public void removeGroup(int group) {
        mData.remove(group);
    }

    /**
     * @param group 分组
     * @return 获取指定组在全局所在的开始位置, 总是 <code>>=0</code>
     */
    public int getGroupPositionStart(int group) {
        int position = 0;

        int size = mData.size();
        for (int i = 0; i < size; i++) {
            int groupNum = mData.keyAt(i);
            if (groupNum >= group) {
                break;
            } else {
                final ArrayListWrapper groupItems = mData.valueAt(i);
                if (groupItems != null) {
                    position += groupItems.size();
                }
            }
        }

        return position;
    }

    /**
     * 清除指定组下指定位置的数据
     */
    @Nullable
    public UnionTypeItemObject removeGroupItem(int group, int positionInGroup) {
        if (positionInGroup < 0) {
            return null;
        }

        final ArrayListWrapper groupItems = mData.get(group);
        if (groupItems == null) {
            return null;
        }

        if (groupItems.size() <= positionInGroup) {
            return null;
        }

        return groupItems.remove(positionInGroup);
    }

    /**
     * 清除指定组下指定位置区域的数据
     */
    public void removeGroupItems(int group, int positionInGroup, int size) {
        if (positionInGroup < 0) {
            return;
        }

        if (size <= 0) {
            return;
        }

        final ArrayListWrapper groupItems = mData.get(group);
        if (groupItems == null) {
            return;
        }

        if (groupItems.size() < positionInGroup + size) {
            if (groupItems.size() > positionInGroup) {
                groupItems.removeRangeWrapper(positionInGroup, groupItems.size() - positionInGroup);
            }
            return;
        }

        groupItems.removeRangeWrapper(positionInGroup, size);
    }

    /**
     * <pre>
     * 获取指定位置所在的组以及组内的位置，如果该位置没有找到，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识所在的组，总是 <code>>=0</code>
     * [1] 标识在该组内所处的位置，总是 <code>>=0</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] groupAndPosition = getGroupAndPosition(13);
     * if(groupAndPosition != null) {
     *     int group = groupAndPosition[0];
     *     int positionInGroup = groupAndPosition[1];
     * } else {
     *     // item not found
     * }
     *
     * </code>
     * </pre>
     *
     * @param position 全局位置
     * @return 指定位置所在的组以及组内的位置
     */
    public int[] getGroupAndPosition(int position) {
        if (position < 0) {
            return null;
        }

        int[] groupAndPosition = new int[2];
        int globalPosition = 0;

        int size = mData.size();
        for (int i = 0; i < size; i++) {
            int groupItemCount = 0;
            final ArrayListWrapper groupItems = mData.valueAt(i);
            if (groupItems != null) {
                groupItemCount = groupItems.size();
            }
            if (position < globalPosition + groupItemCount) {
                // position在第i组内的(position-globalPosition)位置
                groupAndPosition[0] = mData.keyAt(i);
                groupAndPosition[1] = position - globalPosition;
                return groupAndPosition;
            }
            globalPosition += groupItemCount;
        }

        return null;
    }

    /**
     * 清除指定位置的数据
     */
    public void removeItem(int position) {
        int[] groupAndPosition = getGroupAndPosition(position);
        if (groupAndPosition == null) {
            return;
        }

        removeGroupItem(groupAndPosition[0], groupAndPosition[1]);
    }

    /**
     * 清除指定位置附近的数据.
     * Filter 用来匹配需要删除的数据，所删除的数据总是在同一组并且相邻
     */
    public void removeItems(int position, Filter filter) {
        int[] groupAndPosition = getGroupAndPosition(position);
        if (groupAndPosition == null) {
            return;
        }

        final int groupItemsSize = getGroupItemsSize(groupAndPosition[0]);
        // 根据 position 确定删除的区域

        // 搜寻开始位置
        int start = -1;
        for (int i = groupAndPosition[1]; i >= 0; i--) {
            UnionTypeItemObject item = getGroupItem(groupAndPosition[0], i);
            if (!filter.filter(item)) {
                break;
            }
            start = i;
        }

        if (start < 0) {
            return;
        }

        int end = groupAndPosition[1];
        for (int i = end + 1; i < groupItemsSize; i++) {
            UnionTypeItemObject item = getGroupItem(groupAndPosition[0], i);
            if (!filter.filter(item)) {
                break;
            }
            end = i;
        }

        // 删除[start, end]区间的数据
        removeGroupItems(groupAndPosition[0], start, end - start + 1);
    }

    public interface Filter {
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean filter(UnionTypeItemObject item);
    }

    /**
     * 清除所有数据(保留分组)
     */
    public void clearAllGroupItems() {
        int size = mData.size();
        for (int i = 0; i < size; i++) {
            final ArrayListWrapper groupItems = mData.valueAt(i);
            if (groupItems != null) {
                groupItems.clear();
            }
        }
    }

    /**
     * 删除所有数据(包括分组)
     */
    public void removeAll() {
        mData.clear();
    }

    public int size() {
        int count = 0;
        int size = mData.size();
        for (int i = 0; i < size; i++) {
            final ArrayListWrapper groupItems = mData.valueAt(i);
            if (groupItems != null) {
                count += groupItems.size();
            }
        }
        return count;
    }

    public void setGroupItems(int group, @Nullable Collection<UnionTypeItemObject> items) {
        if (items == null) {
            clearGroupItems(group);
        } else {
            mData.put(group, new ArrayListWrapper(items));
        }
    }

    /**
     * 向指定组中的指定位置添加数据
     */
    public void insertGroupItems(int group, int positionInGroup, @Nullable Collection<UnionTypeItemObject> items) {
        if (items != null && items.size() > 0) {
            final ArrayListWrapper groupItems = mData.get(group);
            if (groupItems == null) {
                mData.put(group, new ArrayListWrapper(items));
                return;
            }

            if (positionInGroup <= 0) {
                groupItems.addAll(0, items);
                return;
            }

            if (positionInGroup > groupItems.size()) {
                positionInGroup = groupItems.size();
            }
            groupItems.addAll(positionInGroup, items);
        }
    }

    /**
     * 向指定组中添加数据
     */
    public void appendGroupItems(int group, @Nullable Collection<UnionTypeItemObject> items) {
        if (items != null && items.size() > 0) {
            final ArrayListWrapper groupItems = mData.get(group);
            if (groupItems == null) {
                mData.put(group, new ArrayListWrapper(items));
                return;
            }

            groupItems.addAll(items);
        }
    }

    /**
     * 如果没有找到，返回 <code>null</code>.
     */
    @Nullable
    public UnionTypeItemObject getGroupItem(int group, int positionInGroup) {
        if (positionInGroup < 0) {
            return null;
        }

        final ArrayListWrapper groupItems = mData.get(group);
        if (groupItems == null) {
            return null;
        }

        if (groupItems.size() <= positionInGroup) {
            return null;
        }

        return groupItems.get(positionInGroup);
    }

    /**
     * 如果没有找到，返回 <code>null</code>.
     */
    @Nullable
    public UnionTypeItemObject getItem(int position) {
        if (position < 0) {
            return null;
        }

        int[] groupAndPosition = getGroupAndPosition(position);
        if (groupAndPosition == null) {
            return null;
        }
        return getGroupItem(groupAndPosition[0], groupAndPosition[1]);
    }

    private static final class ArrayListWrapper extends ArrayList<UnionTypeItemObject> {
        public ArrayListWrapper(@NonNull Collection<UnionTypeItemObject> collection) {
            super(collection);
        }

        private void removeRangeWrapper(int fromIndex, int size) {
            removeRange(fromIndex, fromIndex + size);
        }
    }

}

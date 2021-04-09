package io.github.idonans.uniontype;

import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupArrayList {

    private final SparseArrayCompat<ArrayList<UnionTypeItemObject>> mData;

    public GroupArrayList() {
        mData = new SparseArrayCompat<>();
    }

    public GroupArrayList(GroupArrayList input) {
        mData = new SparseArrayCompat<>();
        if (input != null) {
            int size = input.mData.size();
            for (int i = 0; i < size; i++) {
                int key = input.mData.keyAt(i);
                ArrayList<UnionTypeItemObject> groupItems = input.mData.valueAt(i);
                mData.put(key, new ArrayListWrapper(groupItems));
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
    public int getGroupItemCount(int group) {
        ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
        if (groupItems == null) {
            return 0;
        }

        return groupItems.size();
    }

    /**
     * <pre>
     * 清除某一组数据，如果该组下没有数据，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识被清除数据在整体数据中的开始位置，总是 <code>>=0</code>
     * [1] 标识被清除的数据的长度(即清除前该组数据的数量)，总是 <code>>0</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = clearGroupItems(GROUP_DATA);
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] clearGroupItems(int group) {
        ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
        if (groupItems == null) {
            return null;
        }

        if (groupItems.isEmpty()) {
            return null;
        }

        int[] result = new int[2];
        result[0] = getGroupPositionStart(group);
        result[1] = groupItems.size();

        groupItems.clear();

        return result;
    }

    /**
     * 获取指定组在全局所在的开始位置, 总是 <code>>=0</code>
     */
    public int getGroupPositionStart(int group) {
        int position = 0;

        int size = mData.size();
        for (int i = 0; i < size; i++) {
            int groupNum = mData.keyAt(i);
            if (groupNum >= group) {
                break;
            } else {
                ArrayList<UnionTypeItemObject> groupItems = mData.valueAt(i);
                if (groupItems != null) {
                    position += groupItems.size();
                }
            }
        }

        return position;
    }

    /**
     * <pre>
     * 清除指定组下指定位置的数据，如果该数据没有找到，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识被清除数据在整体数据中的开始位置，总是 <code>>=0</code>
     * [1] 标识被清除的数据的长度，总是 <code>=1</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = removeGroupItem(GROUP_DATA, 3);
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] removeGroupItem(int group, int positionInGroup) {
        if (positionInGroup < 0) {
            return null;
        }

        ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
        if (groupItems == null) {
            return null;
        }

        if (groupItems.size() <= positionInGroup) {
            return null;
        }

        groupItems.remove(positionInGroup);

        int[] result = new int[2];
        result[0] = getGroupPositionStart(group) + positionInGroup;
        result[1] = 1;
        return result;
    }

    /**
     * <pre>
     * 清除指定组下指定位置区域的数据，如果该区域不合法，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识被清除数据在整体数据中的开始位置，总是 <code>>=0</code>
     * [1] 标识被清除的数据的长度，总是 <code>>0</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = removeGroupItems(GROUP_DATA, 3, 2); // 删除该组数据的第3项和第4项
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] removeGroupItems(int group, int positionInGroup, int size) {
        if (positionInGroup < 0) {
            return null;
        }

        if (size <= 0) {
            return null;
        }

        ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
        if (groupItems == null) {
            return null;
        }

        if (groupItems.size() < positionInGroup + size) {
            return null;
        }

        ((ArrayListWrapper) groupItems).removeRangeWrapper(positionInGroup, size);

        int[] result = new int[2];
        result[0] = getGroupPositionStart(group) + positionInGroup;
        result[1] = size;
        return result;
    }

    private static final class ArrayListWrapper extends ArrayList<UnionTypeItemObject> {
        public ArrayListWrapper(Collection<UnionTypeItemObject> collection) {
            super(collection);
        }

        private void removeRangeWrapper(int fromIndex, int size) {
            removeRange(fromIndex, fromIndex + size);
        }
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
            ArrayList<UnionTypeItemObject> groupItems = mData.valueAt(i);
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
     * <pre>
     * 清除指定位置的数据，如果该数据没有找到，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识被清除数据在整体数据中的开始位置, 总是 <code>=position</code> (传入的参数)
     * [1] 标识被清除的数据的长度，总是 <code>=1</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = removeItem(13);
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] removeItem(int position) {
        int[] groupAndPosition = getGroupAndPosition(position);
        if (groupAndPosition == null) {
            return null;
        }

        return removeGroupItem(groupAndPosition[0], groupAndPosition[1]);
    }

    /**
     * <pre>
     * 清除指定位置附近的数据，如果没有数据可以匹配，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识被清除数据在整体数据中的开始位置, 总是 <code>>=0</code>
     * [1] 标识被清除的数据的长度，总是 <code>>0</code>
     *
     * Filter 用来匹配需要删除的数据，所删除的数据总是在同一组并且相邻
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = removeItem(13, filter);
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] removeItems(int position, Filter filter) {
        int[] groupAndPosition = getGroupAndPosition(position);
        if (groupAndPosition == null) {
            return null;
        }

        int groupSize = getGroupItemCount(groupAndPosition[0]);
        // 根据position确定删除的区域

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
            return null;
        }

        int end = groupAndPosition[1];
        for (int i = end + 1; i < groupSize; i++) {
            UnionTypeItemObject item = getGroupItem(groupAndPosition[0], i);
            if (!filter.filter(item)) {
                break;
            }
            end = i;
        }

        // 删除[start, end]区间的数据
        return removeGroupItems(groupAndPosition[0], start, end - start + 1);
    }

    public interface Filter {
        boolean filter(UnionTypeItemObject item);
    }

    /**
     * <pre>
     * 将数据从一个位置移动到另一个位置, 如果移动失败，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识移动前的位置
     * [1] 标识移动后的位置
     * 不同的ViewType之间不能移动
     *
     * 使用示例：
     * <code>
     *
     * int[] movePosition = move(fromPosition, toPosition);
     * if (movePosition != null) {
     *     mAdapter.notifyItemMoved(movePosition[0], movePosition[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] move(int fromPosition, int toPosition) {
        if (fromPosition < 0
                || toPosition < 0
                || fromPosition == toPosition) {
            return null;
        }

        int[] groupAndPositionFrom = getGroupAndPosition(fromPosition);
        int[] groupAndPositionTo = getGroupAndPosition(toPosition);

        if (groupAndPositionFrom == null
                || groupAndPositionTo == null) {
            return null;
        }

        int itemViewTypeFrom = getGroupItemViewType(fromPosition, groupAndPositionFrom[0], groupAndPositionFrom[1]);
        int itemViewTypeTo = getGroupItemViewType(toPosition, groupAndPositionTo[0], groupAndPositionTo[1]);

        if (itemViewTypeFrom == itemViewTypeTo) {
            // 类型相同，可以直接移动
            if (groupAndPositionFrom[0] == groupAndPositionTo[0]) {
                // 同组内移动
                ArrayList<UnionTypeItemObject> groupItems = mData.get(groupAndPositionFrom[0]);
                UnionTypeItemObject object = groupItems.remove(groupAndPositionFrom[1]);
                groupItems.add(groupAndPositionTo[1], object);
            } else {
                // 不同组之间移动
                ArrayList<UnionTypeItemObject> groupItemsFrom = mData.get(groupAndPositionFrom[0]);
                ArrayList<UnionTypeItemObject> groupItemsTo = mData.get(groupAndPositionTo[0]);
                UnionTypeItemObject object = groupItemsFrom.remove(groupAndPositionFrom[1]);

                if (fromPosition > toPosition) {
                    groupItemsTo.add(groupAndPositionTo[1], object);
                } else {
                    groupItemsTo.add(groupAndPositionTo[1] + 1, object);
                }
            }
            return new int[]{fromPosition, toPosition};
        }

        /*
         * 从from到to的方向，to后面的一个位置. 当前位置不能移动，但是当前位置的下一个位置可以移动时，仍然要处理移动。
         * 此时应当移动到下一个位置前面(向移动前的位置的方向)。
         */
        int positionToNext;
        int[] groupAndPositionToNext;
        if (fromPosition < toPosition) {
            positionToNext = toPosition + 1;
        } else {
            positionToNext = toPosition - 1;
        }
        groupAndPositionToNext = getGroupAndPosition(positionToNext);
        if (groupAndPositionToNext == null) {
            // 没有可移动的下一个位置
            return null;
        }

        // try move to before positionToNext
        int itemViewTypeToNext = getGroupItemViewType(positionToNext, groupAndPositionToNext[0], groupAndPositionToNext[1]);

        if (itemViewTypeFrom == itemViewTypeToNext) {
            // 类型相同，可以移动
            if (groupAndPositionFrom[0] == groupAndPositionToNext[0]) {
                // 同组内移动
                ArrayList<UnionTypeItemObject> groupItems = mData.get(groupAndPositionFrom[0]);
                UnionTypeItemObject object = groupItems.remove(groupAndPositionFrom[1]);
                groupItems.add(groupAndPositionToNext[1], object);
            } else {
                // 不同组之间移动
                ArrayList<UnionTypeItemObject> groupItemsFrom = mData.get(groupAndPositionFrom[0]);
                ArrayList<UnionTypeItemObject> groupItemsToNext = mData.get(groupAndPositionToNext[0]);
                UnionTypeItemObject object = groupItemsFrom.remove(groupAndPositionFrom[1]);
                if (fromPosition > toPosition) {
                    groupItemsToNext.add(groupAndPositionToNext[1] + 1, object);
                } else {
                    groupItemsToNext.add(groupAndPositionToNext[1], object);
                }
            }
            return new int[]{fromPosition, toPosition};
        }

        return null;
    }

    public int getGroupItemViewType(int position, int group, int positionInGroup) {
        UnionTypeItemObject itemObject = getGroupItem(group, positionInGroup);
        if (itemObject == null) {
            return UnionTypeMapper.UNION_TYPE_NULL;
        }
        return itemObject.unionType;
    }


    /**
     * <pre>
     * 清除所有数据，如果该数据没有找到，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识被清除数据在整体数据中的开始位置, 总是 <code>=0</code>
     * [1] 标识被清除的数据的长度，总是 <code>>0</code> (与此前整个数据的长度相等)
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = clearAll();
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeRemoved(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] clearAll() {
        int count = getItemCount();
        if (count <= 0) {
            return null;
        }
        mData.clear();
        return new int[]{0, count};
    }

    public int getItemCount() {
        int count = 0;
        int size = mData.size();
        for (int i = 0; i < size; i++) {
            ArrayList<UnionTypeItemObject> groupItems = mData.valueAt(i);
            if (groupItems != null) {
                count += groupItems.size();
            }
        }
        return count;
    }

    /**
     * @see #clearGroupItems(int)
     * @see #appendGroupItems(int, Collection)
     */
    public void setGroupItems(int group, Collection<UnionTypeItemObject> items) {
        if (items == null) {
            clearGroupItems(group);
        } else {
            mData.put(group, new ArrayListWrapper(items));
        }
    }

    /**
     * <pre>
     * 向指定组中的指定位置添加数据，如果数据为空，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识添加的数据在整体数据中的开始位置 总是 <code>>=0</code>
     * [1] 标识添加的数据的长度，总是 <code>>0</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = insertGroupItems(GROUP_DATA, 2, items);
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeInserted(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] insertGroupItems(int group, int positionInGroup, Collection<UnionTypeItemObject> items) {
        if (items != null && items.size() > 0) {
            ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
            if (groupItems == null) {

                if (positionInGroup != 0) {
                    return null;
                }

                groupItems = new ArrayListWrapper(items);
                mData.put(group, groupItems);
                return new int[]{getGroupPositionStart(group), items.size()};
            } else {
                int oldSize = groupItems.size();
                if (oldSize < positionInGroup) {
                    return null;
                }

                groupItems.addAll(positionInGroup, items);
                return new int[]{getGroupPositionStart(group) + positionInGroup, items.size()};
            }
        } else {
            return null;
        }
    }

    /**
     * <pre>
     * 向指定组中添加数据，如果数据为空，返回 <code>null</code>.
     * 否则返回一个长度为 2 的整数数组，其中
     * [0] 标识添加的数据在整体数据中的开始位置 总是 <code>>=0</code>
     * [1] 标识添加的数据的长度，总是 <code>>0</code>
     *
     * 使用示例：
     * <code>
     *
     * int[] positionAndSize = appendGroupItems(GROUP_DATA, items);
     * if(positionAndSize != null) {
     *     mAdapter.notifyItemRangeInserted(positionAndSize[0], positionAndSize[1]);
     * }
     *
     * </code>
     * </pre>
     */
    public int[] appendGroupItems(int group, Collection<UnionTypeItemObject> items) {
        if (items != null && items.size() > 0) {
            ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
            if (groupItems == null) {
                groupItems = new ArrayListWrapper(items);
                mData.put(group, groupItems);
                return new int[]{getGroupPositionStart(group), items.size()};
            } else {
                int positionInGroup = groupItems.size();
                groupItems.addAll(items);
                return new int[]{getGroupPositionStart(group) + positionInGroup, items.size()};
            }
        } else {
            return null;
        }
    }

    /**
     * 如果没有找到，返回 <code>null</code>.
     */
    public UnionTypeItemObject getGroupItem(int group, int positionInGroup) {
        if (positionInGroup < 0) {
            return null;
        }

        ArrayList<UnionTypeItemObject> groupItems = mData.get(group);
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

}

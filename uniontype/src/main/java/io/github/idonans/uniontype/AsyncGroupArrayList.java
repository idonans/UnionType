package io.github.idonans.uniontype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.thread.TaskQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;

public class AsyncGroupArrayList {

    private final ListUpdateCallback mListUpdateCallback;

    @NonNull
    private GroupArrayList mGroupArrayListOrigin;
    @NonNull
    private ReadOnly mReadOnly;

    @NonNull
    private final Object mTransactionListLock = new Object();
    @NonNull
    private List<Transaction> mTransactionList = new ArrayList<>();
    private final TaskQueue mTransactionActionQueue = new TaskQueue(1);

    public AsyncGroupArrayList(@NonNull RecyclerView.Adapter<?> adapter) {
        this(new AdapterListUpdateCallback(adapter));
    }

    public AsyncGroupArrayList(@NonNull final ListUpdateCallback listUpdateCallback) {
        mListUpdateCallback = listUpdateCallback;
        mGroupArrayListOrigin = new GroupArrayList();
        mReadOnly = new ReadOnly(mGroupArrayListOrigin);
    }

    @NonNull
    public ReadOnly getReadOnly() {
        return mReadOnly;
    }

    @NonNull
    public Transaction beginTransaction() {
        return new Transaction(this);
    }

    private void commit(@NonNull Transaction transaction) {
        synchronized (mTransactionListLock) {
            mTransactionList.add(transaction);
        }
        mTransactionActionQueue.skipQueue();
        mTransactionActionQueue.enqueue(new TransactionAction());
    }

    private class TransactionAction implements Runnable {
        @Override
        public void run() {
            final List<Transaction> transactionList;
            synchronized (mTransactionListLock) {
                transactionList = mTransactionList;
                mTransactionList = new ArrayList<>();
            }
            if (transactionList.isEmpty()) {
                return;
            }

            boolean detectMoves = false;
            boolean forbiddenMoves = false;
            final GroupArrayList oldList = mGroupArrayListOrigin;
            final GroupArrayList newList = new GroupArrayList(oldList);
            for (Transaction transaction : transactionList) {
                for (Transaction.Action action : transaction.mActionList) {
                    action.onAction(transaction, newList);
                }
                detectMoves |= transaction.mDetectMoves;
                forbiddenMoves |= transaction.mForbiddenMoves;
            }
            final ReadOnly readOnly = new ReadOnly(newList);

            // cal diff
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    UnionTypeItemObject oldItemObject = oldList.getItem(oldItemPosition);
                    UnionTypeItemObject newItemObject = newList.getItem(newItemPosition);
                    if (oldItemObject == null || newItemObject == null) {
                        return oldItemObject == newItemObject;
                    }
                    return oldItemObject.isSameItem(newItemObject);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    UnionTypeItemObject oldItemObject = oldList.getItem(oldItemPosition);
                    UnionTypeItemObject newItemObject = newList.getItem(newItemPosition);
                    if (oldItemObject == null || newItemObject == null) {
                        return oldItemObject == newItemObject;
                    }
                    return oldItemObject.isSameContent(newItemObject);
                }
            }, detectMoves && !forbiddenMoves);

            Threads.postUi(() -> {
                for (Transaction transaction : transactionList) {
                    if (transaction.mBatchCommitStartCallback != null) {
                        transaction.mBatchCommitStartCallback.run();
                    }
                }
                synchronized (mTransactionActionQueue) {
                    mGroupArrayListOrigin = newList;
                    mReadOnly = readOnly;
                    mTransactionActionQueue.notify();
                }
                diffResult.dispatchUpdatesTo(mListUpdateCallback);
                for (Transaction transaction : transactionList) {
                    if (transaction.mBatchCommitEndCallback != null) {
                        transaction.mBatchCommitEndCallback.run();
                    }
                }
            });
            synchronized (mTransactionActionQueue) {
                int loop = 0;
                final long timeStart = System.currentTimeMillis();
                while (mReadOnly != readOnly) {
                    UnionTypeLog.v("TransactionActionQueue[%s] wait diff result post success loop:%s, time interval ms:%s", this, loop, System.currentTimeMillis() - timeStart);
                    try {
                        mTransactionActionQueue.wait(1000);
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
        }
    }

    public static class Transaction {

        @NonNull
        private final AsyncGroupArrayList mAsyncGroupArrayList;
        @NonNull
        private final List<Action> mActionList = new ArrayList<>();
        @Nullable
        private Runnable mBatchCommitStartCallback;
        @Nullable
        private Runnable mBatchCommitEndCallback;

        private boolean mCommit;
        private boolean mDetectMoves;
        private boolean mForbiddenMoves;

        private Transaction(@NonNull AsyncGroupArrayList asyncGroupArrayList) {
            mAsyncGroupArrayList = asyncGroupArrayList;
        }

        @NonNull
        public Transaction setDetectMoves(boolean detectMoves) {
            mDetectMoves = detectMoves;
            return this;
        }

        @NonNull
        public Transaction setForbiddenMoves(boolean forbiddenMoves) {
            mForbiddenMoves = forbiddenMoves;
            return this;
        }

        @NonNull
        public Transaction add(@NonNull Action action) {
            Preconditions.checkArgument(!mCommit);
            mActionList.add(action);
            return this;
        }

        public void commit() {
            this.commit(null);
        }

        public void commit(@Nullable Runnable batchCommitEndCallback) {
            this.commit(null, batchCommitEndCallback);
        }

        public void commit(@Nullable Runnable batchCommitStartCallback,
                           @Nullable Runnable batchCommitEndCallback) {
            Preconditions.checkArgument(!mCommit);
            mCommit = true;
            mBatchCommitStartCallback = batchCommitStartCallback;
            mBatchCommitEndCallback = batchCommitEndCallback;
            mAsyncGroupArrayList.commit(this);
        }

        public interface Action {
            void onAction(@NonNull Transaction transaction, @NonNull GroupArrayList groupArrayList);
        }

    }

    public static final class ReadOnly {

        @NonNull
        private final GroupArrayList mGroupArrayListReadOnly;

        private ReadOnly(@NonNull GroupArrayList groupArrayList) {
            mGroupArrayListReadOnly = new GroupArrayList(groupArrayList);
        }

        public int getGroupItemsSize(int group) {
            return mGroupArrayListReadOnly.getGroupItemsSize(group);
        }

        public int getGroupPositionStart(int group) {
            return mGroupArrayListReadOnly.getGroupPositionStart(group);
        }

        @Nullable
        public int[] getGroupAndPosition(int position) {
            return mGroupArrayListReadOnly.getGroupAndPosition(position);
        }

        public int size() {
            return mGroupArrayListReadOnly.size();
        }

        @Nullable
        public UnionTypeItemObject getGroupItem(int group, int positionInGroup) {
            return mGroupArrayListReadOnly.getGroupItem(group, positionInGroup);
        }

        @Nullable
        public UnionTypeItemObject getItem(int position) {
            return mGroupArrayListReadOnly.getItem(position);
        }
    }

}

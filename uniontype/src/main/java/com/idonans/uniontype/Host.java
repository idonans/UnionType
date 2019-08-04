package com.idonans.uniontype;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.idonans.lang.util.ContextUtil;

public interface Host {

    @Nullable
    Activity getActivity();

    @Nullable
    Fragment getFragment();

    @NonNull
    LayoutInflater getLayoutInflater();

    @NonNull
    RecyclerView getRecyclerView();

    @NonNull
    UnionTypeAdapter getAdapter();

    class Factory {

        public static Host create(Fragment fragment, RecyclerView recyclerView, UnionTypeAdapter adapter) {
            return new FragmentHost(fragment, recyclerView, adapter);
        }

        public static Host create(Activity activity, RecyclerView recyclerView, UnionTypeAdapter adapter) {
            return new ActivityHost(activity, recyclerView, adapter);
        }

    }

    abstract class BaseHost implements Host {

        private final RecyclerView mRecyclerView;
        private final UnionTypeAdapter mAdapter;

        protected BaseHost(RecyclerView recyclerView, UnionTypeAdapter adapter) {
            mRecyclerView = recyclerView;
            mAdapter = adapter;
        }

        @Override
        public UnionTypeAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }

    class FragmentHost extends BaseHost {

        private final Fragment mFragment;

        public FragmentHost(Fragment fragment, RecyclerView recyclerView, UnionTypeAdapter adapter) {
            super(recyclerView, adapter);
            mFragment = fragment;
        }


        @Nullable
        @Override
        public Activity getActivity() {
            if (mFragment == null) {
                return null;
            }
            return mFragment.getActivity();
        }

        @Nullable
        @Override
        public Fragment getFragment() {
            return mFragment;
        }

        @Override
        public LayoutInflater getLayoutInflater() {
            LayoutInflater inflater = null;
            if (mFragment != null) {
                Context context = mFragment.getContext();
                if (context != null) {
                    inflater = mFragment.getLayoutInflater();
                    if (inflater == null) {
                        if (context instanceof Activity) {
                            inflater = ((Activity) context).getLayoutInflater();
                        }
                    }
                }
            }
            if (inflater == null) {
                inflater = LayoutInflater.from(ContextUtil.getContext());
            }
            return inflater;
        }

    }

    class ActivityHost extends BaseHost {

        private final Activity mActivity;

        public ActivityHost(Activity activity, RecyclerView recyclerView, UnionTypeAdapter adapter) {
            super(recyclerView, adapter);
            mActivity = activity;
        }

        @Nullable
        @Override
        public Activity getActivity() {
            return mActivity;
        }

        @Nullable
        @Override
        public Fragment getFragment() {
            return null;
        }

        @Override
        public LayoutInflater getLayoutInflater() {
            LayoutInflater inflater = null;
            if (mActivity != null) {
                inflater = mActivity.getLayoutInflater();
            }
            if (inflater == null) {
                inflater = LayoutInflater.from(ContextUtil.getContext());
            }
            return inflater;
        }

    }

}

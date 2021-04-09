package io.github.idonans.uniontype;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.idonans.core.util.ContextUtil;

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

        public static Host create(@NonNull Fragment fragment,
                                  @NonNull RecyclerView recyclerView,
                                  @NonNull UnionTypeAdapter adapter) {
            return new FragmentHost(fragment, recyclerView, adapter);
        }

        public static Host create(@NonNull Activity activity,
                                  @NonNull RecyclerView recyclerView,
                                  @NonNull UnionTypeAdapter adapter) {
            return new ActivityHost(activity, recyclerView, adapter);
        }

        public static Host create(@NonNull Host baseHost,
                                  @NonNull RecyclerView recyclerView,
                                  @NonNull UnionTypeAdapter adapter) {
            return new HostWrapper(baseHost, recyclerView, adapter);
        }

    }

    abstract class BaseHost implements Host {

        @NonNull
        private final RecyclerView mRecyclerView;
        @NonNull
        private final UnionTypeAdapter mAdapter;

        protected BaseHost(@NonNull RecyclerView recyclerView, @NonNull UnionTypeAdapter adapter) {
            mRecyclerView = recyclerView;
            mAdapter = adapter;
        }

        @NonNull
        @Override
        public UnionTypeAdapter getAdapter() {
            return mAdapter;
        }

        @NonNull
        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }

    class FragmentHost extends BaseHost {

        private final Fragment mFragment;

        public FragmentHost(Fragment fragment,
                            @NonNull RecyclerView recyclerView,
                            @NonNull UnionTypeAdapter adapter) {
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

        @NonNull
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

        public ActivityHost(Activity activity,
                            @NonNull RecyclerView recyclerView,
                            @NonNull UnionTypeAdapter adapter) {
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

        @NonNull
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

    class HostWrapper extends BaseHost {

        private final Host mHost;

        public HostWrapper(Host host,
                           @NonNull RecyclerView recyclerView,
                           @NonNull UnionTypeAdapter adapter) {
            super(recyclerView, adapter);
            mHost = host;
        }

        @Nullable
        @Override
        public Activity getActivity() {
            return mHost.getActivity();
        }

        @Nullable
        @Override
        public Fragment getFragment() {
            return mHost.getFragment();
        }

        @NonNull
        @Override
        public LayoutInflater getLayoutInflater() {
            return mHost.getLayoutInflater();
        }

    }

}

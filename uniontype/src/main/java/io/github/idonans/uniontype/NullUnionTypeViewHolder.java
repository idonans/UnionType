package io.github.idonans.uniontype;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.annotation.NonNull;

/**
 * 空占位, 宽度与高度都是 0.
 */
public class NullUnionTypeViewHolder extends UnionTypeViewHolder {

    public NullUnionTypeViewHolder(@NonNull Host host) {
        super(host, createNullView(host.getRecyclerView().getContext()));
    }

    @Override
    public void onBindUpdate() {
    }

    private static View createNullView(Context context) {
        Space view = new Space(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        return view;
    }

}

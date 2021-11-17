package io.github.idonans.uniontype;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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

}

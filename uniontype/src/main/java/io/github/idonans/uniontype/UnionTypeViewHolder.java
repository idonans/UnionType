package io.github.idonans.uniontype;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class UnionTypeViewHolder extends RecyclerView.ViewHolder {

    @NonNull
    public final Host host;

    public UnionTypeViewHolder(@NonNull Host host, @LayoutRes int layout) {
        this(host, host.getLayoutInflater().inflate(layout, host.getRecyclerView(), false));
    }

    public UnionTypeViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(itemView);
        this.host = host;
    }

    public abstract void onBind(int position, @NonNull Object itemObject);

}

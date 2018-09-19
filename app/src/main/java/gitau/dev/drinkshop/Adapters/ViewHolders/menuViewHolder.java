package gitau.dev.drinkshop.Adapters.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import gitau.dev.drinkshop.Interface.IItemClickListener;
import gitau.dev.drinkshop.R;

public class menuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public ImageView product_image;
    public TextView txt_product_name;

    IItemClickListener itemClickListener;

    public void setItemClickListener(IItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public menuViewHolder(@NonNull View itemView) {
        super(itemView);

        product_image = itemView.findViewById(R.id.image_product);
        txt_product_name = itemView.findViewById(R.id.txt_menu_name);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, false);
    }

    @Override
    public boolean onLongClick(View view) {
        itemClickListener.onClick(view, true);
        return true;
    }
}

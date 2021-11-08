package org.techtown.recipe.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.techtown.recipe.R;

import java.util.ArrayList;


public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> implements OnMainItemClickListener {

    ArrayList<MainItem> items=new ArrayList<MainItem>();

    OnMainItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
        View itemView=inflater.inflate(R.layout.main_item,viewGroup,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder viewHolder, int position) {
        MainItem item=items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(MainItem item){
        items.add(item);
    }

    public void setItems(ArrayList<MainItem> items){
        this.items=items;
    }

    public MainItem getItem(int position){
        return items.get(position);
    }
    public void setOnItemClickListener(OnMainItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onItemClick(holder,view,position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mainImageView;
        TextView mainTitleTextView;

        public ViewHolder(@NonNull View itemView, final OnMainItemClickListener listener) {
            super(itemView);
            mainImageView=itemView.findViewById(R.id.mainImageView);
            mainTitleTextView=itemView.findViewById(R.id.mainTitleTextView);
            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition();
                    if(listener!=null){
                        listener.onItemClick(ViewHolder.this,view,position);
                    }
                }
            });
        }
        public void setItem(MainItem item){
            String imgPath=item.getMenuImg();

            if(imgPath!=null&&!imgPath.equals("")){
                Glide.with(itemView.getContext()).load(imgPath).into(mainImageView);
            }
            mainTitleTextView.setText(item.getRecipeTitle());

        }
    }
}
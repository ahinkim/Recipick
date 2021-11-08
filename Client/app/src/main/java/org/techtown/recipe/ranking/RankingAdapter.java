package org.techtown.recipe.ranking;

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


public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> implements OnRankingItemClickListener {

    ArrayList<RankingItem> items=new ArrayList<RankingItem>();

    OnRankingItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
        View itemView=inflater.inflate(R.layout.ranking_item,viewGroup,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingAdapter.ViewHolder viewHolder, int position) {
        RankingItem item=items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(RankingItem item){
        items.add(item);
    }

    public void setItems(ArrayList<RankingItem> items){
        this.items=items;
    }

    public RankingItem getItem(int position){
        return items.get(position);
    }
    public void setOnItemClickListener(OnRankingItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onItemClick(holder,view,position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView rankingImageView;
        TextView rankingTitleTextView;
        TextView rankingNumberTextView;

        public ViewHolder(@NonNull View itemView, final OnRankingItemClickListener listener) {
            super(itemView);
            rankingImageView=itemView.findViewById(R.id.rankingImageView);
            rankingTitleTextView=itemView.findViewById(R.id.rankingTitleTextView);
            rankingNumberTextView=itemView.findViewById(R.id.rankingNumberTextView);
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
        public void setItem(RankingItem item){
            String imgPath=item.getMenuImg();

            if(imgPath!=null&&!imgPath.equals("")){
                Glide.with(itemView.getContext()).load(imgPath).into(rankingImageView);
            }
            rankingTitleTextView.setText(item.getRecipeTitle());
            rankingNumberTextView.setText(item.getRank());
        }
    }
}
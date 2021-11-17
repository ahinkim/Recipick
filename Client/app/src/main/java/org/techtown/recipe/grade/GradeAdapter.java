package org.techtown.recipe.grade;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.recipe.R;

import java.util.ArrayList;


public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ViewHolder> implements OnGradeItemClickListener {

    ArrayList<GradeItem> items=new ArrayList<GradeItem>();

    OnGradeItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
        View itemView=inflater.inflate(R.layout.grade_item,viewGroup,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeAdapter.ViewHolder viewHolder, int position) {
        GradeItem item=items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(GradeItem item){
        items.add(item);
    }

    public void setItems(ArrayList<GradeItem> items){
        this.items=items;
    }

    public GradeItem getItem(int position){
        return items.get(position);
    }
    public void setOnItemClickListener(OnGradeItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onItemClick(holder,view,position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView gradeUserIdTextView;
        TextView gradeCommentTextView;
        RatingBar ratingBar;
        ImageButton popupImageButton;

        public ViewHolder(@NonNull View itemView, final OnGradeItemClickListener listener) {
            super(itemView);
            gradeUserIdTextView=itemView.findViewById(R.id.gradeUserIdTextView);
            gradeCommentTextView=itemView.findViewById(R.id.gradeCommentTextView);
            ratingBar=itemView.findViewById(R.id.littleRatingBar);

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
        public void setItem(GradeItem item){
            gradeUserIdTextView.setText(item.getUserId());
            gradeCommentTextView.setText(item.getComment());
            float rating=0;
            if("5".equals(item.getGrade())){
                rating=5;
            }
            else if("4".equals(item.getGrade())){
                rating=4;
            }
            else if("3".equals(item.getGrade())){
                rating=3;
            }
            else if("2".equals(item.getGrade())){
                rating=2;
            }
            else if("1".equals(item.getGrade())){
                rating=1;
            }
            else{
                rating=0;
            }
            ratingBar.setRating(rating);
        }
    }
}
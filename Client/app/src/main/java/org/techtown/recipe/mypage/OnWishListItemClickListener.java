package org.techtown.recipe.mypage;

import android.view.View;

import org.techtown.recipe.main.MainAdapter;
import org.techtown.recipe.mypage.WishListAdapter;

public interface OnWishListItemClickListener {
    public void onItemClick(WishListAdapter.ViewHolder holder, View view, int position);
}
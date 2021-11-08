package org.techtown.recipe.main;

import android.view.View;

import org.techtown.recipe.main.MainAdapter;

public interface OnOrderItemClickListener {
    public void onItemClick(OrderAdapter.ViewHolder holder, View view, int position);
}
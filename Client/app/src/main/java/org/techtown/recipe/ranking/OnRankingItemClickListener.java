package org.techtown.recipe.ranking;

import android.view.View;

public interface OnRankingItemClickListener {
    public void onItemClick(RankingAdapter.ViewHolder holder, View view, int position);
}
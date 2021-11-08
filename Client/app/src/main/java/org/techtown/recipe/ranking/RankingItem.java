package org.techtown.recipe.ranking;

public class RankingItem {
    String rank;//레시피 랭킹
    String rId;//레시피 아이디
    String recipe_title;//레시피 제목
    String menu_img;//레시피 사진

    public RankingItem(String rank, String rId, String recipe_title, String menu_img){
        this.rank=rank;
        this.rId=rId;
        this.recipe_title=recipe_title;
        this.menu_img=menu_img;
    }

    public String getRank(){return rank;}
    public void setRank(String rank){this.rank=rank;}

    public String getRId(){return rId;}
    public void setRId(String rId){this.rId=rId;}

    public String getRecipeTitle(){
        return recipe_title;
    }
    public void setRecipeTitle(String recipe_title){
        this.recipe_title=recipe_title;
    }

    public String getMenuImg(){

        return menu_img;
    }
    public void setMenuImg(String menu_img){
        this.menu_img=menu_img;
    }
}
package org.techtown.recipe.mypage;

public class WishListItem {
    String rId;//레시피 아이디
    String recipe_title;//레시피 제목
    String menu_img;//레시피 사진

    public WishListItem(String rId,String recipe_title, String menu_img){
        this.rId=rId;
        this.recipe_title=recipe_title;
        this.menu_img=menu_img;
    }
    public String getRId(){return rId;}
    public void setRId(String rId){this.rId=rId;}

    public String getRecipeTitle(){
        return recipe_title;
    }
    public void setRecipeTitle(String recipe_title){
        this.recipe_title=recipe_title;
    }

    public String getMenuImg(){ return menu_img; }
    public void setMenuImg(String menu_img){
        this.menu_img=menu_img;
    }
}
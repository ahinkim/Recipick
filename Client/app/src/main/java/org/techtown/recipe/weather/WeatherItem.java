package org.techtown.recipe.weather;

public class WeatherItem {
    String rId;//레시피 아이디
    String recipe_title;//레시피 제목
    String menu_img;//레시피 사진
    String recipe_url;//만개의 레시피 url

    public WeatherItem(String rId,String recipe_title, String menu_img, String recipe_url){
        this.rId=rId;
        this.recipe_title=recipe_title;
        this.menu_img=menu_img;
        this.recipe_url=recipe_url;
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

    public String getRecipe_url(){return recipe_url;}
    public void setRecipe_url(String recipe_url){this.recipe_url=recipe_url;}
}
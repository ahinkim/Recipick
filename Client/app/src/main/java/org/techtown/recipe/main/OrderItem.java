package org.techtown.recipe.main;

public class OrderItem {
    String rId;//레시피 아이디
    String recipe_order;//레시피 번호
    String description;//레시피

    public OrderItem(String rId,String recipe_order, String description){
        this.rId=rId;
        this.recipe_order=recipe_order;
        this.description=description;
    }
    public String getRId(){return rId;}
    public void setRId(String rId){this.rId=rId;}

    public String getRecipe_order(){
        return recipe_order;
    }
    public void setRecipe_order(String recipe_order){
        this.recipe_order=recipe_order;
    }

    public String getDescription(){ return description; }
    public void setDescription(String description){
        this.description=description;
    }

}
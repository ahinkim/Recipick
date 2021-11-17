package org.techtown.recipe.grade;

public class GradeItem {
    String id;//평점 아이디
    String userId;
    String rId;//레시피 아이디
    String grade;//평점
    String comment;//댓글
    Integer userFlag;//1이면 해당 유저임(디폴트는 0)

    public GradeItem(String id,String userId,String rId,String grade,String comment, Integer userFlag){
        this.id=id;
        this.userId=userId;
        this.rId=rId;
        this.grade=grade;
        this.comment=comment;
        this.userFlag=userFlag;
    }
    public String getId(){return id;}
    public void setId(String id){this.id=id;}

    public String getUserId(){return userId;}
    public void setUserId(String userId){this.userId=userId;}

    public String getRId(){return rId;}
    public void setRId(String rId){this.rId=rId;}

    public String getGrade(){return grade;}
    public void setGrade(String grade){this.grade=grade;}

    public String getComment(){return comment;}
    public void setComment(String comment){this.comment=comment;}

    public Integer getUserFlag(){return userFlag;}
    public void setUserFlag(Integer userFlag){this.userFlag=userFlag;}

}
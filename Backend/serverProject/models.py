from django.db import models
import datetime

class User(models.Model): #User Table
    userId = models.CharField(max_length=100, primary_key=True)
    accessToken = models.CharField(max_length=200, blank=True, null=True)
    refreshToken = models.CharField(max_length=200, blank=True, null=True)
    password = models.CharField(max_length=100)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    # updated_at = models.DateTimeField(auto_now_add=True)
    
class R_info(models.Model): #Recipe information Table
    rId = models.AutoField(primary_key=True)
    recipe_title = models.CharField(max_length=100)
    serving = models.CharField(max_length=20)
    cookingTime = models.CharField(max_length=20)
    difficult = models.CharField(max_length=20)
    recipe_source = models.CharField(max_length=100)
    menu_img = models.CharField(max_length=100)
    recipe_category = models.CharField(max_length=100, null=True)

class R_order(models.Model): #Recipe Order Table
    rId =  models.ForeignKey('R_info', on_delete=models.CASCADE)
    recipe_order = models.IntegerField()
    description = models.CharField(max_length=500)

class R_grade(models.Model): #Recipe grade Table
    userId = models.ForeignKey('User', on_delete=models.CASCADE)
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE)
    grade = models.IntegerField()
    comment = models.TextField(null=True)  #평점 길이제한 x
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    # updated_at = models.DateTimeField(auto_now_add=True)

class MainDefault(models.Model):
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE, related_name = 'mainRecipes')

class WishList(models.Model):
    userId = models.ForeignKey('User', on_delete=models.CASCADE)
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE)

class UserPreferredCategories(models.Model): #사용자가 선호하는 카테고리 테이블
    userId = models.ForeignKey('User', on_delete=models.CASCADE, related_name = 'usersInCategory')
    category = models.CharField(max_length=100)
    created_at = models.DateTimeField(auto_now_add=True)

class RankingViews(models.Model): #랭킹 조회수 테이블
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE)
    views = models.IntegerField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

class TenThousand_R_grade(models.Model): #만개의 레시피 평점 테이블
    userId = models.CharField(max_length=100)
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE)
    grade = models.IntegerField()
    comment = models.TextField(null=True)  #평점 길이제한 x
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

class UserRecipeList(models.Model): #사용자가 등록한 레시피 리스트
    userId = models.ForeignKey('User', on_delete=models.CASCADE, related_name = 'users')
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE, related_name = 'userRecipes')



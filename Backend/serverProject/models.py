from django.db import models


class User(models.Model): #User Table
    userId = models.CharField(max_length=100, primary_key=True)
    accessToken = models.CharField(max_length=100, blank=True, null=True)
    password = models.CharField(max_length=100)
    created_at = models.DateTimeField(auto_now_add=True)
    # updated_at = models.DateTimeField(auto_now_add=True)
    
class R_info(models.Model): #Recipe information Table
    rId = models.IntegerField(primary_key=True)
    recipe_title = models.CharField(max_length=100)
    serving = models.CharField(max_length=20)
    cookingTime = models.CharField(max_length=20)
    difficult = models.CharField(max_length=20)
    recipe_source = models.CharField(max_length=100)
    menu_img = models.CharField(max_length=100)
    recipe_category = models.CharField(max_length=20)

class R_order(models.Model): #Recipe Order Table
    rId =  models.ForeignKey('R_info', on_delete=models.CASCADE)
    recipe_order = models.IntegerField()
    description = models.CharField(max_length=100)

class R_grade(models.Model): #Recipe grade Table
    userId  = models.ForeignKey('User', on_delete=models.CASCADE)
    created_at =models.DateTimeField(auto_now_add=True) # models.DateTimeField(auto_now_add=True)
    rId = models.ForeignKey('R_info', on_delete=models.CASCADE)
    grade = models.IntegerField()
    # updated_at = models.DateTimeField(auto_now_add=True)
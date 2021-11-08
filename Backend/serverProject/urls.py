from django.contrib import admin
from django.urls import path
from .views import userViews
from .views import recipeViews
from django.conf.urls import include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('users', userViews.user_list),
    path('users/validate', userViews.userValidate),
    path('users/secession', userViews.userSecession),
    path('users/login', userViews.login),
    path('users/access', userViews.access),
    path('users/reissuance', userViews.reissuance),
    path('users/access', userViews.access),
    path('users/access/id', userViews.accessToId),

    path('recipe/defaultMain', recipeViews.main_list),
    path('recipe/defaultRanking', recipeViews.ranking_list),

    path('recipe/', recipeViews.recipe),
    path('recipe/wishlist/', recipeViews.wishlist),
    path('recipe/usergrade/', recipeViews.userRGrade),

    path('user/recipelist', recipeViews.userRecipeList),
    path('recipe/order/', recipeViews.recipeOrder),
    path('recipe/search', recipeViews.search),

    path('auth', include('rest_framework.urls', namespace='rest_framework'))
]

#모델로 만든 데이터를 json 형태로 변환해 주는 것
from rest_framework import serializers
from .models import User
from .models import R_info
from .models import MainDefault
# from .models import RankingDefault
from .models import WishList
from .models import R_grade
from .models import UserRecipeList
from .models import R_order
from .models import UserPreferredCategories
from .models import RankingViews

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['userId', 'accessToken', 'password']

class RecipeSerializer(serializers.ModelSerializer):
    class Meta:
        model = R_info
        fields = ['rId', 'recipe_title', 'serving', 'cookingTime', 'difficult', 'recipe_source', 'menu_img', 'recipe_category', 'recipe_url']

class MainDefaultSerializer(serializers.ModelSerializer):
    class Meta:
        model = MainDefault
        fields = ['rId']
        
    def to_representation(self, instance):
        self.fields['rId'] =  RecipeSerializer(read_only=True)
        return super(MainDefaultSerializer, self).to_representation(instance)

class WishSerializer(serializers.ModelSerializer):
    class Meta:
        model = R_info
        fields = ['rId', 'recipe_title', 'menu_img', 'recipe_url']


class userWishListSerializer(serializers.ModelSerializer):
    class Meta:
        model = WishList
        fields = ['userId', 'rId']
        
    def to_representation(self, instance):
        self.fields['rId'] =  WishSerializer(read_only=True)
        return super(userWishListSerializer, self).to_representation(instance)

class UserGradeSerializer(serializers.ModelSerializer):
    class Meta:
        model = R_grade
        fields = ['id', 'userId', 'rId', 'grade', 'comment']


class UserRecipeListSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserRecipeList
        fields = ['userId', 'rId']

    def to_representation(self, instance):
        self.fields['rId'] =  RecipeSerializer(read_only=True)
        return super(UserRecipeListSerializer, self).to_representation(instance)

class R_OrderSerializer(serializers.ModelSerializer):
    class Meta:
        model = R_order
        fields = ['rId', 'recipe_order', 'description']

class UserPreferCategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = UserPreferredCategories
        fields = ['userId', 'category']

class UserPreferCategoryListSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserPreferredCategories
        fields = ['category']

class RankingViewsSerializer(serializers.ModelSerializer):
    class Meta:
        model = RankingViews
        fields = ['views', 'rId']

    def to_representation(self, instance):
        self.fields['rId'] =  RecipeSerializer(read_only=True)
        return super(RankingViewsSerializer, self).to_representation(instance)

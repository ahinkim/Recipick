#모델로 만든 데이터를 json 형태로 변환해 주는 것
from rest_framework import serializers
from .models import User
from .models import R_info
from .models import main_defaultRecipe
from .models import ranking_defaultRecipe

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['userId', 'accessToken', 'password']

class RecipeSerializer(serializers.ModelSerializer):
    class Meta:
        model = R_info
        fields = ['rId', 'recipe_title', 'serving', 'cookingTime', 'difficult', 'recipe_source', 'menu_img', 'recipe_category', 'created_at', 'updated_at']

class MainDefaultSerializer(serializers.ModelSerializer):
    class Meta:
        model = main_defaultRecipe
        fields = ['rId']
        
    def to_representation(self, instance):
        self.fields['rId'] =  RecipeSerializer(read_only=True)
        return super(MainDefaultSerializer, self).to_representation(instance)

class RankingDefaultSerializer(serializers.ModelSerializer):
    class Meta:
        model = ranking_defaultRecipe
        fields = ['rank','rId']
        
    def to_representation(self, instance):
        self.fields['rId'] =  RecipeSerializer(read_only=True)
        return super(RankingDefaultSerializer, self).to_representation(instance)
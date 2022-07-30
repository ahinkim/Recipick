# from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from ..models import MainDefault, UserRecipeList
# from ..models import RankingDefault
from ..models import R_info
from ..models import User
from ..models import WishList
from ..models import R_grade
from ..models import R_order
from ..models import UserPreferredCategories
from ..models import RankingViews
from ..models import CloudyRecipes
from ..models import RainyRecipes
from ..models import SnowyRecipes
from ..models import SunnyRecipes

from ..serializers import RecipeSerializer
from ..serializers import MainDefaultSerializer
# from ..serializers import RankingDefaultSerializer
from ..serializers import userWishListSerializer
from ..serializers import UserGradeSerializer
from ..serializers import UserRecipeListSerializer
from ..serializers import R_OrderSerializer
from ..serializers import UserPreferCategorySerializer
from ..serializers import UserPreferCategoryListSerializer
from ..serializers import RankingViewsSerializer

from ..serializers import CloudyRecipesSerializer
from ..serializers import RainyRecipesSerializer
from ..serializers import SnowyRecipesSerializer
from ..serializers import SunnyRecipesSerializer

from rest_framework.parsers import JSONParser

import jwt
# import datetime

import os
import sys

import random

#sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(os.path.abspath(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))))))
sys.path.append(".")
from Bigdata.bigdata.recipesGenerate import getnerateRecipe

from django.db.models import Q

from django.db.models import Count

#main페이지 유저 맞춤형 조회 or default page 조회
import asyncio

#날씨 api
# 2xx,3xx,5xx 비
# 6xx 눈
# 7xx,8xx 구름많음
# 800, 801 맑음

from django.shortcuts import render
import requests

@csrf_exempt
def weather(request):
    if request.method == 'POST':
        try:
            data = JSONParser().parse(request)
            lat = data['lat']
            lon = data['lon']
            print(lat, lon)
            url = 'http://api.openweathermap.org/data/2.5/weather?lat=%d&lon=%d&appid=c63b4dac86f05fedf45a18dc9346e9a2'%(lat,lon)
            city_weather = requests.get(url).json() #request the API data and convert the JSON to Python data types

            weather_id = city_weather['weather'][0]['id']

            if weather_id >= 200 and weather_id < 600:
                print("비")
                try:
                    rankObj = RainyRecipes.objects.all().order_by('?')[:10]
                    serializer = RainyRecipesSerializer(rankObj, many=True)
                    return JsonResponse({"recipes": serializer.data}, safe=False, status=200)

                except:
                    return JsonResponse({"message":"SERVER ERROR"}, status=500)

            elif weather_id < 700:
                print("눈")
                try:
                    rankObj = SnowyRecipes.objects.all().order_by('?')[:10]
                    serializer = SnowyRecipesSerializer(rankObj, many=True)
                    return JsonResponse({"recipes": serializer.data}, safe=False, status=200)

                except:
                    return JsonResponse({"message":"SERVER ERROR"}, status=500)

            elif weather_id == 800 or weather_id == 801:
                print("맑음")
                try:
                    rankObj = SunnyRecipes.objects.all().order_by('?')[:10]
                    serializer = SunnyRecipesSerializer(rankObj, many=True)
                    return JsonResponse({"recipes": serializer.data}, safe=False, status=200)

                except:
                    return JsonResponse({"message":"SERVER ERROR"}, status=500)
                    
            elif weather_id < 900:
                print("구름많음")
                try:
                    rankObj = CloudyRecipes.objects.all().order_by('?')[:10]
                    serializer = CloudyRecipesSerializer(rankObj, many=True)
                    return JsonResponse({"recipes": serializer.data}, safe=False, status=200)

                except:
                    return JsonResponse({"message":"SERVER ERROR"}, status=500)
                
        
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)
            

        

@csrf_exempt
def main_list(request):
    if request.method == 'GET':
        try:
            access_data = request.META['HTTP_ACCESSTOKEN']
            access = access_data.encode('utf-8')
            userObj = User.objects.get(accessToken=access)
            userId = userObj.userId

            try:
                if UserPreferredCategories.objects.filter(userId=userId).exists():
                    query_set = UserPreferredCategories.objects.filter(userId=userId).all()
                    serializer = UserPreferCategoryListSerializer(query_set, many=True)
                    userPreferCategoryList = []

                    for Category in serializer.data:
                        userPreferCategoryList.append(Category['category'])
        
                    rId_list = getnerateRecipe(userPreferCategoryList)
                    
                    query_set = R_info.objects.filter(rId__in=rId_list).all().order_by('?')
                    serializer = RecipeSerializer(query_set, many=True)

                    return JsonResponse({"recipes": serializer.data}, safe=False, status=200)

                else:
                    return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)

            except:
                return JsonResponse({"message":"SERVER ERROR"}, status=500)
                    
        except:
            return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)


#ranking페이지의 클릭수별 조회 or default레시피 조회
@csrf_exempt
def ranking_list(request):
    if request.method == 'GET':
        try:
            rankObj = RankingViews.objects.all().order_by('-views', 'id')[:20]
            serializer = RankingViewsSerializer(rankObj, many=True)
            return JsonResponse({"recipes": serializer.data}, safe=False, status=200)

        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)


@csrf_exempt
def recipe(request):
    if request.method == 'GET':
        try:
            rId = request.GET['rId']
            query_set = R_info.objects.filter(rId=rId)
            serializer = RecipeSerializer(query_set, many=True)
            
            #존재하는 rId라면 유저 선호카테고리 테이블에 넣기
            if R_info.objects.filter(rId=rId).exists():
                try:
                    #Ranking조회수 올리기
                    if RankingViews.objects.filter(rId=rId).exists():
                        rankObj = RankingViews.objects.filter(rId=rId).get()
                        rankObj.views = rankObj.views + 1
                        rankObj.save()
                    else:
                        data = {"rId": rId, "views": 1}
                        rank_serializer = RankingViewsSerializer(data=data)
                        if rank_serializer.is_valid():
                            rank_serializer.save() 

                    access_data = request.META['HTTP_ACCESSTOKEN']
                    access = access_data.encode('utf-8')
                    userObj = User.objects.get(accessToken=access)
                    recipeObj = R_info.objects.filter(rId=rId).get()
                    userId = userObj.userId
                    recipe_category = recipeObj.recipe_category
                    data = {"userId": userId, "category": recipe_category}
                    
                    #만약에 원래 존재하는 데이터였다면 중복방지를 위해 삭제하고 다시 만들기
                    if UserPreferredCategories.objects.filter(userId=userId, category=recipe_category).exists():
                        upcObj = UserPreferredCategories.objects.filter(userId=userId, category=recipe_category).get()
                        upcObj.delete()
                        upc_serializer = UserPreferCategorySerializer(data=data)   
                        if upc_serializer.is_valid():
                            upc_serializer.save()
                    else:
                        #만약 데이터 넣은 후에 5개 이상 있으면 제일 오래된 데이터 삭제하기
                        upcObj = UserPreferredCategories.objects.filter(userId=userId).all()
                        upc_serializer = UserPreferCategorySerializer(data=data) 
                        if upc_serializer.is_valid():
                            upc_serializer.save() 
                        objCount = upcObj.aggregate(count=Count('id'))
                        
                        if objCount['count'] > 5:
                            upcObj = upcObj = UserPreferredCategories.objects.filter(userId=userId).all().first()
                            upcObj.delete()
                except:
                    return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)

            return JsonResponse({"recipes": serializer.data}, safe=False, status=200)
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)
 
    try:
        access_data = request.META['HTTP_ACCESSTOKEN']
        access = access_data.encode('utf-8')
        obj = User.objects.get(accessToken=access)

        if request.method == 'POST':
            data = JSONParser().parse(request)
            r_serializer = RecipeSerializer(data=data)
            if r_serializer.is_valid():
                r_info_obj=r_serializer.save()
                userId = obj.userId
                rId = r_info_obj.rId
                data = {"userId":userId, "rId":rId}

                #user가 등록한 recipe들 저장하기 위해 UserRecipeList에도 추가
                ur_serializer = UserRecipeListSerializer(data=data)
                if ur_serializer.is_valid():
                    ur_serializer.save()
                else:
                    return JsonResponse(ur_serializer.errors, status=504)

                return JsonResponse({'message': 'SUCCESS'}, status=200)
            return JsonResponse(r_serializer.errors, status=504)

        elif request.method == 'DELETE':
            rId = request.GET['rId']

            try:
                obj = R_info.objects.get(rId=rId)
                obj.delete()
                return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)
            except:
                return JsonResponse({"message":"No recipe to delete"}, status=421)
            
    except:
        return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)

@csrf_exempt
def wishlist(request):
    try:
        access_data = request.META['HTTP_ACCESSTOKEN']
        access = access_data.encode('utf-8')
        obj = User.objects.get(accessToken=access)
        userId = obj.userId

        if request.method == 'GET':
            try:
                query_set = WishList.objects.filter(userId=userId).all().order_by('-id')
                serializer = userWishListSerializer(query_set, many=True)
                return JsonResponse({"wish_list": serializer.data}, safe=False, status=200)
            except:
                return JsonResponse({"message":"SERVER ERROR"}, status=500)

        elif request.method == 'POST':
            rId = request.GET['rId']
            data = {"rId": rId, "userId": userId}
            if WishList.objects.filter(userId=userId, rId=rId).exists():
                return JsonResponse({"message":"Recipe already added to wishlist"}, safe=False, status=401)
            else:    
                serializer = userWishListSerializer(data=data)
                if serializer.is_valid():
                    serializer.save()
                    return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)

                return JsonResponse(serializer.errors, status=504)
        
        elif request.method == 'DELETE':
            rId = request.GET['rId']
            data = {"rId": rId, "userId": userId}
            try:
                obj = WishList.objects.get(userId=userId, rId=rId)
                obj.delete()
                return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)
            except:
                return JsonResponse({"message":"No recipes to delete"}, status=421)

    except:
        return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)


@csrf_exempt
def userRGrade(request):
    if request.method == 'GET':
        try:
            rId = request.GET['rId']
            query_set = R_grade.objects.filter(rId=rId).all()
            serializer = UserGradeSerializer(query_set, many=True)
            return JsonResponse({"grade_list": serializer.data}, safe=False, status=200)
        except:
            return JsonResponse({"message":"REQUEST ERROR"}, status=421)
        
    try:
        access_data = request.META['HTTP_ACCESSTOKEN']
        access = access_data.encode('utf-8')
        obj = User.objects.get(accessToken=access)
        userId = obj.userId

        if request.method == 'POST':
            data = JSONParser().parse(request)
            rId = request.GET['rId']
            grade = data['grade']
            comment = data['comment']

            data = {"rId": rId, "userId": userId, "grade":grade, "comment": comment}
            if R_grade.objects.filter(userId=userId, rId=rId).exists():
                return JsonResponse({"message":"Rating already exists for recipe"}, safe=False, status=401)
            else:    
                serializer = UserGradeSerializer(data=data)
                if serializer.is_valid():
                    serializer.save()
                    return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)

                return JsonResponse(serializer.errors, status=504)
        
        elif request.method == 'DELETE':
            id = request.GET['id']

            try:
                obj = R_grade.objects.get(id=id)
                obj.delete()
                return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)
            except:
                return JsonResponse({"message":"No grade to delete"}, status=421)

        elif request.method == 'PUT':
            data = JSONParser().parse(request)
            id = request.GET['id']
            grade = data['grade']
            comment = data['comment']

            try:
                obj = R_grade.objects.get(id=id)
                obj.grade = grade
                obj.comment = comment
                obj.save()

                return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)
            except:
                return JsonResponse({"message":"No grade to update"}, status=421)

    except:
        return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)


@csrf_exempt
def userRecipeList(request):
    try:
        access_data = request.META['HTTP_ACCESSTOKEN']
        access = access_data.encode('utf-8')
        obj = User.objects.get(accessToken=access)
        userId = obj.userId

        if request.method == 'GET':
            try:
                query_set = UserRecipeList.objects.filter(userId=userId).all()
                serializer = UserRecipeListSerializer(query_set, many=True)
                return JsonResponse({"recipes": serializer.data}, safe=False, status=200)
            except:
                return JsonResponse(serializer.errors, status=504)
    
    except:
        return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN OR REQUEST ERROR"}, status=411)

@csrf_exempt
def recipeOrder(request):
    if request.method == 'GET':
        try:
            rId = request.GET['rId']
            query_set = R_order.objects.filter(rId=rId).all()
            serializer = R_OrderSerializer(query_set, many=True)
            return JsonResponse({"r_order": serializer.data}, safe=False, status=200)
        except:
            return JsonResponse({"message":"REQUEST ERROR"}, status=421)



@csrf_exempt
def search(request):
    try:
        if request.method == 'POST':
            data = JSONParser().parse(request)
            searchWord = data['searchWord']

            query_set = R_info.objects.filter(Q(recipe_title__icontains=searchWord)).distinct()
            query_set2 = R_info.objects.filter(Q(recipe_source__icontains=searchWord)).distinct()
            query_set3 = R_info.objects.filter(Q(recipe_category__icontains=searchWord)).distinct()

            query_set = query_set.union(query_set2)
            query_set = query_set.union(query_set3)
            query_set = query_set.distinct()

            serializer = RecipeSerializer(query_set, many=True)
            
            return JsonResponse({"recipes": serializer.data}, status=200)
            
    except:
        return JsonResponse({"message":"REQUEST ERROR"}, status=421)


# @csrf_exempt
# def test(request):
#     if request.method == 'GET':
#         try:
#             rId_list = getnerateRecipe(1)
#             rId_list.append(3)
#             rId_list.append(5)
#             rId_list.append(7)
#             query_set = R_info.objects.filter(rId__in=rId_list).all()
#             serializer = RecipeSerializer(query_set, many=True)
#             return JsonResponse({"recipes": serializer.data}, safe=False, status=200)
#         except:
#             return JsonResponse({"message":"REQUEST ERROR"}, status=421)


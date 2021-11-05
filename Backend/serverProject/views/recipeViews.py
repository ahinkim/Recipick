# from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from ..models import MainDefault, UserRecipeList
from ..models import RankingDefault
from ..models import R_info
from ..models import User
from ..models import WishList
from ..models import R_grade
from ..models import R_order

from ..serializers import RecipeSerializer
from ..serializers import MainDefaultSerializer
from ..serializers import RankingDefaultSerializer
from ..serializers import userWishListSerializer
from ..serializers import UserGradeSerializer
from ..serializers import UserRecipeListSerializer
from ..serializers import R_OrderSerializer

from rest_framework.parsers import JSONParser

import jwt
# import datetime

#main페이지의 default레시피들 조회
@csrf_exempt
def main_list(request):
    if request.method == 'GET':
        try:
            query_set = MainDefault.objects.all()
            serializer = MainDefaultSerializer(query_set, many=True)
            return JsonResponse({"recipes":serializer.data}, safe=False, status=200)
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)

#ranking페이지의 default레시피들 조회
@csrf_exempt
def ranking_list(request):
    if request.method == 'GET':
        try:
            query_set = RankingDefault.objects.all()
            serializer = RankingDefaultSerializer(query_set, many=True)
            return JsonResponse({"recipes":serializer.data}, safe=False, status=200)
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)


@csrf_exempt
def recipe(request):
    if request.method == 'GET':
        try:
            rId = request.GET['rId']
            query_set = R_info.objects.filter(rId=rId)
            serializer = RecipeSerializer(query_set, many=True)
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
                query_set = WishList.objects.filter(userId=userId).all()
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



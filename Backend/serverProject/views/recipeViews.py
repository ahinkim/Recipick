# from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from ..models import MainDefault
from ..models import RankingDefault
from ..models import R_info
from ..models import User
from ..models import WishList
from ..serializers import RecipeSerializer
from ..serializers import MainDefaultSerializer
from ..serializers import RankingDefaultSerializer
from ..serializers import UserRListSerializer
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
def get_recipe(request):
    if request.method == 'GET':
        try:
            rId = request.GET['rId']
            query_set = R_info.objects.filter(rId=rId)
            serializer = RecipeSerializer(query_set, many=True)
            return JsonResponse(serializer.data, safe=False, status=200)
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)


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
                serializer = UserRListSerializer(query_set, many=True)
                return JsonResponse({"wish_list": serializer.data}, safe=False, status=200)
            except:
                return JsonResponse({"message":"No wishlist for user"}, status=401)

        elif request.method == 'POST':
            rId = request.GET['rId']
            data = {"rId": rId, "userId": userId}
            if WishList.objects.filter(userId=userId, rId=rId).exists():
                return JsonResponse({"message":"Recipe already added to wishlist"}, safe=False, status=401)
            else:    
                serializer = UserRListSerializer(data=data)
                if serializer.is_valid():
                    serializer.save()
                    return JsonResponse({"message":"SUCCESS"}, safe=False, status=200)

                return JsonResponse(serializer.errors, status=404)
        
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
        return JsonResponse({"message":"MISMATCHED_ACCESSTOKEN"}, status=411)
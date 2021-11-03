# from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from ..models import main_defaultRecipe
from ..models import ranking_defaultRecipe
from ..serializers import RecipeSerializer
from ..serializers import MainDefaultSerializer
from ..serializers import RankingDefaultSerializer
from rest_framework.parsers import JSONParser

# import jwt
# import datetime

#main페이지의 default레시피들 조회
@csrf_exempt
def main_list(request):
    if request.method == 'GET':
        try:
            query_set = main_defaultRecipe.objects.all()
            serializer = MainDefaultSerializer(query_set, many=True)
            return JsonResponse(serializer.data, safe=False, status=200)
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)

#ranking페이지의 default레시피들 조회
@csrf_exempt
def ranking_list(request):
    if request.method == 'GET':
        try:
            query_set = ranking_defaultRecipe.objects.all()
            serializer = RankingDefaultSerializer(query_set, many=True)
            return JsonResponse(serializer.data, safe=False, status=200)
        except:
            return JsonResponse({"message":"SERVER ERROR"}, status=500)
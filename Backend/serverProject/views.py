from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import User
from .serializers import UserSerializer
from rest_framework.parsers import JSONParser

import jwt
import datetime

import json
from django.core.exceptions import ImproperlyConfigured
with open("../secret.json") as f:
    secrets = json.loads(f.read())

#외부에 저장한 SECRETKEY 불러오는 함수
def get_secret(setting, secrets=secrets):
    try:
        return secrets[setting]
    except KeyError:
        error_msg = f"Set the {setting} enviroment variable"
        raise ImproperlyConfigured(error_msg)

#계정 중복 확인(GET), 회원가입(POST)
@csrf_exempt
def user_list(request):
    if request.method == 'GET':
        data = JSONParser().parse(request)
        search_userId = data['userId']

        #중복인 경우
        if User.objects.filter(userId=search_userId).exists():
            return JsonResponse({'message': 'FALSE'}, status=401)
        else: #중복이 아닌 경우
            return JsonResponse({'message': 'SUCCESS'}, status=200)

    elif request.method == 'POST':
        data = JSONParser().parse(request)
        serializer = UserSerializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return JsonResponse({'message': 'SUCCESS'}, status=200)
        return JsonResponse(serializer.errors, status=401)


#accessToken으로 특정 계정 삭제(DELETE)
#나중에 특정 유저 조회, 수정 만들고 싶으면 여기다 추가
# @csrf_exempt
# def user(request):
    
#     obj = User.objects.get(pk=pk)

#     if request.method == 'DELETE':
#         obj.delete()
#         return HttpResponse(status=204)
#     if access.decode('utf-8') == access.decode('utf-8'): -->user 비교할 때 꼭 이렇게 비교하기

#login - 로그인(POST)
@csrf_exempt
def login(request):
    if request.method == 'POST':
        data = JSONParser().parse(request)
        search_userId = data['userId']
        if User.objects.filter(userId=search_userId).exists(): #아이디는 존재
            obj = User.objects.get(userId=search_userId)
            
            if data['password'] == obj.password: #비밀번호 일치
                secret = get_secret("SECRET_KEY")
                #weeks=2로 바꾸기
                access = jwt.encode({"exp": datetime.datetime.utcnow() + datetime.timedelta(seconds=300),"userId": data['userId']}, secret, algorithm="HS256")

                return JsonResponse({"accessToken": access.decode('utf-8')},status=200)
            else:  #비밀번호 불일치
                return JsonResponse({'message': 'FALSE'}, status=401)
        else: #없는 아이디인 경우
            return JsonResponse({'message': 'FALSE'}, status=401)
        

#accessToken 발급
@csrf_exempt
def issueAtk(request):
    if request.method == 'POST':
        data = JSONParser().parse(request)
        search_userId = data['userId']
        obj = User.objects.get(userId=search_userId)

        if data['password'] == obj.password:
            return HttpResponse(status=200)
        else:
            return HttpResponse(status=400)
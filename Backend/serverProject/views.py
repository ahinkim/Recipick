from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import User
from .serializers import UserSerializer
from rest_framework.parsers import JSONParser

import jwt
import datetime
from pytz import timezone

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

#회원가입(POST)
@csrf_exempt
def user_list(request):
    if request.method == 'POST':
        data = JSONParser().parse(request)
        serializer = UserSerializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return JsonResponse({'message': 'SUCCESS'}, status=200)
        return JsonResponse(serializer.errors, status=401)

#계정 중복 확인(POST)
@csrf_exempt
def userValidate(request):
    if request.method == 'POST':
        data = JSONParser().parse(request)
        search_userId = data['userId']

        #중복인 경우
        if User.objects.filter(userId=search_userId).exists():
            return JsonResponse({'message': 'FALSE'}, status=401)
        else: #중복이 아닌 경우
            return JsonResponse({'message': 'SUCCESS'}, status=200)


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
                access = jwt.encode({"exp": datetime.datetime.now(timezone('UTC')) + datetime.timedelta(seconds=60),"userId": data['userId']}, secret, algorithm="HS256")
                obj.accessToken = access
                obj.save()

                return JsonResponse({"accessToken": access.decode('utf-8')},status=200)
            
            else:  #비밀번호 불일치
                return JsonResponse({'message': 'FALSE'}, status=401)
        else: #없는 아이디인 경우
            return JsonResponse({'message': 'FALSE'}, status=401)
        

#accessToken 발급
@csrf_exempt
def reissuanceAtk(request):
    if request.method == 'GET':

        data = request.META['HTTP_ACCESSTOKEN']
        try:
            access = data.encode('utf-8') 
            secret = get_secret("SECRET_KEY")
            jwt.decode(data, secret, algorithm="HS256")

            try:
                #만료기간이 지나지 않고 accessToken도 일치하는 경우
                obj = User.objects.get(accessToken = access)
                secret = get_secret("SECRET_KEY") ###########
                print(jwt.decode(access, secret, algorithm="HS256")) #############
                
                return JsonResponse({"message": "NOT_EXPIRED_TOKEN"},status=200)
            except:
                #만료기간은 지나지 않았지만 accessToken은 일치하지 않은 경우
                return JsonResponse({"message": "MISMATCHED_TOKEN"},status=419)

        except jwt.ExpiredSignatureError:
            try:
                #access token이랑 일치하지만 만료기간이 지난 경우
                obj = User.objects.get(accessToken = access)
                userId = obj.userId
                secret = get_secret("SECRET_KEY")
                #weeks=2로 바꾸기
                access = jwt.encode({"exp": datetime.datetime.now(timezone('UTC')) + datetime.timedelta(seconds=60),"userId": userId}, secret, algorithm="HS256")
                obj.accessToken = access
                obj.save()
                return JsonResponse({"accessToken": access.decode('utf-8')}, status = 412)

            except:
                #access token이랑 일치하지도 않고 만료기간이 지난 경우
                return JsonResponse({"message": "MISMATCHED_TOKEN"},status=419)
        
        except jwt.InvalidTokenError:
            #유효하지 않은 token 형태인 경우
            return JsonResponse({"message": "INVALID_TOKEN"},status=420)

#회원 탙퇴
@csrf_exempt
def userSecession(request):
    if request.method == 'DELETE':
        data = request.META['HTTP_ACCESSTOKEN']
        access = data.encode('utf-8')
        try:
            obj = User.objects.get(accessToken = access)
            obj.delete()
            return JsonResponse({"message": "SUCCESS"},status=200)

        except: 
            return JsonResponse({"message": "FALSE"},status=401)
        

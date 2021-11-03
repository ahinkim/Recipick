# from os import access
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from ..models import User
from ..serializers import UserSerializer
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
    search_userId = request.META['HTTP_USERID']
    password = request.META['HTTP_PASSWORD'] 
    data = {"userId":search_userId, "password":password}
    if request.method == 'POST':
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



#login - 로그인(POST)
@csrf_exempt
def login(request):
    if request.method == 'POST':
        search_userId = request.META['HTTP_USERID']
        password = request.META['HTTP_PASSWORD']
        if User.objects.filter(userId=search_userId).exists(): #아이디는 존재
            obj = User.objects.get(userId=search_userId)
            
            if password == obj.password: #비밀번호 일치
                secret = get_secret("SECRET_KEY")
        
                access = jwt.encode({"exp": datetime.datetime.now(timezone('UTC')) + datetime.timedelta(weeks=2),"userId": search_userId}, secret, algorithm="HS256")
                refresh = jwt.encode({"userId": search_userId}, secret, algorithm="HS256")
                obj.accessToken = access
                obj.refreshToken = refresh
                obj.save()

                return JsonResponse({"accessToken": access.decode('utf-8'), "refreshToken": refresh.decode('utf-8')},status=200)

            else:  #비밀번호 불일치
                return JsonResponse({'message': 'FALSE'}, status=401)
        else: #없는 아이디인 경우
            return JsonResponse({'message': 'FALSE'}, status=401)
        

#accessToken 인증
@csrf_exempt
def access(request):
    if request.method == 'GET':
        data = request.META['HTTP_ACCESSTOKEN']

        try:
            access = data.encode('utf-8') 
            secret = get_secret("SECRET_KEY")
            jwt.decode(data, secret, algorithm="HS256") #이거 없으면 에러처리 안된다.

            try:
                #만료기간이 지나지 않고 accessToken도 일치하는 경우
                obj = User.objects.get(accessToken = access)
                secret = get_secret("SECRET_KEY") 
 
                return JsonResponse({"message": "NOT_EXPIRED_ACCESSTOKEN"},status=200)

            except:
                #만료기간은 지나지 않았지만 accessToken은 일치하지 않은 경우
                return JsonResponse({"message": "MISMATCHED_ACCESSTOKEN"},status=401)

        except jwt.ExpiredSignatureError:
            try:
                #access token이랑 일치하지만 만료기간이 지난 경우
                obj = User.objects.get(accessToken = access)
                
                return JsonResponse({"message": "EXPIRED_ACCESSTOKEN"},status=419)

            except:
                #access token이랑 일치하지도 않고 만료기간이 지난 경우
                return JsonResponse({"message": "MISMATCHED_ACCESSTOKEN"},status=401)
        
        except jwt.InvalidTokenError:
            #유효하지 않은 token 형태인 경우
            return JsonResponse({"message": "INVALID_TOKEN"},status=402)



#accessToken 재발급
@csrf_exempt
def reissuance(request):
    if request.method == 'GET':
        access_data = request.META['HTTP_ACCESSTOKEN']
        refresh_data = request.META['HTTP_REFRESHTOKEN']
        try:
            access = access_data.encode('utf-8') 
            refresh = refresh_data.encode('utf-8') 
            secret = get_secret("SECRET_KEY")
            jwt.decode(refresh, secret, algorithm="HS256") #이거 없으면 에러처리 안된다.

            try:
                #accessToken, refreshToken 일치하는 경우
                obj = User.objects.get(accessToken = access, refreshToken = refresh)
                userId = obj.userId
                # secret = get_secret("SECRET_KEY")

                access = jwt.encode({"exp": datetime.datetime.now(timezone('UTC')) + datetime.timedelta(weeks=2),"userId": userId}, secret, algorithm="HS256")
                obj.accessToken = access
                obj.save()
 
                return JsonResponse({"newAccessToken": access.decode('utf-8')},status=200)

            except:
                #access,refresh tokeh과 동시에 일치하는 사용자가 없는 경우
                return JsonResponse({"message": "MISMATCHED_REFRESHTOKEN OR ACCESSTOKEN"},status=411)

        
        except jwt.InvalidTokenError:
            #유효하지 않은 refresh token 형태인 경우
            return JsonResponse({"message": "INVALID_REFRESHTOKEN"},status=412)


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
        

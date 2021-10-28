from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import User
from .serializers import UserSerializer
from rest_framework.parsers import JSONParser
import jwt
# from django.shortcuts import render
# from django.core import serializers
# from django.http import HttpResponse
# from rest_framework.decorators import api_view, permission_classes, authentication_classes
# from rest_framework.permissions import IsAuthenticated
# from rest_framework_jwt.authentication import JSONWebTokenAuthentication
# import jwt
#계정 중복 확인(GET), 회원가입(POST)
@csrf_exempt
def user_list(request):
    if request.method == 'GET':
        data = JSONParser().parse(request)
        search_userId = data['userId']

        #중복인 경우
        if User.objects.filter(userId=search_userId).exists():
            return HttpResponse(status=401)
        else: #중복이 아닌 경우
            return HttpResponse(status=200)

    elif request.method == 'POST':
        data = JSONParser().parse(request)
        serializer = UserSerializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return HttpResponse(status=200)
        return JsonResponse(serializer.errors, status=401)


#accessToken으로 특정 계정 삭제(DELETE)
#나중에 특정 유저 조회, 수정 만들고 싶으면 여기다 추가
# @csrf_exempt
# def user(request):
    
#     obj = User.objects.get(pk=pk)

#     if request.method == 'DELETE':
#         obj.delete()
#         return HttpResponse(status=204)


#login - 로그인(POST)
@csrf_exempt
def login(request):
    if request.method == 'POST':
        data = JSONParser().parse(request)
        search_userId = data['userId']
        if User.objects.filter(userId=search_userId).exists(): #아이디는 존재
            obj = User.objects.get(userId=search_userId)
            
            if data['password'] == obj.password: #비밀번호 일치
                return HttpResponse(status=200)
            else:  #비밀번호 불일치
                return HttpResponse(status=401)
        else: #없는 아이디인 경우
            return HttpResponse(status=401)
        

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
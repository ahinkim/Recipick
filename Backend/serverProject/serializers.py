#모델로 만든 데이터를 json 형태로 변환해 주는 것
from rest_framework import serializers
from .models import User
# from django.contrib.auth import get_user_model
# from django.contrib.auth.models import update_last_login
# from django.contrib.auth import authenticate
# from rest_framework_jwt.settings import api_settings

# JWT_PAYLOAD_HANDLER = api_settings.JWT_PAYLOAD_HANDLER
# JWT_ENCODE_HANDLER = api_settings.JWT_ENCODE_HANDLER

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['userId', 'accessToken', 'password']
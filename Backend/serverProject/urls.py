from django.contrib import admin
from django.urls import path
from . import views
from django.conf.urls import include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('users', views.user_list),
    # path('userSecession', views.user),
    path('login', views.login),
    path('auth', include('rest_framework.urls', namespace='rest_framework'))
]

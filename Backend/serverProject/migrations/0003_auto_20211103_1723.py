# Generated by Django 3.0.7 on 2021-11-03 17:23

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('serverProject', '0002_auto_20211103_1723'),
    ]

    operations = [
        migrations.AlterField(
            model_name='r_grade',
            name='comment',
            field=models.TextField(null=True),
        ),
    ]

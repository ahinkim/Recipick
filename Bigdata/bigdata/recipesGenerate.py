import os
import pandas as pd
import csv
import random
from pathlib import Path

def getnerateRecipe(userId):
  #경로가지고오기 : 실제로는 다를 수 있음 확인해 봐야한다.
  recipePath = str(Path.cwd()) + "/Bigdata/bigdata/recipeData.csv"
  collaboratedPath = str(Path.cwd()) + "/Bigdata/bigdata/collaborated.csv"

  #데이터
  recipeData = pd.read_csv(recipePath,encoding='utf-8-sig')
  collaborated = pd.read_csv(collaboratedPath,encoding='utf-8')

  #데이터 수정
  recipeData = recipeData.drop(['Unnamed: 0'],axis = 1)
  collaborated = collaborated.set_index('recipe_category')
    
  # #유저의 선호 카테고리 가져오기 : 추후 구현
  # #def getUserWishCategroy(userId): 
  # # return category 


  # #유저의 선호 카테고리의 상위 3~5개의 카테고리를 반환
  def is_topCategory(collaborated,wishCategory):
    isTopCategory = collaborated[wishCategory] >=0.2 #유사도 0.4이상은 너무 적다.. 고려해야할 사항 : 유사도를 낮추거나 기본레시피리스트가 필요
    categoryList = collaborated[isTopCategory]
    categoryList = categoryList.index.to_list()  
    return categoryList #list

  #해당 카테고리의 레시피 rId 반환
  def is_recipes(recipeData,categoryNames): #list
      ridList = []
      for item in categoryNames:
          datas = recipeData['recipe_category'] == item
          newRecipes = recipeData[datas]
          newRid = newRecipes['rId']
          newRid = newRid.to_list()
          ridList += newRid
      return ridList #list

  #시작
  # userId
  # wishCategory = getUserWishCategory(userId)
  wishCategory = '가라아게' # getUserWishCategory를 해야한다. 편의를 위해 임의로 넣어둠.
  categories = is_topCategory(collaborated,wishCategory)
  recipeList = is_recipes(recipeData,categories)

  #임의로 셔플
  random.shuffle(recipeList)

  #100개보다도 작은 rId개수가 나올 경우 추가로 랜덤한 레시피를 추가해주자.
  # print(len(recipeList))
  return recipeList
  
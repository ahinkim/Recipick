import pandas as pd
import random
from pathlib import Path

def getnerateRecipe(wishList): 
  
  #경로가지고오기 : 실제로는 다를 수 있음 확인해 봐야한다.

  # MARK: 경로 업데이트
  # 서버기준
  recipePath = str(Path.cwd().parent) + r"/Bigdata/bigdata/RecipeData.csv"
  collaboratedPath = str(Path.cwd().parent) + r"/Bigdata/bigdata/collaborated.csv"
  # recipePath = "./recipeData.csv"
  # 로컬기준
  # recipePath = str(Path.cwd()) + "/Bigdata/bigdata/RecipeData.csv"
  # collaboratedPath = str(Path.cwd()) + "/Bigdata/bigdata/collaborated.csv"
  
  #데이터 가지고오기
  recipeData = pd.read_csv(recipePath,encoding='cp949')
  collaborated = pd.read_csv(collaboratedPath,encoding='utf-8-sig')

  #데이터 수정(읽을 때 생김)
  recipeData = recipeData.drop(['Unnamed: 0'],axis = 1)
  collaborated = collaborated.set_index('recipe_category')

  #유저의 선호 카테고리의 상위 5개의 카테고리를 반환 
  def is_topCategory(collaborated,wishCategory):
    isTopCategory = collaborated[wishCategory] >=0.4  #유사도 0.4이상이 좋은 결과를 보여줌
    categoryList = collaborated[isTopCategory]
    categoryList = categoryList.index.to_list()
    print(categoryList)  
    return categoryList #list

  #해당 카테고리의 레시피 rId 반환
  def is_recipes(recipeData,categoryNames): #list
      ridList = []
      for item in categoryNames:
        try:
          datas = recipeData['recipe_category'] == item
          newRecipes = recipeData[datas]
          newRid = newRecipes['rId']
          newRid = newRid.to_list()
          ridList += newRid
        except:
          print("Error Recipe")
      return ridList #list

  def randomRecipes(recipeData, collaborated,count) :
    randlist = random.sample(list(collaborated.columns),count+50)
    ridList = []
    for item in randlist:
      
        if len(ridList) == count : break
        datas = recipeData['recipe_category'] == item
        newRecipes = recipeData[datas]
        newRid = newRecipes['rId']
        newRid = newRid.to_list()

        if not newRid:
          continue
        else :
          out = random.choice(newRid)
        ridList += [out]

    return ridList #list

  #시작
  midList = [] # 중간리스트
  recipeList = [] #최종리스트
  count = 0
  for wishCategory in wishList:
    try:
      categories = is_topCategory(collaborated,wishCategory)
      midList += [is_recipes(recipeData,categories)]
      #총 개수 count
      print(len(is_recipes(recipeData,categories)))
      count += len(is_recipes(recipeData,categories))
    except KeyError:
      continue
  # midList = [[],[],[],[],[]] 총 5개의 리스트 30,25,20,15,10 (최근선택 - 예전선택)
  # 조건에 따른 가중치

  # 부족 -> 100개 
  if count <= 75 :
    recipeList = sum(midList,[])
    recipeList += randomRecipes(recipeData,collaborated,100 - len(recipeList))

  elif 75 < count and count <= 100 :
    recipeList = sum(midList,[])
    recipeList += randomRecipes(recipeData,collaborated,120-len(recipeList))

  # 오버 -> 150개 
  # 개수가 모자를 수 있으니 비율로 정산할 것!
  else :
    weight = [30,25,20,15,10] 
    for item in range(5):
      try:
        recipeList += random.sample(midList[item],weight[item])
      except:
        #안에 없을 경우 어떻게 할 것인가.
        recipeList += midList[item]
    if len(recipeList) <= 125 :
      recipeList += randomRecipes(recipeData,collaborated,125-len(recipeList))
        



  #셔플
  random.shuffle(recipeList)
  # print(len(recipeList))
  # print(recipeList)
  return recipeList

# myWish = ["소고기말이주먹밥","가나슈마카롱","스팸김치볶음밥","닭갈비덮밥","새우양파튀김"] #중간
# myWish = ['떡볶이','닭볶음탕','부추전','돼지김치찌개','스팸김치볶음밥'] # 최상
# myWish = ['LA찹쌀떡','가시오이부추무침','흑임자연근샐러드','황치즈쿠키','황치즈머핀'] #최악
# getnerateRecipe(myWish)

# 1. 리사이클 : 해결
# 클릭없이 리사이클

# 2. 개수 : 100개

# 3. 버리기 / 채우기
# 백그라운드(랜덤) 레시피 : 반드시 필요할 듯 / 자연스럽게 가지고올 수 있는 방법은 뭐가 있을까 / 일단 카테고리당 한개씩 [30개]
# 버리기 : 200개에 맞춰서 가중치 만큼 버리기 -> 랜덤으로 절반 보충
# 채우기 : 150개에 맞춰서 가중치 만큼 채우기 -> 부족한 부분을 랜덤에서 더 가지고와보기
 
# 구현
# 상위 5개 카테고리 : 아인님 전달 / 상위 5개 카테고리, 큐형태로 실험 -> ['떡볶이','닭볶음탕','부추전','돼지김치찌개','','스팸김치볶음밥']
# 랜덤 레시피 반환 함수 : 부족한지 채워야하는지 확인후 진행

# Ahead : 기본레시피 작성 + 디폴트 레시피 조합으로 보충해주기
# 1. rId의 총개수를 100개로 설정
# 2. random.chice()를 이용해 가중치를 넣어준다. - 각 카테고리 안에서의 가중치 / 처음(10% 고정) ~ 마지막 / 개수가 가변 
# 3. random.chice()를 이용해 가중치를 넣어준다. - 전체 카테고리에서의 가중치 / 최근순으로 높게 / 개수 5개 고정
# 4. 기본.random() + 디폴트.random() 으로 부족한 개수 보충
# 5. random.shuffle()을 이용해 섞어서 표시 - 클릭없을 경우 해당 메소드가 있으므로 패싱
# 완성
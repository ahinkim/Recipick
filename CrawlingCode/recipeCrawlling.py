from pandas.core.frame import DataFrame
import requests
from bs4 import BeautifulSoup
import re
import pandas as pd
  
page_baseUrl = "https://www.10000recipe.com/recipe/list.html?q=&query=&cat1="
categoryNumList = ["6", "1", "7", "36","41", "42", "8", "10", "9", "38", "67", "39", "37","11"]
#볶음 끓이기 부침 조림 무침 비빔 찜 절임 튀김 삶기 굽기 데치기 회 기타

recipeId = 0

global firstDf,secondDf,thirdDf

firstDf =  pd.DataFrame()
secondDf =  pd.DataFrame()
thirdDf =  pd.DataFrame()

def RecipePageCrawler(recipeUrl, rId, category):
    
    global firstDf,secondDf,thirdDf

    page = requests.get(recipeUrl)
    soup = BeautifulSoup(page.content, 'html.parser')
    recipe_title = []
    recipe_source = []
    recipe_step = []
    recipe_category = []
    user_grade = []  # 만개의 레시피 user별 닉네임,평점 크롤링

    try:
        res = soup.find('div', 'view2_summary')
        res = res.find('h3')
        recipe_title.append(res.get_text().replace('\n', ''))
        res = soup.find('span', 'view2_summary_info1')  # 인분
        serving = res.getText()
        res = soup.find('span', 'view2_summary_info2')  # 요리 시간
        cookingTime = res.getText()
        res = soup.find('span', 'view2_summary_info3')  # 난이도
        difficult = res.getText()
        res = soup.find('div', 'ready_ingre3')
        recipe_category.append(category)  # 카테고리 분류

    except(AttributeError):
        return

    try:
        for n in soup.find_all('div', 'media-body'):
            res = n.find('b', 'info_name_f')  # 유저 닉네임

            if res != None:
                regex = re.compile(r'\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d')
                p = regex.search(str(n))
                created_day = p.group()  # 평점 생성날짜

                nick = res.getText()
                res = n.find('span', 'reply_list_star')  # 평점
                i = 0
                for k in res.find_all('img'):
                    if "icon_star2_on.png" in k.get('src'):
                        i = i + 1
                grade = i
                user_grade.append([rId, nick, grade, created_day])

    except(AttributeError):
        return

    try:
        res = soup.find('div', 'view2_summary')
        res = res.find('h3')
        res = soup.find('div', 'view2_summary_info')
        res = soup.find('div', 'ready_ingre3')

        for n in res.find_all('ul'):
            for tmp in n.find_all('li'):
                tempSource = tmp.get_text().replace('\n', '').replace(' ', ' ')
                recipe_source.append(tempSource.split("    ")[0])

    except (AttributeError):
        return

    try:
        res = soup.find('div', 'view_step')
        i = 0
        for n in res.find_all('div', 'view_step_cont'):
            i = i + 1
            recipe_step.append([str(i), n.get_text().replace('\n', '')])

        if not recipe_step:
            return

    except (AttributeError):
        return

    try:
        res = soup.find('div', 'centeredcrop')
        res = res.find('img')
        menu_img = res.get('src')

    except (AttributeError):
        return

    recipe_all = [rId, recipe_title, serving, cookingTime, difficult, recipe_source, recipe_step, menu_img,
                  recipe_category]
    print(rId, recipe_title, recipe_category)
#     print(recipe_all, '\n\n')
#     print(user_grade)
    first,second,third = getDataFrame(recipe_all,user_grade)
    
    firstDf = firstDf.append(first)
    secondDf = secondDf.append(second)
    thirdDf = thirdDf.append(third)

def RotateRecipe(url, page, soup,category):
    global recipeId
    i = 0
    while True:
        try:
            i = i+1
            numPageurl = url + "&order=reco&page=" + str(i) 
            page = requests.get(numPageurl)
            soup = BeautifulSoup(page.content, 'html.parser')
            for href in soup.find("ul", class_="common_sp_list_ul ea4").find_all("li"):
                recipeUrl = "https://www.10000recipe.com" + href.find("a")["href"]
                recipeId = recipeId + 1
                RecipePageCrawler(recipeUrl,recipeId, category)
#                 print("\n\n\n")
        except (AttributeError):
            break
    

def getDataFrame(recipe_all, user_grade):
    # 1st
    # columns = {'rId','recipe_title','serving','coockingTime','difficult','recipe_source','menu_img','recipe_category'}
    data= {'rId':[recipe_all[0]],'recipe_title':[recipe_all[1][0]], 'serving':[recipe_all[2]], 'coockingTime':[recipe_all[3]], 'difficult':[recipe_all[4]], 'recipe_source':[','.join(recipe_all[5])], 'menu_img':[recipe_all[7]], 'recipe_category':recipe_all[8][0]}
    first = pd.DataFrame(data=data)

    # 2nd
    recipe_step = recipe_all[6]
    columns = {'rId','recipe_order','description'}
    data = {column:[] for column in columns}
    for recipe_order, description in recipe_step:
        data['rId'].append(recipe_all[0])
        data['recipe_order'].append(recipe_order)
        data['description'].append(description)
    second = pd.DataFrame(data=data)

    # 3rd
    columns = {'rId','nick','grade','created_day'}
    data = {column:[] for column in columns}
    for rId, nick, grade, created_day in user_grade:
        data['rId'].append(rId)
        data['nick'].append(nick)
        data['grade'].append(grade)
        data['created_day'].append(created_day)
    third = pd.DataFrame(data=data)

    return first, second, third    
    
def CategoryPageCrawler(category_num):
    pageUrl = page_baseUrl + category_num + "&cat2=&cat3=&cat4=&fct=&order=reco&lastcate=cat1&dsearch=&copyshot=&scrap=&degree=&portion=&time=&niresource="
    page = requests.get(pageUrl)
    soup = BeautifulSoup(page.content, 'html.parser')

    try:
        for href in soup.find("ul", class_="tag_cont").find_all("li"):
            pageUrl1 = "https://www.10000recipe.com/" + href.find("a")["href"]
            page = requests.get(pageUrl1)
            soup = BeautifulSoup(page.content, 'html.parser')
            res = soup.find("ul", class_="tag_cont").find_all("li")
            category = href.getText()
            if not res:
                RotateRecipe(pageUrl1, page, soup, category)
            else:
                for href in res:
                    pageUrl2 = "https://www.10000recipe.com/" + href.find("a")["href"]
                    page = requests.get(pageUrl2)
                    soup = BeautifulSoup(page.content, 'html.parser')
                    res = soup.find("ul", class_="tag_cont").find_all("li")
                    category = href.getText()
                    if not res:
                        RotateRecipe(pageUrl2, page, soup, category)
                    else:
                        for href in res:
                            category = href.getText()
                            pageUrl3 = "https://www.10000recipe.com/" + href.find("a")["href"]
                            page = requests.get(pageUrl3)
                            soup = BeautifulSoup(page.content, 'html.parser')
                            RotateRecipe(pageUrl3, page, soup, category)

                                

    except(AttributeError):
        return


for category_num in categoryNumList:
    CategoryPageCrawler(category_num)
    
firstDf.to_csv("first.csv", encoding='utf-8-sig')
secondDf.to_csv("second.csv", encoding='utf-8-sig')
thirdDf.to_csv("third.csv", encoding='utf-8-sig')
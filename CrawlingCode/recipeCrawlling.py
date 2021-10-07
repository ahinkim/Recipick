from pandas.core.frame import DataFrame
import requests
from bs4 import BeautifulSoup
import re
import pandas as pd

page_baseUrl = "https://www.10000recipe.com/recipe/list.html?cat4="
categoryNumList = ["63", "56", "54", "55", "60", "53", "52", "61", "57", "58", "65", "64", "68", "66", "69", "59", "62"]
categoryList = ["밑반찬", "메인반찬", "국/탕", "찌개", "디저트", "면/만두", "밥/죽/떡", "퓨전", "김치/젓갈/장류", "양념/소스/잼", "양식", "샐러드", "스프", "빵",
                "과자", "차/음료/술", "기타"]
pageNumList = [320, 220, 120, 50, 40, 70, 150, 6, 47, 22, 20, 20, 6, 82, 25, 20, 30]
recipeId = 0

global firstDf,secondDf,thirdDf

firstDf =  pd.DataFrame()
secondDf =  pd.DataFrame()
thirdDf =  pd.DataFrame()

def RecipePageCrawler(recipeUrl, categoryIdx, rId):
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
        recipe_category.append(categoryList[categoryIdx])  # 카테고리 분류

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

    print(recipe_all, '\n\n')
    print(user_grade)
    first,second,third = getDataFrame(recipe_all,user_grade)

    firstDf = firstDf.append(first)
    secondDf = secondDf.append(second)
    thirdDf = thirdDf.append(third)

def CategoryPageCrawler(category_num, categoryIdx):
    global recipeId
    Url = page_baseUrl + category_num + "&order=reco&page="

    for i in range(1, pageNumList[categoryIdx] + 1):
        pageUrl = Url + str(i)
        page = requests.get(pageUrl)
        soup = BeautifulSoup(page.content, 'html.parser')

        try:
            for href in soup.find("ul", class_="common_sp_list_ul ea4").find_all("li"):
                recipeUrl = "https://www.10000recipe.com" + href.find("a")["href"]
                recipeId = recipeId + 1
                RecipePageCrawler(recipeUrl, categoryIdx, recipeId)
                # print("\n\n\n")
        except(AttributeError):
            return

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

i = 0
for category_num in categoryNumList:
    CategoryPageCrawler(category_num, i)
    i = i + 1


firstDf.to_csv("first.csv", encoding='utf-8-sig')
secondDf.to_csv("second.csv", encoding='utf-8-sig')
thirdDf.to_csv("third.csv", encoding='utf-8-sig')
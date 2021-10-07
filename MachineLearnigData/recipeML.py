import io
import pandas as pd

userData = pd.read_csv('/Users/leegyeonghu/myCapstoneProj/MachineLearnigData/UserRating.csv')
recipeData = pd.read_csv('/Users/leegyeonghu/myCapstoneProj/MachineLearnigData/recipeDataSet.csv')

# 두 데이터프레임을 합쳐보자
userRecipeRating = pd.merge(userData,recipeData,on = 'rId')

# 닉네임은 필요 없으니 여기서는 제거해주자 Unnamed도 같이 제거
userRecipeRating = userRecipeRating.drop('nick',axis = 1)
userRecipeRating = userRecipeRating.drop('Unnamed: 0_x',axis = 1)
userRecipeRating = userRecipeRating.drop('Unnamed: 0_y',axis = 1)

#정렬
userRecipeRating = userRecipeRating.sort_values(by= 'userId',ascending=True)

# 4등분하여 진행해보자.
n = int(277441/8)

subRUR1 = userRecipeRating[:n]
subRUR2 = userRecipeRating[n:n*2]
subRUR3 = userRecipeRating[n*2:n*3]
subRUR4 = userRecipeRating[n*3:n*4]
subRUR5 = userRecipeRating[n*4:n*5]
subRUR6 = userRecipeRating[n*5:n*6]
subRUR7 = userRecipeRating[n*6:n*7]
subRUR8 = userRecipeRating[n*7:277441]

#1번째 섹션
matrix1 = subRUR1.pivot_table('grade',index = 'recipe_title',columns='userId')
matrix2 = subRUR2.pivot_table('grade',index = 'recipe_title',columns='userId')

#2번째 섹션
matrix3 = subRUR3.pivot_table('grade',index = 'recipe_title',columns='userId')
matrix4 = subRUR4.pivot_table('grade',index = 'recipe_title',columns='userId')

#3번째 섹션
matrix5 = subRUR5.pivot_table('grade',index = 'recipe_title',columns='userId')
matrix6 = subRUR6.pivot_table('grade',index = 'recipe_title',columns='userId')

#4번째 섹션
matrix7 = subRUR7.pivot_table('grade',index = 'recipe_title',columns='userId')
matrix8 = subRUR8.pivot_table('grade',index = 'recipe_title',columns='userId')

print("Matrix is done!")

# N/A -> 0으로 채우기
matrix1 = matrix1.fillna(0).astype(int)
matrix2 = matrix2.fillna(0).astype(int)
matrix3 = matrix3.fillna(0).astype(int)
matrix4 = matrix4.fillna(0).astype(int)
matrix5 = matrix5.fillna(0).astype(int)
matrix6 = matrix6.fillna(0).astype(int)
matrix7 = matrix7.fillna(0).astype(int)
matrix8 = matrix8.fillna(0).astype(int)

print("결측값 is done!")

complete_matrix1 = pd.merge(matrix1,matrix2)
print("merge 1st-1 is done!")
complete_matrix2 = pd.merge(matrix3,matrix4)
print("merge 1st-2 is done!")
complete_matrix3 = pd.merge(matrix5,matrix6)
print("merge 1st-3 is done!")
complete_matrix4 = pd.merge(matrix7,matrix8)
print("merge 1st-4 is done!")

print("merge 1st is done!")

compMatrix1 = pd.merge(complete_matrix1,complete_matrix2)
print("merge 2nd is done!")
compMatrix2 = pd.merge(complete_matrix3,complete_matrix4)
print("merge 2nd is done!")

compMatrix = pd.merge(compMatrix1,compMatrix2)

print("merge 3rd is done!")

compMatrix.head(20)

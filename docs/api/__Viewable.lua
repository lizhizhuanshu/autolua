---可以找图找色的对象
---@class Viewable
local M = {}


---范围内查找符合点阵特征的点的坐标，返回第一个找到的点坐标，没找到则返回-1，-1
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@param dotMatrix DotMatrix|nil 将点阵数据填充到其中
---@return DotMatrix 点阵
function M:getDotMatrixByShiftColor(x,y,x1,y1,color,shiftColor,dotMatrix)
end

---范围内查找符合点阵特征的点的坐标，返回第一个找到的点坐标，没找到则返回-1，-1
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与特征颜色计算的差和小于等于此参数则符合，大于则不符合
---@param dotMatrix DotMatrix|nil 将点阵数据填充到其中
---@return DotMatrix 点阵
function M:getDotMatrixByShiftColorSum(x,y,x1,y1,color,shiftColorSum,dotMatrix)
end

---获取范围内符合颜色特征的坐标与颜色
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return table 坐标与颜色的数据
function M:getColorCoordMatrixByShiftColor(x,y,x1,y1,color,shiftColor)
end

---获取范围内符合颜色特征的坐标与颜色
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return table 坐标与颜色的数据
function M:getColorCoordMatrixByShiftColorSum(x,y,x1,y1,color,shiftColorSum)
end


---获取屏幕指定坐标的颜色
---@param x integer 横坐标
---@param y integer 纵坐标
---@return integer 坐标颜色，格式BGR
function M:getColor(x,y)
end


---用屏幕指定坐标的颜色与多个颜色进行对比，返回第一个符合的索引，没有则返回0
---@param x integer 横坐标
---@param y integer 纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return integer 第一个符合屏幕颜色的颜色特征的索引，没有符合的返回0
function M:whichColorByShiftColor(x,y,color,shiftColor)
end

---用屏幕指定坐标的颜色与多个颜色进行对比，返回第一个符合的索引，没有则返回0
---@param x integer 横坐标
---@param y integer 纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return integer 第一个符合屏幕颜色的颜色特征的索引，没有符合的返回0
function M:whichColorByShiftColorSum(x,y,color,shiftColorSum)
end


---获取范围内符合颜色特征的点的计数
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return integer 符合颜色特征的总数
function M:getColorCountByShiftColor(x,y,x1,y1,color,shiftColor)
end


---获取范围内符合颜色特征的点的计数
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return integer 符合颜色特征的总数
function M:getColorCountByShiftColorSum(x,y,x1,y1,color,shiftColorSum)
end


---范围内查找符合指定颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param findOrder integer 查找的方向
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return integer 找到的点的横坐标
---@return integer 找到的点的纵坐标
function M:findColorByShiftColor(x,y,x1,y1,color,shiftColor,findOrder)
end

---范围内查找符合指定颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param findOrder integer 查找的方向
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return integer 找到的点的横坐标
---@return integer 找到的点的纵坐标
function M:findColorByShiftColorSum(x,y,x1,y1,color,shiftColorSum,findOrder)
end



---范围内查找符合坐标颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param findOrder integer 查找的方向
---@param color integer|string|ColorFeature 颜色特征
---@param coordColor string|CoordColorFeature 偏移坐标颜色特征
---@param shiftColor integer 偏移颜色
---@param canErrorSum integer       允许错误的坐标颜色的总数，当对比坐标颜色时允许一定数量的点颜色不符
---@return integer 找到的点的横坐标
---@return integer 找到的点的纵坐标
function M:findFeatureByShiftColor(x,y,x1,y1,color,coordColor,shiftColor,canErrorSum,findOrder)
end

---范围内查找符合坐标颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param findOrder integer 查找的方向
---@param color integer|string|ColorFeature 颜色特征
---@param coordColor string|CoordColorFeature 偏移坐标颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@param canErrorSum integer       允许错误的坐标颜色的总数，当对比坐标颜色时允许一定数量的点颜色不符
---@return integer 找到的点的横坐标
---@return integer 找到的点的纵坐标
function M:findFeatureByShiftColorSum(x,y,x1,y1,color,coordColor,shiftColorSum,canErrorSum,findOrder)
end



---对比坐标颜色的特征
---@param coordColor string|CoordColorFeature 坐标颜色特征
---@param shiftColor integer 偏移颜色
---@param canErrorSum integer       允许错误的坐标颜色的总数，当对比坐标颜色时允许一定数量的点颜色不符
---@return boolean                  是否符合坐标颜色特征
function M:isFeatureByShiftColor(coordColor,shiftColor,canErrorSum)
end

---对比坐标颜色的特征
---@param coordColor string|CoordColorFeature 坐标颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@param canErrorSum integer       允许错误的坐标颜色的总数，当对比坐标颜色时允许一定数量的点颜色不符
---@return boolean                  是否符合坐标颜色特征
function M:isFeatureByShiftColorSum(coordColor,shiftColorSum,canErrorSum)
end
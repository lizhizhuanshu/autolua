---‘userdata’类型的，包含了颜色的二进制数据
---@class ColorFeature
---@field toString fun():string

---‘userdata’类型的，包含了一个或多个颜色与坐标数据的二进制数据
---@class CoordColorFeature
---@field toString fun():string

local M = {
    ---找图找色函数超出屏幕范围时，抛出的错误消息
    COORDINATES_OVERFLOW = "The coordinates are off screen",
    ---找图找色时使用的查找方向
    FIND_ORDER = {
        UP_DOWN_LEFT_RIGHT=0,
        UP_DOWN_RIGHT_LEFT=1,
        DOWN_UP_LEFT_RIGHT=2,
        DOWN_UP_RIGHT_LEFT=3,
        LEFT_RIGHT_UP_DOWN=4,
        LEFT_RIGHT_DOWN_UP=5,
        RIGHT_LEFT_UP_DOWN=6,
        RIGHT_LEFT_DOWN_UP=7,
    }
}


---位图‘userdata’类型的，包含了二进制图色数据
---@class Bitmap

---‘userdata’类型的点阵数据
---@class DotMatrix
---@field toTable fun():table
---@field height fun():integer
---@field width fun():integer
---@field findMatrix fun(matrix:DotMatrix,sim:number):integer,integer


---将table类型的点阵数据转化为userdata类型的以便于使用
---@param t table ‘table’类型的点阵数据
---@return DotMatrix ‘userdata’类型的点阵数据
function M.newDotMatrix(t)
    
end


---范围内查找符合点阵特征的点的坐标，返回第一个找到的点坐标，没找到则返回-1，-1
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@param dotMatrix DotMatrix|nil 将点阵数据填充到其中
---@return DotMatrix 点阵
function M.getDotMatrixByShiftColor(bitmap,x,y,x1,y1,color,shiftColor,dotMatrix)
end

---范围内查找符合点阵特征的点的坐标，返回第一个找到的点坐标，没找到则返回-1，-1
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与特征颜色计算的差和小于等于此参数则符合，大于则不符合
---@param dotMatrix DotMatrix|nil 将点阵数据填充到其中
---@return DotMatrix 点阵
function M.getDotMatrixByShiftColorSum(bitmap,x,y,x1,y1,color,shiftColorSum,dotMatrix)
end

---获取范围内符合颜色特征的坐标与颜色
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return table 坐标与颜色的数据
function M.getColorCoordMatrixByShiftColor(bitmap,x,y,x1,y1,color,shiftColor)
end

---获取范围内符合颜色特征的坐标与颜色
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return table 坐标与颜色的数据
function M.getColorCoordMatrixByShiftColorSum(bitmap,x,y,x1,y1,color,shiftColorSum)
end


---获取屏幕指定坐标的颜色
---@param bitmap Bitmap 位图
---@param x integer 横坐标
---@param y integer 纵坐标
---@return integer 坐标颜色，格式BGR
function M.getColor(bitmap,x,y)
end


---用屏幕指定坐标的颜色与多个颜色进行对比，返回第一个符合的索引，没有则返回0
---@param bitmap Bitmap 位图
---@param x integer 横坐标
---@param y integer 纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return integer 第一个符合屏幕颜色的颜色特征的索引，没有符合的返回0
function M.whichColorByShiftColor(bitmap,x,y,color,shiftColor)
end

---用屏幕指定坐标的颜色与多个颜色进行对比，返回第一个符合的索引，没有则返回0
---@param bitmap Bitmap 位图
---@param x integer 横坐标
---@param y integer 纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return integer 第一个符合屏幕颜色的颜色特征的索引，没有符合的返回0
function M.whichColorByShiftColorSum(bitmap,x,y,color,shiftColorSum)
end


---获取范围内符合颜色特征的点的计数
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return integer 符合颜色特征的总数
function M.getColorCountByShiftColor(bitmap,x,y,x1,y1,color,shiftColor)
end


---获取范围内符合颜色特征的点的计数
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return integer 符合颜色特征的总数
function M.getColorCountByShiftColorSum(bitmap,x,y,x1,y1,color,shiftColorSum)
end


---范围内查找符合指定颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param findOrder integer 查找的方向
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColor integer 偏移颜色
---@return integer 找到的点的横坐标
---@return integer 找到的点的纵坐标
function M.findColorByShiftColor(bitmap,x,y,x1,y1,color,shiftColor,findOrder)
end

---范围内查找符合指定颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param bitmap Bitmap 位图
---@param x integer 最小的横坐标
---@param y integer 最小的纵坐标
---@param x1 integer 最大的横坐标
---@param y1 integer 最大的纵坐标
---@param findOrder integer 查找的方向
---@param color integer|string|ColorFeature 颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@return integer 找到的点的横坐标
---@return integer 找到的点的纵坐标
function M.findColorByShiftColorSum(bitmap,x,y,x1,y1,color,shiftColorSum,findOrder)
end



---范围内查找符合坐标颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param bitmap Bitmap 位图
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
function M.findFeatureByShiftColor(bitmap,x,y,x1,y1,color,coordColor,shiftColor,canErrorSum,findOrder)
end

---范围内查找符合坐标颜色特征的点，返回第一个找到的点坐标，没找到则返回-1，-1
---@param bitmap Bitmap 位图
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
function M.findFeatureByShiftColorSum(bitmap,x,y,x1,y1,color,coordColor,shiftColorSum,canErrorSum,findOrder)
end



---对比坐标颜色的特征
---@param bitmap Bitmap 位图
---@param coordColor string|CoordColorFeature 坐标颜色特征
---@param shiftColor integer 偏移颜色
---@param canErrorSum integer       允许错误的坐标颜色的总数，当对比坐标颜色时允许一定数量的点颜色不符
---@return boolean                  是否符合坐标颜色特征
function M.isFeatureByShiftColor(bitmap,coordColor,shiftColor,canErrorSum)
end

---对比坐标颜色的特征
---@param bitmap Bitmap 位图
---@param coordColor string|CoordColorFeature 坐标颜色特征
---@param shiftColorSum integer 屏幕颜色与指定颜色计算的差和小于等于此参数则符合，大于则不符合
---@param canErrorSum integer       允许错误的坐标颜色的总数，当对比坐标颜色时允许一定数量的点颜色不符
---@return boolean                  是否符合坐标颜色特征
function M.isFeatureByShiftColorSum(bitmap,coordColor,shiftColorSum,canErrorSum)
end

return M
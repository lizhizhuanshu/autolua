---‘userdata’类型的点阵数据
---@class DotMatrix
---@field toTable fun():table
---@field height fun():integer
---@field width fun():integer
---@field findMatrix fun(matrix:DotMatrix,sim:number):integer,integer


---将table类型的点阵数据转化为userdata类型的以便于使用
---@param t table ‘table’类型的点阵数据
---@return DotMatrix ‘userdata’类型的点阵数据
function DotMatrix(t)
end
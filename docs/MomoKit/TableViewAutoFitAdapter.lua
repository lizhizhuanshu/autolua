---
--- Generated by MLN Team (https://www.immomo.com)
--- Created by MLN Team.
--- DateTime: 15-01-2020 17:35
---

---
---  用于自适应Cell高度。需要配合自动布局使用
---
---@note  在init和fillData时候布局尽量不要获取cell.contentView的宽高，因为这时候有可能是不准确的
---@class TableViewAutoFitAdapter @parent class
---@public field name string 
---@type TableViewAutoFitAdapter
local _class = {
	_priveta_class_name = "TableViewAutoFitAdapter"}

---
---  构造方法
---
---
---  初始化一个适配器对象
---
---@return TableViewAutoFitAdapter 
function TableViewAutoFitAdapter()
	return _class
end

---
---  根据复用ID回调高度
---
---@param reuseId string  reuseId:复用ID
---@param callback fun(cell:table, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number row) 
---		 	 ---cell：视图cell 
---		 	 ---row：视图页数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:heightForCellByReuseId(reuseId, callback)
	return self
end

---
---  设置组数回调
---
---@param callback fun():void
---	 回调格式：
---		 ``` 
---		 function() 
---		 	 ---在回调中返回组数，默认为1 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  该方法不设置，默认组数为1
function _class:sectionCount(callback)
	return self
end

---
---  设置行数回调
---
---
---  根据组数返回对应的行数
---
---@param callback fun(section:number):void
---	 回调格式：
---		 ``` 
---		 function(number section) 
---		 	 ---section:组数，根据组数返回对应的行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:rowCount(callback)
	return self
end

---
---  设置回调复用ID
---
---
---  根据组数和行数返回对应cell的复用ID
---
---@param callback fun(section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(number section,number row) 
---		 	 ---section：组数 
---		 	 ---row：行数  
---		 	 ---返回复用ID，string 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  使用该方法需要配合initCellByReuseId和fillCellDataByReuseId方法,默认id写法与此方法不要同时使用
function _class:reuseId(callback)
	return self
end

---
---  设置初始化cell的回调
---
---
---  根据复用ID，组数和行数进行初始化cell的回调
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(cell:table):void
---	 回调格式：
---		 ``` 
---		 function(table cell) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  使用该方法，配合fillCellDataByReuseId和reuseId方法，注意：方法中获取cell中控件宽/高是不准确的
function _class:initCellByReuseId(reuseId, callback)
	return self
end

---
---  设置进行数据赋值的回调
---
---
---  根据复用ID，组数和行数进行cell的数据赋值操作
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  使用该方法，配合reuseId和initCellByReuseId方法，注意：方法中获取cell中控件宽/高是不准确的
function _class:fillCellDataByReuseId(reuseId, callback)
	return self
end

---
---  设置初始化cell的回调
---
---@param callback fun(cell:table):void
---	 回调格式：
---		 ``` 
---		 function(table cell) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  注意：方法中获取cell中控件宽/高是不准确的
function _class:initCell(callback)
	return self
end

---
---  设置cell赋值的回调
---
---
---  根据cell，组数和行数对cell进行赋值操作
---
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  注意：方法中获取cell中控件宽/高是不准确的
function _class:fillCellData(callback)
	return self
end

---
---  点击了某行
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:selectedRowByReuseId(reuseId, callback)
	return self
end

---
---  设置点击cell的回调
---
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:selectedRow(callback)
	return self
end

---
---  设置某个reuseID对应cell的长按回调
---
---
---  设置某个reuseID对应cell的长按回调，触发时长0.5s
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:longPressRowByReuseId(reuseId, callback)
	return self
end

---
---  设置cell的长按回调
---
---
---  设置cell的长按回调，触发时长0.5s
---
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:longPressRow(callback)
	return self
end

---
---  设置返回某行的高度的回调
---
---@param callback fun(section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(number section,number row) 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 	 ---返回高度,number 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:heightForCell(callback)
	return self
end

---
---  设置返回某行的高度的回调
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(number section,number row) 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 	 ---返回高度,number 
---		 end
---		```
---@return TableViewAutoFitAdapter 
function _class:heightForCellByReuseId(reuseId, callback)
	return self
end

---
---  cell将要展示的回调
---
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  iOS端会在刚刚展示的时候就调用，Android会在完全展示后调用
function _class:cellWillAppear(callback)
	return self
end

---
---  cell已经消失后的回调
---
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  注意时机问题，即该回调的调用时机是cell已经消失
function _class:cellDidDisappear(callback)
	return self
end

---
---  cell将要展示时的回调
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  需配合reuseId方法使用，iOS端会在刚刚展示的时候就调用，Android会在完全展示后调用
function _class:cellWillAppearByReuseId(reuseId, callback)
	return self
end

---
---  cell已经消失后的回调
---
---@param reuseId string  reuseId：复用ID
---@param callback fun(cell:table, section:number, row:number):void
---	 回调格式：
---		 ``` 
---		 function(table cell,number section,number row) 
---		 	 ---cell：cell视图表, 类型为Lua中的table，表中仅存在一个元素contentView 
---		 	 ---section：组数 
---		 	 ---row：行数 
---		 end
---		```
---@return TableViewAutoFitAdapter 
---@note  需配合reuseId方法使用
function _class:cellDidDisappearByReuseId(reuseId, callback)
	return self
end

---
---  点击Cell后高亮
---
---@param isShow boolean  是否开启，默认关闭
---@return TableViewAutoFitAdapter 
function _class:showPressed(isShow)
	return self
end

---
---  获取是否开启了高亮效果
---
---@return boolean 布尔值
function _class:showPressed()
	return true
end

---
---  点击后的高亮颜色
---
---@param pressedColor Color  设置cell点击后的高亮颜色
---@return TableViewAutoFitAdapter 
function _class:pressedColor(pressedColor)
	return self
end

---
---  获取高亮颜色
---
---@return Color 色值
function _class:pressedColor()
	return Color()
end

return _class

---@class UiSelector
local UiSelector = {}

---@return UiSelector
function UiSelector:className(className)
    
end

---@return UiSelector
function UiSelector:classNamePattern(classNamePattern)
    
end

---@return UiSelector
function UiSelector:classNameStartsWith(str)
    
end

---@return UiSelector
function UiSelector:classNameEndsWith(str)
    
end

---@return UiSelector
function UiSelector:classNameContains(str)
    
end

---@return UiSelector
function UiSelector:description(description)
    
end

---@return UiSelector
function UiSelector:descriptionPattern(descriptionPattern)
    
end

---@return UiSelector
function UiSelector:descriptionStartsWith(str)
    
end

---@return UiSelector
function UiSelector:descriptionEndsWith(str)
    
end

---@return UiSelector
function UiSelector:descriptionContains(str)
    
end


---@return UiSelector
function UiSelector:packageName(packageName)
    
end

---@return UiSelector
function UiSelector:packageNamePattern(packageNamePattern)
    
end


---@return UiSelector
function UiSelector:packageNameStartsWith(str)
    
end

---@return UiSelector
function UiSelector:packageNameEndsWith(str)
    
end

---@return UiSelector
function UiSelector:packageNameContains(str)
    
end


---@return UiSelector
function UiSelector:resource(resource)
    
end

---@return UiSelector
function UiSelector:resourcePattern(resourcePattern)
    
end

---@return UiSelector
function UiSelector:resourceStartsWith(str)
    
end

---@return UiSelector
function UiSelector:resourceEndsWith(str)
    
end

---@return UiSelector
function UiSelector:resourceContains(str)
    
end



---@return UiSelector
function UiSelector:text(text)
    
end

---@return UiSelector
function UiSelector:textPattern(textPattern)
    
end

---@return UiSelector
function UiSelector:textStartsWith(str)
    
end

---@return UiSelector
function UiSelector:textEndsWith(str)
    
end

---@return UiSelector
function UiSelector:textContains(str)
    
end


---@return UiSelector
function UiSelector:checkable(is)
    
end
---@return UiSelector
function UiSelector:checked(is)
    
end
---@return UiSelector
function UiSelector:clickable(is)
    
end


---@return UiSelector
function UiSelector:enabled(is)
    
end


---@return UiSelector
function UiSelector:focusable(is)
    
end

---@return UiSelector
function UiSelector:focused(is)
    
end


---@return UiSelector
function UiSelector:longClickable(is)
    
end


---@return UiSelector
function UiSelector:scrollable(is)
    
end


---@return UiSelector
function UiSelector:selected(is)
    
end

---@return UiSelector
function UiSelector:visible(is)
    
end

---@return UiSelector
function UiSelector:depth(min,max)
    
end

---@return UiSelector
function UiSelector:toString()
    
end


---@class Searchable
local Searchable={}

---判断是否拥有符合选择器的对象
---@param selector UiSelector UI选择器
---@return boolean
function Searchable:hasObject(selector)
    
end

---查找符合选择器的UI对象,返回第一个符合要求的UI对象
---@param selector  UiSelector
---@return UiObject
function Searchable:findObject(selector)
    
end

---查找符合选择器的UI对象,并返回所有符合要求的UI对象
---@param selector UiSelector
---@return table<integer,UiObject>
function Searchable:findObjects(selector)
    
end


---@class UiObject:Searchable
local UiObject = {}

function UiObject:getChildCount()
    
end

---@return UiObject
function UiObject:getChild(index)
    
end

---@return table<integer,UiObject>
function UiObject:getChildren()
    
end


function UiObject:getVisibleBounds()
    
end

function UiObject:getClassName()
    
end

function UiObject:getContentDescription()
    
end

function UiObject:getPackageName()
    
end

function UiObject:getResourceName()
    
end

function UiObject:getText()
    
end

function UiObject:setText(text)
    
end

function UiObject:click()
    
end

function UiObject:longClick()
    
end

function UiObject:select()
    
end

function UiObject:copy()
    
end

function UiObject:paste()
    
end

function UiObject:cut()
    
end

function UiObject:scrollForward()
    
end

function UiObject:scrollBackward()
    
end

function UiObject:scrollDown()
    
end

function UiObject:scrollUp()
    
end

function UiObject:scrollLeft()
    
end

function UiObject:scrollRight()
    
end

function UiObject:scrollTo(row,column)
    
end




function UiObject:isCheckable()
    
end

function UiObject:isChecked()
    
end

function UiObject:isClickable()
    
end

function UiObject:isEnabled()
    
end

function UiObject:isFocusable()
    
end

function UiObject:isFocused()
    
end

function UiObject:isLongClickable()
    
end

function UiObject:isScrollable()
    
end

function UiObject:isSelected()
    
end


function UiObject:toString()
    
end

---@return UiObject
function UiObject:getParent()
    
end


---@class UiDriver:Searchable
local UiDriver = {}

---生成一个新的Ui选择器
---@return UiSelector
function UiDriver:newSelector()
    
end


function UiDriver:waitForIdle(idleTimeout,globalTimeout)
    
end
Screen:initialize(0,0)

---@type LayoutParams
local layoutParams = {
    width = -1,
    height = -1,
    flags = LayoutParamsFlags.FLAG_NOT_FOCUSABLE,
    format = PixelFormat.RGBA_8888,
}

local floatView = UI:newFloatView("test","file://view.lua",layoutParams)
floatView:show()
print("等待一秒钟，等待图片悬浮窗正常显示了")
Thread:sleep(1000)
print("更新虚拟内的图色数据")
Screen:update()
--下面是我事先取好的关于图片的特征数据，我用的是1080x1920的手机设备，可能因为
--设备不同而造成找不到结果的问题，可根据不同设备自行取点
local color,feature = 0x333333,"8|12|333333,-14|45|353432,-12|96|333333,28|124|333333,76|107|333333,86|63|333333,33|57|333333,32|88|333333,72|1|333333"
local shiftColor = 0x101010--确定我们的偏移颜色特征
local maxX,maxY = Screen:getWidthHeight()

print("全屏查找刚才显示的图片")
local x,y = Screen:findFeatureByShiftColor(0,0,maxX-1,maxY-1,color,feature,shiftColor,0,0)
print("找到的坐标是多少",x,y)


if x >-1 then
    print("找到颜色 0x333333 的计数",Screen:getColorCountByShiftColor(x,y,x+50,y+50,0x333333,shiftColor))
    print("根据找到的坐标把偏移坐标颜色特征转换为 绝对坐标颜色特征")
    local newFeature = string.gsub(feature,"(%-?%d+)|(%-?%d+)|([^,]+)",function (ox,oy,color)
        ox,oy = tonumber(ox) + x,tonumber(oy)+y
        return string.format("%d|%d|%s",ox,oy,color)
    end)
    print("打印出根据找到的坐标拼接好的对比特征",newFeature)
    print("打印比色特征结果",Screen:isFeatureByShiftColor(newFeature,shiftColor,0))
end
floatView:conceal()
print("等待一段时间确保悬浮窗口已经隐藏了")
Thread:sleep(1000)
print("如果上面找到正常的话，这里依旧可以再次找到")
x,y = Screen:findFeatureByShiftColor(0,0,maxX-1,maxY-1,color,feature,shiftColor,0,0)
print("找到的坐标是多少",x,y)
print("刷新一下虚拟屏幕数据，应该就找不到了")
Screen:update()
x,y = Screen:findFeatureByShiftColor(0,0,maxX-1,maxY-1,color,feature,shiftColor,0,0)
print("找到的坐标是多少",x,y)



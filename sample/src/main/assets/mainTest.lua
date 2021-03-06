local screen = require "screen"
print("----------------------------------")
print(screen)
print("打印屏幕的宽和高",screen:getWidthHeight())
print("开始测试注册进来的TestShow")
TestShow:showMessage("这是在root进程中的lua消息")

local screen = require "screen"
print("----------------------------------")
print(screen)
screen:initialize(0,0)
print("打印屏幕的宽和高",screen:getWidthHeight())
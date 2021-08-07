local tess = TessBaseAPIFactory:create()

tess:init(ROOT_PATH .. "/assets","chi_sim")

local function printRecognizeResult()
    print(tess:getUTF8Text())
    local results = tess:getResultIterator()
    repeat
        print("文字所在位置",results:getBoundingRect(PageIteratorLevel.RIL_WORD))
        print("识别出的文字",results:getUTF8Text(PageIteratorLevel.RIL_WORD))
    until not results:next(PageIteratorLevel.RIL_WORD)
end
print("-----------开始打印屏幕识别文字的结果---------------")
tess:setImageFromDisplay()
printRecognizeResult()
print("-----------开始打印test.png图片识别文字的结果--------------")
tess:setImageFromFile(ROOT_PATH .. "/assets/image/test.png")
tess:setScope(8, 407, 415, 628)
printRecognizeResult()

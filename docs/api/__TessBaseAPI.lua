

---@class TessBaseAPIFactory
local TessBaseAPIFactory = {}

---@return TessBaseAPI
function TessBaseAPIFactory:create()
    
end


---页拆分的方式,参数的详细说明请查看Tesseract的文档
---@class PageSegMode
local PageSegMode = {
    PSM_OSD_ONLY = 0;
    PSM_AUTO_OSD = 1;
    PSM_AUTO_ONLY = 2;
    PSM_AUTO = 3;
    PSM_SINGLE_COLUMN = 4;
    PSM_SINGLE_BLOCK_VERT_TEXT = 5;
    PSM_SINGLE_BLOCK = 6;
    PSM_SINGLE_LINE = 7;
    PSM_SINGLE_WORD = 8;
    PSM_CIRCLE_WORD = 9;
    PSM_SINGLE_CHAR = 10;
    PSM_SPARSE_TEXT = 11;
    PSM_SPARSE_TEXT_OSD = 12;
    PSM_RAW_LINE = 13;
}


---文字识别引擎的工作方式
---@class OcrEngineMode
local OcrEngineMode ={
    OEM_TESSERACT_ONLY = 0;
    OEM_CUBE_ONLY = 1;
    OEM_TESSERACT_CUBE_COMBINED = 2;
    OEM_DEFAULT = 3;
}

---页面迭代器的层次
---@class PageIteratorLevel
local PageIteratorLevel = {
    RIL_BLOCK = 0;
    RIL_PARA = 1;
    RIL_TEXTLINE = 2;
    RIL_WORD = 3;
    RIL_SYMBOL = 4;
}

---详细说明请查阅Tesseractor的文档
---@class ResultIterator
local ResultIterator = {}

---@param level integer 只能是PageIteratorLevel中的某个值
function ResultIterator:next(level)
    
end

---@param level integer 只能是PageIteratorLevel中的某个值
function ResultIterator:getUTF8Text(level)
    
end

---@param level integer 只能是PageIteratorLevel中的某个值
function ResultIterator:getBoundingRect(level)
    
end

function ResultIterator:delect()
    
end

---@param level integer 只能是PageIteratorLevel中的某个值
function ResultIterator:confidence(level)
    
end

---@param level integer 只能是PageIteratorLevel中的某个值
function ResultIterator:isAtBeginningOf(level)
    
end

---@param level integer 只能是PageIteratorLevel中的某个值
function ResultIterator:isFinalElement(level,element)
    
end


---@class TessBaseAPI
local TessBaseAPI = {}


---初始化Tesseract,识别前必须调用
---@param tesseractDataPath string 语言模板文件所在的目录,他必须有子目录tessdata
---@param language string 语言类型
---@param engineMode integer 只能是PageSegMode中的类型
function TessBaseAPI:init(tesseractDataPath,language,engineMode)
    
end

---识别并获取识别结果
---@return string
function TessBaseAPI:getUTF8Text()
    
end

---将当前屏幕的图片加载到Tesseract中,识别前必须调用
function TessBaseAPI:setImageFromDisplay()
    
end

---将指定路径的图片文件加载到Tesseract中,识别前必须调用
---@param path string 文件路径
function TessBaseAPI:setImageFromFile(path)
    
end

---清理TessBaseAPI内的数据
function TessBaseAPI:clear()
    
end

---彻底释放TessBaseAPI中的数据
function TessBaseAPI:release()
    
end

---获取指定盒子内的文字
---@param page integer 页的索引
---@return string
function TessBaseAPI:getBoxText(page)
    
end

---将识别限制到图像的一个子矩形区域,SetImage之后调用此函数。
---每一次该函数调用后将清除识别结果，以便同一张图像可以进行多矩形区域的识别。
---@param x integer 矩形的左上角横坐标
---@param y integer 矩形的左上角纵坐标
---@param width integer 矩形的宽度
---@param height integer 矩形的高度
function TessBaseAPI:setRectangle(x,y,width,height)
    
end

---将识别限制到图像的一个子矩形区域,SetImage之后调用此函数。
---每一次该函数调用后将清除识别结果，以便同一张图像可以进行多矩形区域的识别。
---@param x integer 左上角横坐标
---@param y integer 左上角纵坐标
---@param x1 integer 右下角横坐标
---@param y1 integer 右下角纵坐标
function TessBaseAPI:setScope(x,y,x1,y1)
    
end


---返回识别的结果
---@return ResultIterator
function TessBaseAPI:getResultIterator()
    
end

---获取识别后文字的所有置信度,置信度取值范围是0-100
---@return table
function TessBaseAPI:wordConfidences()
    
end

---设置页拆分的方式
---@param mode integer PageSegMode中相关的参数
function TessBaseAPI:setPageSegMode(mode)
    
end

---获取页拆分的方式
---@return integer mode PageSegMode中相关的参数
function TessBaseAPI:getPageSegMode()
    
end

---设置引擎参数
---@param var string
---@param value string
function TessBaseAPI:setVariable(var,value)
    
end
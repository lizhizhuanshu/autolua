local image = ImageView()
-- 这个图片的路径就在文件夹image中
image:setImageUrl("file://image/auxiliary.png")
image:width(48)
image:height(48)
image:setGravity(Gravity.CENTER)
window:addView(image)
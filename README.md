# TransferImage

仿微博、微信、qq 点击缩略图后预览高清图的组件

TransferImage 是一款模仿微博、微信、qq的高清图查看控件, 实现了在列表控件(ListView, RecycleView, GridView)中
点击缩略图后播放过渡动画, 加载高清图, 加载高清图时同时显示加载进度条, 加载完成后显示高清图的一个组件。同时关闭
TransferImage 也会有对应的过渡动画.

支持或包含的功能：

    - 打开和关闭 TransferImage 的过渡动画, 支持自定义
    - 图片加载内置了一个使用 Glide 的图片加载器, 支持自定义
    - 图片支持手势操作, 可缩放、双击、移动
    - 图片加载时的进度条, 支持自定义
    - 图片索引指示器, 支持自定义

# Preview

预览图图片质量有问题, 会出现卡顿的现象, 真实项目中不会出现.

<img src="preview/transfer_1.gif" width="265px"/>
<img src="preview/transfer_2.gif" width="265px"/>
<img src="preview/transfer_3.gif" width="265px"/>

# Usage

    transferLayout = new TransferImage.Builder(GroupImageActivity.this)
            .setBackgroundColor(Color.BLACK)
            .setMissPlaceHolder(R.mipmap.ic_launcher)
            .setOriginImageList(imageViewList)
            .setImageUrlList(imageStrList)
            .setOriginIndex(imageViewList.indexOf(v))
            .setOffscreenPageLimit(1)
            .create();
    transferLayout.show();

注意：需要在 Activity 中重写 onBackPressed 方法

    @Override
    public void onBackPressed() {
        if (transferLayout != null && transferLayout.isShown()) {
            transferLayout.dismiss();
        } else {
            super.onBackPressed();
        }
    }

详细示例代码可以参考 [TransferImageDemo](https://github.com/Hitomis/TransferImage/blob/master/app/src/main/java/com/hitomi/transferimage)

# Attribute

| 属性 | 说明 |
| :--: | :--: |
| originImages | 缩略图 ImageView 数组 |
| originImageList | 缩略图 ImageView 集合 |
| originIndex | 如果是一组缩略图, originIndex 表示当前点击的缩略图下标位置, 否则 originIndex 默认为 0 |
| offscreenPageLimit | 显示 TransferImage 时初始化加载的图片数量, 默认为1, 表示第一次加载3张(originIndex, originIndex + 1, originIndex - 1); 值为 2, 表示加载5张。依次类推 |
| backgroundColor | TransferImage 背景色 |
| missPlaceHolder | 缺省的占位图, 假设缩略图为 N 张, 而 TransferImage 要显示 N + 1 张高清图, 那么 missPlaceHolder 表示为第 N + 1 张图加载完成之前的展位图 |
| imageUrls | 高清图地址数组 |
| imageUrlList | 高清图地址集合 |
| transferAnima | 从缩略图到高清图的过渡动画 (默认内置 TransitionAnimator), 可自实现 ITransferAnimator 接口定义自己的过渡动画 |
| progressIndicat | 加载高清图的进度条 (默认内置 ProgressPieIndicator), 可自实现 IProgressIndicator 接口定义自己的图片加载进度条 |
| indexIndicator | 图片索引指示器 (默认内置 IndexCircleIndicator), 可自实现 IIndexIndicator 接口定义自己的图片索引指示器|
| imageLoader | 图片加载器 (默认内置 GlideImageLoader), 可自实现 ImageLoader 接口定义自己的图片加载器|


#Licence

      Copyright 2016 Hitomis, Inc.

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
 



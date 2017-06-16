# Transferee
transferee 可以帮助你完成从缩略图到原图的无缝过渡转变，功能体系仿照并涵盖 ios 版本的 QQ、微信朋友圈、新浪微博的图片浏览功能。

transferee 支持两种模式：

1. 只有原图，就是说九宫格列表中的图片和全屏显示的大图其实来源于一张图片。详见 [GlideNoThumActivity](https://github.com/Hitomis/transferee/blob/master/app/src/main/java/com/hitomi/transferimage/activity/glide/GlideNoThumActivity.java) 和 [UniversalNoThumActivity](https://github.com/Hitomis/transferee/blob/master/app/src/main/java/com/hitomi/transferimage/activity/universal/UniversalNoThumActivity.java)
2. 既有原图，又有缩略图，例如我司使用了阿里云的图片裁剪功能提供了缩略图来源，在列表页使用阿里云裁剪后的缩略图，优化列表数据流量和流畅度，同时又能在详情页或者图片查看器中显示大图。这种情况下也是 transferee 最适合的模式。详见[UniversalNormalActivity](https://github.com/Hitomis/transferee/blob/master/app/src/main/java/com/hitomi/transferimage/activity/universal/UniversalNormalActivity.java)

在使用 transferee 组件的时候，还需要注意一些问题：

由于不同 ImageLoader 的缓存和图片加载策略不同，所以在使用目前 transferee 中内置的 Glide 或者 Universal-Image-Loader 时，所支持的功能体系也是不一样的：

- 使用 Glide 作为 transferee 的图片加载器时，不支持设置 thumbnailImageList 属性，即只支持模式1
- 使用 Glide 作为 transferee 的图片加载器时，如果您的项目中也是使用的 Glide 去加载图片，最好使用 ProgressBarIndicator 作为 transferee 的进度指示器；如果一定要显示出图片的百分比加载进度，即使用 ProgressPieIndicator 的话，那么在显示 transferee 时，应该暂停列表页当前图片的加载。详见 [GlideNoThumActivity](https://github.com/Hitomis/transferee/blob/master/app/src/main/java/com/hitomi/transferimage/activity/glide/GlideNoThumActivity.java)
- 使用 Universal-Image-Loader 作为 transferee 的图片加载器时，且只有原图的场景下，如果您的项目中也是使用的 Universal-Image-Loader 去加载图片，那么 transferee 中将无法显示出当前图片的百分比加载进度，只能使用 ProgressBarIndicator 作为 transferee 的进度指示器。详见 [UniversalNoThumActivity](https://github.com/Hitomis/transferee/blob/master/app/src/main/java/com/hitomi/transferimage/activity/universal/UniversalNoThumActivity.java)
- 缩略图的 ScaleType 需要设置为 centerCrop (这个有点废话了...)

如有任何问题可以提 Issues

# Preview
<img src="preview/transferee_1.gif" />
<img src="preview/transferee_2.gif" />
<img src="preview/transferee_3.gif" />

# Usage

### Dependency
Step 1. Add the JitPack repository to your build file

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```
dependencies {
        compile 'com.github.Hitomis.transferee:Transferee:v1.0.0'
        compile 'com.github.Hitomis.transferee:GlideLoader:v1.0.0' // load with glide
        compile 'com.github.Hitomis.transferee:UniversalLoader:v1.0.0' //  load with universal-image-loader
}
```

图片加载器暂时只支持 glide 与 universal-image-loader。根据你的需要选择一个添加依赖即可

### Demo

step 1: 一个页面只创建一个 transferee 示例 (建议写在 onCreate 方法中)
```
transferee = Transferee.getDefault(context);
```

setp 2: 在需要显示 transferee 的位置为 transferee 创建参数配置器，并 show 出 transferee
```
TransferConfig config = TransferConfig.build()
    .setNowThumbnailIndex(position)
    .setSourceImageList(sourceImageList)
    .setThumbnailImageList(thumbnailImageList)
    .setMissPlaceHolder(R.mipmap.ic_empty_photo)
    .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
    .setOriginImageList(wrapOriginImageViewList(thumbnailImageList.size()))
    .setProgressIndicator(new ProgressPieIndicator())
    .setIndexIndicator(new NumberIndexIndicator())
    .setJustLoadHitImage(true)
    .setImageLoader(UniversalImageLoader.with(getApplicationContext()))
    .create();

transferee.apply(config).show();
```

setp 3: 在 Activity 关闭的时候，销毁 transferee (建议写在 onDestroy 方法中)
```
 transferee.destroy();
```

全部示例代码可以参考 [TransfereeDemo](https://github.com/Hitomis/transferee/tree/master/app/src/main/java/com/hitomi/transferimage/activity)


<img src="preview/demo.png" height="500" width= "320"/>

针对很多朋友询问我 setOriginImageList 方法的传值问题，这里给出统一解释：
这个 originImageList 集合中是你页面中<b style="color:'red'">可见</b>缩略图 ImageView 的集合.

我们拿微信朋友圈举个栗子：

上图中图组一（6张图片）和图组二（3张图片）在 Android 中实现方式经常用一个 RecyclerView 或者 GridView 来展示，当我们需要使用
tranferee 查看图组一那么传递 originImageList 值时，originImageList 集合中就应该有图组一中的6个缩略图 ImageView，你当前点
击图组一中的 n 号图片，那么 nowThumbnailIndex 属性值为 n-1 ;

学过 Java 的人都知道 这里应该满足 nowThumbnailIndex <= originImageList.siez() - 1 && nowThumbnailIndex >=0

图组一和图组二相互独立，没有任何关系。所以在使用 tranferee 查看图组二的时候，传值不会与图组一有任何关系！

# Config
| 属性 | 说明 |
| :--: | :--: |
| nowThumbnailIndex | 缩略图在图组中的索引 |
| offscreenPageLimit | 显示 transferee 时初始化加载的图片数量, 默认为1, 表示第一次加载3张(nowThumbnailIndex, nowThumbnailIndex + 1, nowThumbnailIndex - 1); 值为 2, 表示加载5张。依次类推 |
| missPlaceHolder | 缺省的占位图，资源 id 格式。图片未加载完成时默认显示的图片 |
| missDrawable | 缺省的占位图，Drawable 格式。图片未加载完成时默认显示的图片 |
| errorPlaceHolder | 加载错误的占位图，资源 id 格式。原图加载错误时显示的图片 |
| errorDrawable | 加载错误的占位图，Drawable 格式。原图加载错误时显示的图片 |
| backgroundColor | transferee 显示时，图片后的背景色 |
| duration | transferee 播放过渡动画的动画时长 |
| justLoadHitImage | 是否只加载当前显示在屏幕中的的原图。如果设置为 true，那么只有当 transferee 切换到当前页面时，才会触发当前页面原图的加载动作，否则按 offscreenPageLimit 所设置的数值去做预加载和当前页面的加载动作 |
| originImageList | 缩略图 ImageView 集合 |
| thumbnailImageList | 缩略图路径集合 |
| sourceImageList | 原图路径集合 |
| progressIndicat | 图片加载进度指示器 (默认内置 ProgressPieIndicator 和 ProgressBarIndicator)。可实现 IProgressIndicator 接口定义自己的图片加载进度指示器 |
| indexIndicator | 图片索引指示器 (默认内置 CircleIndexIndicator 和 NumberIndexIndicator)。可实现 IIndexIndicator 接口定义自己的图片索引指示器 |
| imageLoader | 图片加载器 (默认内置 GlideImageLoader 和 UniversalImageLoader)。可实现 ImageLoader 接口定义自己的图片加载器 |
| longClickListener | transferee 长按监听器 |

# Method
| 方法 | 说明 |
| :--: | :--: |
| getDefault(context) | 获取 transferee 实例 |
| apply(config) | 将配置参数对象应用到 transferee 实例中 |
| show() | 打开/显示 transferee |
| show(listener) | 打开/显示 transferee，并监听显示/关闭状态 |
| isShown() | transferee 是否显示 |
| dismiss() | 关闭 transferee |
| clear(imageLoader) | 清除 ImageLoader 中加载的缓存 |
| destroy() | 销毁 transferee |
| setOnTransfereeStateChangeListener(listener) | 设置 transferee 显示/关闭状态改变的监听器 |

# Update log
+ v0.5
  - 优化打开或者关闭 transferee 时背景色显示效果，从直接显示背景色优化为渐变显示。
  - 基于 [#26](https://github.com/Hitomis/transferee/issues/26) 添加 transferee 使用本地图片场景下的 Demo
  - 为 transferee 组件添加背景色自定义功能
  - 为 transferee 组件添加长按监听器，并添加了长按保存图片到相册的示例代码
  - 更新了部分示例代码中失效的图片地址

# Todo
+ [ ] 支持高清大图和长图浏览
+ [ ] 支持下拉手势关闭 transferee
+ [x] 支持扩展图片保存、收藏等相关操作
+ [ ] 优化单个图片使用 transferee 的场景
+ [x] 优化 TransferConfig 的参数配置。将固定不变的配置项从每次的参数对象创建中分离开来

# Licence
    Copyright 2017 Hitomis, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 



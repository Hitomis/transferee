# TransferImage





# Preview

<img src="preview/transfer_1.gif" width="250px"/>
<img src="preview/transfer_2.gif" width="250px"/>
<img src="preview/transfer_3.gif" width="250px"/>


# Import

### Gradle

Step 1. Add the JitPack repository to your build file

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
   
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.Hitomis:CircleMenu:v1.0.2'
	}
   
### Maven
   
Step 1. Add the JitPack repository to your build file

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
   
Step 2. Add the dependency

	<dependency>
	    <groupId>com.github.Hitomis</groupId>
	    <artifactId>CircleMenu</artifactId>
	    <version>v1.0.2</version>
	</dependency>
   
# Usage

布局文件中：

    <com.hitomi.cmlibrary.CircleMenu
        android:id="@+id/circle_menu"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

Activity 中：

    circleMenu = (CircleMenu) findViewById(R.id.circle_menu);

    circleMenu.setMainMenu(Color.parseColor("#CDCDCD"), R.mipmap.icon_menu, R.mipmap.icon_cancel)
            .addSubMenu(Color.parseColor("#258CFF"), R.mipmap.icon_home)
            .addSubMenu(Color.parseColor("#30A400"), R.mipmap.icon_search)
            .addSubMenu(Color.parseColor("#FF4B32"), R.mipmap.icon_notify)
            .addSubMenu(Color.parseColor("#8A39FF"), R.mipmap.icon_setting)
            .addSubMenu(Color.parseColor("#FF6A00"), R.mipmap.icon_gps)
            .setOnMenuSelectedListener(new OnMenuSelectedListener() {

                @Override
                public void onMenuSelected(int index) {}

            }).setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {

                @Override
                public void onMenuOpened() {}

                @Override
                public void onMenuClosed() {}

            });

# Method

| 方法 | 说明 |
| :--: | :--: |
| setMainMenu | 设置主按钮(打开/关闭)的背景色，以及打开/关闭的图标。图标支持 Resource、Bitmap、Drawable 形式 |
| addSubMenu | 添加一个子菜单项，包括子菜单的背景色以及图标 。图标支持 Resource、Bitmap、Drawable 形式|
| openMenu | 打开菜单 |
| closeMenu | 关闭菜单 |
| isOpened | 菜单是否打开，返回 boolean 值 |
| setOnMenuSelectedListener | 设置选中子菜单项的监听器，回调方法会传递当前点击子菜单项的下标值，从 0 开始计算 |
| setOnMenuStatusChangeListener | 设置 CircleMenu 行为状态监听器，onMenuOpened 为菜单打开后的回调方法，onMenuClosed 为菜单关闭后的回调方法 |


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
 


